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

@DisplayName("Generation edge cases found in review")
class ReviewFixesTest {

    private static final String HEAD = """
            package demo;
            import jakarta.data.repository.*;
            import jakarta.nosql.*;
            import jakarta.ws.rs.PathParam;
            import jakarta.ws.rs.QueryParam;
            import org.eclipse.jnosql.rest.*;
            import java.util.*;
            """;

    private static final String ITEM = HEAD + """
            @Entity public class Item {
                @Id private Long id;
                @Column private String category;
                @Column private String notes;
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getCategory() { return category; }
                public void setCategory(String category) { this.category = category; }
                public String getNotes() { return notes; }
                public void setNotes(String notes) { this.notes = notes; }
            }
            """;

    private static CompilationHarness items(Path directory, String body) {
        return new CompilationHarness(directory).source("demo.Item", ITEM).source("demo.ItemRepository",
                HEAD + "@Repository @RestRepository(path = \"/items\") public interface ItemRepository extends CrudRepository<Item, Long> { "
                        + body + " }");
    }

    @Test
    @DisplayName("Should escape a Java keyword used as a path variable")
    void shouldEscapeKeyword(@TempDir Path directory) {
        var result = items(directory, "@jakarta.ws.rs.Path(\"/lookup/{class}\") Optional<Item> findById(Long id);")
                .compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@PathParam(\"class\") Long classValue");
    }

    @Test
    @DisplayName("Should honour an explicit path binding on a redeclared operation")
    void shouldHonourPathBinding(@TempDir Path directory) {
        var result = items(directory, "@jakarta.ws.rs.Path(\"/lookup/{key}\") "
                + "Optional<Item> findById(@PathParam(\"key\") Long id);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@PathParam(\"key\") Long key");
    }

    @Test
    @DisplayName("Should reject a conflicting path binding on a redeclared operation")
    void shouldRejectConflictingBinding(@TempDir Path directory) {
        var result = items(directory, "@jakarta.ws.rs.Path(\"/lookup/{key}\") "
                + "Optional<Item> findById(@PathParam(\"other\") Long id);").compile();

        assertThat(result.errors()).anyMatch(error -> error.contains("does not match any variable"));
    }

    @Test
    @DisplayName("Should reject a query binding on a redeclared identifier")
    void shouldRejectQueryBinding(@TempDir Path directory) {
        var result = items(directory, "void deleteById(@QueryParam(\"id\") Long id);").compile();

        assertThat(result.errors()).anyMatch(error -> error.contains("path variable"));
    }

    @Test
    @DisplayName("Should escape query names in generated string literals")
    void shouldEscapeQueryName(@TempDir Path directory) {
        var result = items(directory, "@Find List<Item> findByCategory(@QueryParam(\"a\\\"b\") Long category);")
                .compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@QueryParam(\"a\\\"b\")");
    }

    @Test
    @DisplayName("Should reject an invalid regular expression in a path variable")
    void shouldRejectRegex(@TempDir Path directory) {
        var result = items(directory, "@jakarta.ws.rs.Path(\"/lookup/{key:[}\") Optional<Item> findById(Long key);")
                .compile();

        assertThat(result.errors()).anyMatch(error -> error.contains("regular expression"));
    }

    @Test
    @DisplayName("Should accept a valid regular expression in a path variable")
    void shouldAcceptRegex(@TempDir Path directory) {
        var result = items(directory, "@jakarta.ws.rs.Path(\"/lookup/{key:[0-9]+}\") Optional<Item> findById(Long key);")
                .compile();

        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("Should keep the declared 201 members when adding the body schema")
    void shouldKeepCreatedMembers(@TempDir Path directory) {
        var result = items(directory, "@org.eclipse.microprofile.openapi.annotations.responses.APIResponse("
                + "responseCode = \"201\", name = \"made\") @Insert Item insert(Item item);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("name = \"made\"")
                .contains("implementation = Item.class");
    }

    @Test
    @DisplayName("Should treat request exclusions as sets when repositories share an entity")
    void shouldIgnoreExclusionOrder(@TempDir Path directory) {
        String first = HEAD + "@Repository @RestRepository(path = \"/a\") "
                + "@RestDTO(excludeFromRequest = {\"category\", \"notes\"}) "
                + "public interface FirstRepository extends CrudRepository<Item, Long> { }";
        String second = HEAD + "@Repository @RestRepository(path = \"/b\") "
                + "@RestDTO(excludeFromRequest = {\"notes\", \"category\"}) "
                + "public interface SecondRepository extends CrudRepository<Item, Long> { }";
        var result = new CompilationHarness(directory).source("demo.Item", ITEM)
                .source("demo.FirstRepository", first).source("demo.SecondRepository", second).compile();

        assertThat(result.errors()).isEmpty();
    }
}
