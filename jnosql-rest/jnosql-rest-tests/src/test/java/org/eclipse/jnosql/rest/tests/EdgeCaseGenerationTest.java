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

@DisplayName("Generation of edge-case repositories")
class EdgeCaseGenerationTest {

    private static final String HEAD = """
            package demo;
            import jakarta.data.*;
            import jakarta.data.page.*;
            import jakarta.data.repository.*;
            import jakarta.nosql.*;
            import org.eclipse.jnosql.rest.*;
            import java.time.*;
            import java.util.*;
            """;

    private static String entity(String body) {
        return HEAD + "@Entity public class Item { @Id private Long id; " + body + " "
                + "public Long getId() { return id; } public void setId(Long id) { this.id = id; } }";
    }

    private static String repository(String body) {
        return HEAD + "@Repository @RestRepository(path = \"/items\") public interface ItemRepository "
                + "extends CrudRepository<Item, Long> { " + body + " }";
    }

    private static CompilationHarness harness(Path directory, String entityBody, String repositoryBody) {
        return new CompilationHarness(directory).source("demo.Item", entity(entityBody))
                .source("demo.ItemRepository", repository(repositoryBody));
    }

    @Test
    @DisplayName("Should apply @RestExpose paths to redeclared standard operations")
    void shouldOverrideStandardPaths(@TempDir Path directory) {
        var result = harness(directory, "@Column private String name; public String getName() { return name; }",
                """
                @RestExpose(path = "/lookup/{id}") Optional<Item> findById(Long id);
                @RestExpose(path = "/remove/{id}") void deleteById(Long id);
                """).compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("@Path(\"/lookup/{id}\")")
                .contains("@Path(\"/remove/{id}\")")
                .doesNotContain("@Path(\"/{id}\")");
    }

    @Test
    @DisplayName("Should reject an override with the wrong path parameters")
    void shouldRejectWrongOverride(@TempDir Path directory) {
        var result = harness(directory, "", """
                @RestExpose(path = "/lookup/{key}") Optional<Item> findById(Long id);
                """).compile();

        assertThat(result.errors()).anyMatch(message -> message.contains("path parameters"));
    }

    @Test
    @DisplayName("Should reject path parameters on an operation that has none")
    void shouldRejectUnexpectedTemplate(@TempDir Path directory) {
        var result = harness(directory, "", """
                @RestExpose(path = "/all/{x}") Item save(Item entity);
                """).compile();

        assertThat(result.errors()).anyMatch(message -> message.contains("must not declare path parameters"));
    }

    @Test
    @DisplayName("Should infer the entity through methods inherited from a generic interface")
    void shouldInferThroughGenericParent(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", entity("@Column private String name; public String getName() { return name; }"))
                .source("demo.Queries", HEAD + "public interface Queries<T> { @Find List<T> byName(String name); }")
                .source("demo.ItemRepository", HEAD + "@Repository @RestRepository(path = \"/items\") "
                        + "public interface ItemRepository extends Queries<Item> { }").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("byName");
    }

    @Test
    @DisplayName("Should generate next to a nested repository, in the package of the outer class")
    void shouldSupportNestedRepository(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", entity(""))
                .source("demo.Repositories", HEAD + "public class Repositories { @Repository "
                        + "@RestRepository(path = \"/items\") public interface ItemRepository "
                        + "extends CrudRepository<Item, Long> { } }").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).startsWith("package demo;");
    }

    @Test
    @DisplayName("Should report a clear error for a repository in the unnamed package")
    void shouldRejectUnnamedPackage(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("Plain", "import jakarta.nosql.*; @Entity public class Plain { @Id Long id; }")
                .source("PlainRepository", "import jakarta.data.repository.*; import org.eclipse.jnosql.rest.*; "
                        + "@Repository @RestRepository(path = \"/p\") public interface PlainRepository "
                        + "extends CrudRepository<Plain, Long> { }").compile();

        assertThat(result.errors()).anyMatch(message -> message.contains("unnamed package"));
    }

    @Test
    @DisplayName("Should bind java.time parameters through generated parsing")
    void shouldParseTimeParameters(@TempDir Path directory) {
        var result = harness(directory, "@Column private LocalDate day; public LocalDate getDay() { return day; }",
                "@Find List<Item> byDay(LocalDate day);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("@QueryParam(\"day\") String dayText")
                .contains("QueryValues.parse(\"day\", dayText, LocalDate::parse)");
    }

    @Test
    @DisplayName("Should accept only the entity identifier for the cursor ordering")
    void shouldRequireRealIdentifier(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", HEAD + "@Entity public class Item { @Id private String sku; "
                        + "@Column private String id; public String getSku() { return sku; } "
                        + "public void setSku(String sku) { this.sku = sku; } public String getId() { return id; } "
                        + "public void setId(String id) { this.id = id; } }")
                .source("demo.ItemRepository", HEAD + "@Repository @RestRepository(path = \"/items\") "
                        + "public interface ItemRepository extends CrudRepository<Item, String> { "
                        + "@Find @RestExpose(path = \"/bad\") @OrderBy(\"id\") "
                        + "CursoredPage<Item> bad(PageRequest page); "
                        + "@Find @RestExpose(path = \"/good\") @OrderBy(\"id\") @OrderBy(\"sku\") "
                        + "CursoredPage<Item> good(PageRequest page); }").compile();

        assertThat(result.errors()).hasSize(1).allMatch(message -> message.contains("sku"));
    }

    @Test
    @DisplayName("Should find the identifier stored on a record component for cursor ordering")
    void shouldScanRecordAnnotations(@TempDir Path directory) {
        var result = new CompilationHarness(directory)
                .source("demo.Item", HEAD + "@Entity public record Item(@Id Long code, String name) { }")
                .source("demo.ItemRepository", HEAD + "@Repository @RestRepository(path = \"/items\") "
                        + "public interface ItemRepository extends BasicRepository<Item, Long> { "
                        + "@Find @OrderBy(\"code\") CursoredPage<Item> byName(String name, PageRequest page); }")
                .compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("byName");
    }
}
