/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Absence of Jakarta Data 1.1 restriction support")
class RestrictionExclusionTest {

    private static final String ENTITY = """
            package demo;
            import jakarta.nosql.*;
            @Entity
            public class Item {
                @Id private Long id;
                @Column private String name;
                @Column private String category;
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public String getCategory() { return category; }
                public void setCategory(String category) { this.category = category; }
            }
            """;

    private static final String REPOSITORY = """
            package demo;
            import jakarta.data.repository.*;
            import org.eclipse.jnosql.rest.*;
            import java.util.*;
            @Repository
            @RestRepository(path = "/items")
            public interface ItemRepository extends BasicRepository<Item, Long> {
                @Find List<Item> findByCategory(String category);
                Optional<Item> findByName(String name);
                @Query("where name = :name") List<Item> named(String name);
            }
            """;

    @Test
    @DisplayName("Should not provide the Jakarta Data restriction API on the classpath")
    void shouldNotHaveRestrictionApi() {
        assertThatThrownBy(() -> Class.forName("jakarta.data.restrict.Restriction"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("Should not provide an HTTP QUERY designator")
    void shouldNotHaveQueryDesignator() {
        assertThatThrownBy(() -> Class.forName("org.eclipse.jnosql.rest.QUERY"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("Should not ship a search request template")
    void shouldNotShipSearchTemplate() {
        assertThat(getClass().getClassLoader().getResource("rest-search-request.mustache")).isNull();
    }

    @Test
    @DisplayName("Should generate derived and annotated queries without restriction artifacts")
    void shouldGenerateOrdinaryQueries(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", ENTITY)
                .source("demo.ItemRepository", REPOSITORY)
                .compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.success()).isTrue();
        assertThat(result.generated("ItemRepositoryResource")).isTrue();
        assertThat(result.generated("ItemRepositorySearchRequest")).isFalse();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("findByCategory")
                .contains("findByName")
                .contains("named")
                .doesNotContain("jakarta.data.restrict")
                .doesNotContain("Restriction")
                .doesNotContain("@QUERY")
                .doesNotContain("SearchRequest")
                .doesNotContain("/search");
    }

    @Test
    @DisplayName("Should report a diagnostic instead of generating a search endpoint for an unknown parameter type")
    void shouldReportUnsupportedParameter(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", ENTITY)
                .source("demo.ItemRepository", REPOSITORY.replace("@Query(\"where name",
                        "@Find List<Item> search(java.util.function.Predicate<Item> filter);\n    @Query(\"where name"))
                .compile();

        assertThat(result.generated("ItemRepositorySearchRequest")).isFalse();
        if (result.generated("ItemRepositoryResource")) {
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("@QUERY").doesNotContain("/search");
        } else {
            assertThat(result.errors()).isNotEmpty();
        }
    }
}
