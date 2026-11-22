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

@DisplayName("Limit, Sort, Order and findAll code generation")
class SortingGenerationTest {

    private static final String ENTITY = """
            package demo;
            import jakarta.nosql.Column;
            import jakarta.nosql.Entity;
            import jakarta.nosql.Id;
            @Entity
            public class Item {
                @Id private Long id;
                @Column private String name;
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """;

    private static CompilationHarness harness(Path directory, String parent, String body) {
        return new CompilationHarness(directory)
                .source("demo.Item", ENTITY)
                .source("demo.ItemRepository", """
                        package demo;
                        import jakarta.data.*;
                        import jakarta.data.page.*;
                        import jakarta.data.repository.*;
                        import org.eclipse.jnosql.rest.*;
                        import java.util.*;
                        @Repository
                        @RestRepository(path = "/items")
                        public interface ItemRepository extends %s<Item, Long> {
                        %s
                        }
                        """.formatted(parent, body));
    }

    @Test
    @DisplayName("Should serve the collection root with the paginated findAll for Basic repositories")
    void shouldGenerateBasicRoot(@TempDir Path directory) {
        assertRoot(harness(directory, "BasicRepository", "").compile());
    }

    @Test
    @DisplayName("Should serve the collection root with the paginated findAll for Crud repositories")
    void shouldGenerateCrudRoot(@TempDir Path directory) {
        assertRoot(harness(directory, "CrudRepository", "").compile());
    }

    private static void assertRoot(CompilationHarness.Result result) {
        assertThat(result.errors()).isEmpty();
        String source = result.source("ItemRepositoryResource");
        assertThat(source)
                .contains("repository.findAll(pageRequest, order)")
                .contains("@QueryParam(\"sort\") String sort")
                .doesNotContain("repository.findAll()");
    }

    @Test
    @DisplayName("Should not invent a root endpoint when no paginated findAll exists")
    void shouldNotInventRoot(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", ENTITY)
                .source("demo.ItemRepository", """
                        package demo;
                        import jakarta.data.repository.*;
                        import org.eclipse.jnosql.rest.*;
                        import java.util.*;
                        @Repository
                        @RestRepository(path = "/items")
                        public interface ItemRepository extends DataRepository<Item, Long> {
                            @Find List<Item> byName(String name);
                        }
                        """).compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("findAll").contains("byName");
    }

    @Test
    @DisplayName("Should generate Limit, Sort and Order parameters for custom methods")
    void shouldGenerateParameters(@TempDir Path directory) {
        var result = harness(directory, "BasicRepository", """
                @Find List<Item> byLimit(String name, Limit limit);
                @Find List<Item> bySort(String name, Sort<Item> sort);
                @Find List<Item> byOrder(String name, Order<Item> order);
                """).compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("@QueryParam(\"limit\") String limit")
                .contains("@QueryParam(\"startAt\") String startAt")
                .contains("Limits.of(limit, startAt)")
                .contains("Sorting.sort(")
                .contains("Sorting.order(");
    }

    @Test
    @DisplayName("Should reject Limit combined with PageRequest")
    void shouldRejectLimitWithPageRequest(@TempDir Path directory) {
        var result = harness(directory, "BasicRepository", """
                @jakarta.ws.rs.Path("/bad")
                @Find Page<Item> bad(String name, Limit limit, PageRequest pageRequest);
                """).compile();

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject a Sort of another entity type")
    void shouldRejectWrongEntity(@TempDir Path directory) {
        var result = harness(directory, "BasicRepository", """
                @jakarta.ws.rs.Path("/bad")
                @Find List<Item> bad(String name, Sort<String> sort);
                """).compile();

        assertThat(result.success()).isFalse();
    }

    @Test
    @DisplayName("Should compile without OpenAPI and without MapStruct")
    void shouldCompileWithoutOptionalLibraries(@TempDir Path directory) {
        var result = harness(directory, "CrudRepository", """
                @Find List<Item> byOrder(String name, Order<Item> order, Limit limit);
                """).withoutOpenApi().withoutMapStruct().compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("org.eclipse.microprofile.openapi");
    }
}
