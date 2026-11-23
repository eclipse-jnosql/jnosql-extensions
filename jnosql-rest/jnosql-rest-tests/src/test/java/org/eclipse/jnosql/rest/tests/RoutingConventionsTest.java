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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Convention-based routing and parameter binding")
class RoutingConventionsTest {

    private static final String HEAD = """
            package demo;
            import jakarta.data.page.*;
            import jakarta.data.repository.*;
            import jakarta.nosql.*;
            import jakarta.ws.rs.PathParam;
            import jakarta.ws.rs.QueryParam;
            import org.eclipse.jnosql.rest.*;
            import java.math.BigDecimal;
            import java.util.*;
            """;

    private static final String ITEM = HEAD + """
            @Entity public class Item {
                @Id private Long id;
                @Column private String category;
                @Column private BigDecimal price;
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getCategory() { return category; }
                public void setCategory(String category) { this.category = category; }
                public BigDecimal getPrice() { return price; }
                public void setPrice(BigDecimal price) { this.price = price; }
            }
            """;

    private static final String COMPANY = HEAD + """
            @Entity public class Company {
                @Id private String cnpj;
                @Column private String name;
                public String getCnpj() { return cnpj; }
                public void setCnpj(String cnpj) { this.cnpj = cnpj; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """;

    private static CompilationHarness items(Path directory, String annotation, String body) {
        return new CompilationHarness(directory).source("demo.Item", ITEM).source("demo.ItemRepository",
                HEAD + "@Repository " + annotation + " public interface ItemRepository { " + body + " }");
    }

    private static CompilationHarness items(Path directory, String body) {
        return items(directory, "@RestRepository(path = \"/items\")", body);
    }

    @Nested
    @DisplayName("Default routes")
    class DefaultRoutes {

        @Test
        @DisplayName("Should route inherited operations to resource-oriented paths")
        void shouldRouteInherited(@TempDir Path directory) {
            var result = new CompilationHarness(directory).source("demo.Item", ITEM)
                    .source("demo.ItemRepository", HEAD + "@Repository @RestRepository(path = \"/items\") "
                            + "public interface ItemRepository extends CrudRepository<Item, Long> { }")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@POST")
                    .contains("@GET\n    @Path(\"/{id}\")")
                    .contains("@DELETE\n    @Path(\"/{id}\")")
                    .contains("@PathParam(\"id\")")
                    .doesNotContain("\"/insert\"")
                    .doesNotContain("\"/findById\"");
        }

        @Test
        @DisplayName("Should use the identifier attribute name instead of id")
        void shouldUseIdentifierName(@TempDir Path directory) {
            var result = new CompilationHarness(directory).source("demo.Company", COMPANY)
                    .source("demo.CompanyRepository", HEAD + "@Repository @RestRepository(path = \"/companies\") "
                            + "public interface CompanyRepository extends CrudRepository<Company, String> { }")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("CompanyRepositoryResource"))
                    .contains("@Path(\"/{cnpj}\")")
                    .contains("@PathParam(\"cnpj\")");
        }

        @Test
        @DisplayName("Should route a finder proven to search by primary key to the identifier")
        void shouldRouteProvenFinder(@TempDir Path directory) {
            var result = new CompilationHarness(directory).source("demo.Company", COMPANY)
                    .source("demo.CompanyRepository", HEAD + "@Repository @RestRepository(path = \"/companies\") "
                            + "public interface CompanyRepository { "
                            + "@Find Optional<Company> find(@By(\"id(this)\") String identifier); }")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("CompanyRepositoryResource")).contains("@Path(\"/{identifier}\")")
                    .contains("@PathParam(\"identifier\")");
        }

        @Test
        @DisplayName("Should not guess the identifier for an ordinary attribute")
        void shouldNotGuess(@TempDir Path directory) {
            var result = new CompilationHarness(directory).source("demo.Company", COMPANY)
                    .source("demo.CompanyRepository", HEAD + "@Repository @RestRepository(path = \"/companies\") "
                            + "public interface CompanyRepository { "
                            + "@Find Optional<Company> find(String name); }")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("CompanyRepositoryResource")).contains("@Path(\"/find\")")
                    .contains("@QueryParam(\"name\")");
        }

        @Test
        @DisplayName("Should route other finders to the method name with query parameters")
        void shouldRouteFinderByName(@TempDir Path directory) {
            var result = items(directory, "@Find List<Item> find(String category, BigDecimal price);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/find\")")
                    .contains("@QueryParam(\"category\")").contains("@QueryParam(\"price\")");
        }

        @Test
        @DisplayName("Should not turn multiple parameters into path segments")
        void shouldKeepQueryParameters(@TempDir Path directory) {
            var result = items(directory, "@Find List<Item> findByIdAndCategory(Long id, String category);")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/findByIdAndCategory\")")
                    .doesNotContain("@PathParam");
        }
    }

    @Nested
    @DisplayName("Explicit @Path")
    class ExplicitPath {

        @Test
        @DisplayName("Should use the declared path without the method name")
        void shouldUseDeclaredPath(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/by-category\") "
                    + "List<Item> findByCategory(String category);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/by-category\")")
                    .contains("@QueryParam(\"category\")").doesNotContain("\"/findByCategory\"");
        }

        @Test
        @DisplayName("Should infer a path parameter from the template")
        void shouldInferPathParameter(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{category}\") "
                    + "List<Item> findByCategory(String category);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/{category}\")")
                    .contains("@PathParam(\"category\") String category");
        }

        @Test
        @DisplayName("Should mix path and query parameters")
        void shouldMix(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{category}\") "
                    + "List<Item> find(String category, BigDecimal price);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@PathParam(\"category\") String category")
                    .contains("@QueryParam(\"price\")");
        }

        @Test
        @DisplayName("Should bind several path variables")
        void shouldBindSeveral(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{category}/suppliers/{supplierId}\") "
                    + "List<Item> find(String category, UUID supplierId, BigDecimal price);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@PathParam(\"category\") String category")
                    .contains("@PathParam(\"supplierId\") UUID supplierId")
                    .contains("@QueryParam(\"price\")");
        }

        @Test
        @DisplayName("Should honour explicit bindings whatever the Java parameter name")
        void shouldHonourExplicit(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{category}\") "
                    + "List<Item> a(@PathParam(\"category\") String value);\n"
                    + "@Find @jakarta.ws.rs.Path(\"/by-category\") "
                    + "List<Item> b(@QueryParam(\"category\") String value);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@PathParam(\"category\") String value")
                    .contains("@QueryParam(\"category\") String value");
        }

        @Test
        @DisplayName("Should keep @By independent from the HTTP name")
        void shouldKeepByIndependent(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/by-category\") "
                    + "List<Item> a(@By(\"category\") String value);\n"
                    + "@Find @jakarta.ws.rs.Path(\"/other\") "
                    + "List<Item> b(@By(\"category\") @QueryParam(\"category\") String value);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@QueryParam(\"value\") String value")
                    .contains("@QueryParam(\"category\") String value");
        }

        @Test
        @DisplayName("Should route an explicit empty path to the collection root")
        void shouldRouteEmptyPath(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"\") "
                    + "Page<Item> findAll(PageRequest request);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("\"/findAll\"")
                    .doesNotContain("@Path(\"\")");
        }

        @Test
        @DisplayName("Should normalize a slash path like an empty path")
        void shouldNormalizeSlash(@TempDir Path directory) {
            var slash = items(directory.resolve("a"), "@Find @jakarta.ws.rs.Path(\"/\") "
                    + "Page<Item> findAll(PageRequest request);").compile();
            var empty = items(directory.resolve("b"), "@Find @jakarta.ws.rs.Path(\"\") "
                    + "Page<Item> findAll(PageRequest request);").compile();

            assertThat(slash.errors()).isEmpty();
            assertThat(slash.source("ItemRepositoryResource")).isEqualTo(empty.source("ItemRepositoryResource"));
        }
    }

    @Nested
    @DisplayName("Exposure")
    class Exposure {

        @Test
        @DisplayName("Should expose only methods with @Path under EXPLICIT, including the root")
        void shouldExposeExplicit(@TempDir Path directory) {
            var result = items(directory, "@RestRepository(path = \"/items\", exposure = RestExposure.EXPLICIT)",
                    "@Find @jakarta.ws.rs.Path(\"/by-category\") List<Item> findByCategory(String category);\n"
                            + "@Find List<Item> findByPrice(BigDecimal price);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).contains("findByCategory")
                    .doesNotContain("findByPrice").doesNotContain("findById(");
        }

        @Test
        @DisplayName("Should not let @Path bypass @RestExclude")
        void shouldKeepExclude(@TempDir Path directory) {
            var result = items(directory, "@RestExclude @Find @jakarta.ws.rs.Path(\"/x\") "
                    + "List<Item> findByCategory(String category);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.generated("ItemRepositoryResource")).isFalse();
        }
    }

    @Nested
    @DisplayName("Compile-time validation")
    class Validation {

        @Test
        @DisplayName("Should reject a template variable without a parameter")
        void shouldRejectMissingBinding(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{cnpj}\") "
                    + "Optional<Item> find(String company);").compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(message -> message.contains("{cnpj}"));
        }

        @Test
        @DisplayName("Should accept the missing binding once @PathParam names it")
        void shouldAcceptFixedBinding(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{cnpj}\") "
                    + "Optional<Item> find(@PathParam(\"cnpj\") String company);").compile();

            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("Should reject @QueryParam that names a path variable")
        void shouldRejectConflicting(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{cnpj}\") "
                    + "Optional<Item> find(@QueryParam(\"cnpj\") String cnpj);").compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(message -> message.contains("conflicts with the path variable"));
        }

        @Test
        @DisplayName("Should reject @PathParam outside the template")
        void shouldRejectUnknownPathParam(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/x\") "
                    + "List<Item> find(@PathParam(\"y\") String y);").compile();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should reject both @PathParam and @QueryParam on a parameter")
        void shouldRejectBoth(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{a}\") "
                    + "List<Item> find(@PathParam(\"a\") @QueryParam(\"a\") String a);").compile();

            assertThat(result.errors()).anyMatch(message -> message.contains("both"));
        }

        @Test
        @DisplayName("Should reject two parameters bound to the same variable")
        void shouldRejectDuplicates(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{a}\") "
                    + "List<Item> find(@PathParam(\"a\") String x, @PathParam(\"a\") String y);").compile();

            assertThat(result.errors()).anyMatch(message -> message.contains("more than one parameter"));
        }

        @Test
        @DisplayName("Should reject duplicate query parameter names")
        void shouldRejectDuplicateQuery(@TempDir Path directory) {
            var result = items(directory, "@Find List<Item> find(@QueryParam(\"a\") String x, "
                    + "@QueryParam(\"a\") String y);").compile();

            assertThat(result.errors()).anyMatch(message -> message.contains("more than one parameter"));
        }

        @Test
        @DisplayName("Should reject an invalid URI template")
        void shouldRejectInvalidTemplate(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{a\") "
                    + "List<Item> find(String a);").compile();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should reject a repeated path variable")
        void shouldRejectRepeatedVariable(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{a}/{a}\") "
                    + "List<Item> find(String a);").compile();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should reject unsupported Jakarta REST binding annotations")
        void shouldRejectHeaderParam(@TempDir Path directory) {
            var result = items(directory, "@Find List<Item> find(@jakarta.ws.rs.HeaderParam(\"a\") String a);")
                    .compile();

            assertThat(result.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("Route collisions")
    class Collisions {

        @Test
        @DisplayName("Should reject structurally equivalent GET routes")
        void shouldRejectEquivalentTemplates(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/{id}\") Optional<Item> a(Long id);\n"
                    + "@Find @jakarta.ws.rs.Path(\"/{category}\") List<Item> b(String category);").compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(message -> message.contains("GET"));
        }

        @Test
        @DisplayName("Should reject routes that differ only by query parameters")
        void shouldRejectQueryOnlyDifference(@TempDir Path directory) {
            var result = items(directory, "@Find @jakarta.ws.rs.Path(\"/same\") List<Item> a(String category);\n"
                    + "@Find @jakarta.ws.rs.Path(\"/same\") List<Item> b(BigDecimal price);").compile();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should reject overloaded methods that resolve to the same route")
        void shouldRejectOverloads(@TempDir Path directory) {
            var result = items(directory, "@Find List<Item> find(String category);\n"
                    + "@Find List<Item> find(BigDecimal price);").compile();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should reject a custom route colliding with a canonical one")
        void shouldRejectCanonicalCollision(@TempDir Path directory) {
            var result = new CompilationHarness(directory).source("demo.Item", ITEM)
                    .source("demo.ItemRepository", HEAD + "@Repository @RestRepository(path = \"/items\") "
                            + "public interface ItemRepository extends CrudRepository<Item, Long> { "
                            + "@Find @jakarta.ws.rs.Path(\"/{key}\") Optional<Item> other(Long key); }")
                    .compile();

            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("Should allow different HTTP methods on the same route")
        void shouldAllowDifferentMethods(@TempDir Path directory) {
            var result = new CompilationHarness(directory).source("demo.Item", ITEM)
                    .source("demo.ItemRepository", HEAD + "@Repository @RestRepository(path = \"/items\") "
                            + "public interface ItemRepository extends BasicRepository<Item, Long> { "
                            + "@Delete void deleteById(Long id); }")
                    .compile();

            assertThat(result.errors()).isEmpty();
        }
    }

    @Test
    @DisplayName("Should no longer ship the RestExpose annotation")
    void shouldNotShipRestExpose() {
        var removed = false;
        try {
            Class.forName("org.eclipse.jnosql.rest.RestExpose");
        } catch (ClassNotFoundException exception) {
            removed = true;
        }
        assertThat(removed).isTrue();
    }

    @Test
    @DisplayName("Should keep RestExpose out of the REST sources")
    void shouldKeepSourcesClean() throws Exception {
        Path root = Path.of("..").toAbsolutePath().normalize();
        try (Stream<Path> files = Files.walk(root)) {
            var offenders = files.filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("/target/"))
                    .filter(path -> path.toString().matches(".*\\.(java|mustache)"))
                    .filter(path -> !path.getFileName().toString().equals("RoutingConventionsTest.java"))
                    .filter(path -> read(path).contains("RestExpose")).toList();
            assertThat(offenders).isEmpty();
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }
}
