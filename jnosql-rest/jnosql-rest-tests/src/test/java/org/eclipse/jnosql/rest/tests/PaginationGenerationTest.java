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

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pagination code generation")
class PaginationGenerationTest {

    private static final String ENTITY = """
            package demo;
            import jakarta.nosql.Column;
            import jakarta.nosql.Entity;
            import jakarta.nosql.Id;
            @Entity
            public class Item {
                @Id private Long id;
                @Column private String name;
                @Column private String secret;
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public String getSecret() { return secret; }
                public void setSecret(String secret) { this.secret = secret; }
            }
            """;

    private static CompilationHarness harness(Path directory, String body) {
        return new CompilationHarness(directory)
                .source("demo.Item", ENTITY)
                .source("demo.ItemRepository", """
                        package demo;
                        import jakarta.data.page.*;
                        import jakarta.data.repository.*;
                        import org.eclipse.jnosql.rest.*;
                        import java.util.*;
                        @Repository
                        @RestRepository(path = "/items")
                        public interface ItemRepository extends CrudRepository<Item, Long> {
                        %s
                        }
                        """.formatted(body));
    }

    @Nested
    @DisplayName("Offset pagination")
    class Offset {

        @Test
        @DisplayName("Should generate offset, size and includeTotal parameters and a PageResponse")
        void shouldGenerate(@TempDir Path directory) {
            var result = harness(directory, "@Find Page<Item> findByName(String name, PageRequest pageRequest);")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@GET")
                    .contains("@Path(\"/findByName\")")
                    .contains("public PageResponse<Item> findByName(")
                    .contains("@QueryParam(\"name\") String name")
                    .contains("@QueryParam(\"offset\") String offset")
                    .contains("@QueryParam(\"size\") String size")
                    .contains("@QueryParam(\"includeTotal\") String includeTotal")
                    .contains("PageRequest pageRequest = Pagination.offset(offset, size, includeTotal);")
                    .contains("Page<Item> page = repository.findByName(name, pageRequest);")
                    .contains("return PageResponse.of(page);");
        }

        @Test
        @DisplayName("Should compile without OpenAPI and MapStruct")
        void shouldCompileWithoutOptionalDependencies(@TempDir Path directory) {
            var result = harness(directory, "@Find Page<Item> findByName(String name, PageRequest pageRequest);")
                    .withoutOpenApi().withoutMapStruct().compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("openapi");
        }

        @Test
        @DisplayName("Should describe the pagination parameters when OpenAPI is available")
        void shouldDescribeParameters(@TempDir Path directory) {
            var result = harness(directory, "@Find Page<Item> findByName(String name, PageRequest pageRequest);")
                    .compile();

            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@Parameter(description = \"Zero-based page offset");
        }

        @Test
        @DisplayName("Should map page content to DTOs without the excluded attribute")
        void shouldMapDtos(@TempDir Path directory) {
            var result = new CompilationHarness(directory)
                    .source("demo.Item", ENTITY)
                    .source("demo.ItemRepository", """
                            package demo;
                            import jakarta.data.page.*;
                            import jakarta.data.repository.*;
                            import org.eclipse.jnosql.rest.*;
                            @Repository
                            @RestRepository(path = "/items")
                            @RestDTO(exclude = {"secret"})
                            public interface ItemRepository extends BasicRepository<Item, Long> {
                                @Find Page<Item> findByName(String name, PageRequest pageRequest);
                            }
                            """).compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("public PageResponse<ItemDTO> findByName(")
                    .contains("return PageResponse.of(page, content -> mapper.toDTO(content));");
            assertThat(result.source("ItemDTO")).doesNotContain("secret");
        }

        @Test
        @DisplayName("Should reject a PageRequest on a method that does not return a page")
        void shouldRejectPageRequestWithoutPage(@TempDir Path directory) {
            var result = harness(directory, "@Find List<Item> findByName(String name, PageRequest pageRequest);")
                    .compile();

            assertThat(result.warnings()).anyMatch(warning -> warning.contains("PageRequest"));
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("findByName");
        }

        @Test
        @DisplayName("Should reject a page without a PageRequest")
        void shouldRejectPageWithoutRequest(@TempDir Path directory) {
            var result = harness(directory, "@Find Page<Item> findByName(String name);").compile();

            assertThat(result.warnings()).anyMatch(warning -> warning.contains("PageRequest"));
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("findByName");
        }

        @Test
        @DisplayName("Should reject a business parameter that clashes with a pagination parameter")
        void shouldRejectClashes(@TempDir Path directory) {
            var result = harness(directory, "@Find Page<Item> findByName(String name, String size, PageRequest r);")
                    .compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(error -> error.contains("clashes"));
        }
    }

    @Nested
    @DisplayName("Cursor pagination")
    class Cursor {

        private static final String CURSOR = "@Find @OrderBy(\"name\") @OrderBy(\"id\") "
                + "CursoredPage<Item> findByName(String name, PageRequest pageRequest);";

        @Test
        @DisplayName("Should generate cursor and size parameters and a CursorPageResponse")
        void shouldGenerate(@TempDir Path directory) {
            var result = harness(directory, CURSOR).compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("public CursorPageResponse<Item> findByName(")
                    .contains("@QueryParam(\"cursor\") String cursor")
                    .contains("@QueryParam(\"size\") String size")
                    .contains("String cursorContext = Pagination.context(\"findByName\", name);")
                    .contains("PageRequest pageRequest = Pagination.cursor(cursor, size, cursorContext);")
                    .contains("CursoredPage<Item> page = repository.findByName(name, pageRequest);")
                    .contains("return CursorPageResponse.of(page, cursorContext);");
            String source = result.source("ItemRepositoryResource");
            assertThat(source.substring(source.indexOf("CursorPageResponse<Item> findByName(")))
                    .doesNotContain("@QueryParam(\"offset\")");
        }

        @Test
        @DisplayName("Should compile without OpenAPI and MapStruct")
        void shouldCompileWithoutOptionalDependencies(@TempDir Path directory) {
            var result = harness(directory, CURSOR).withoutOpenApi().withoutMapStruct().compile();

            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("Should fail the build when an explicitly exposed cursor method has no unique ordering")
        void shouldFailWhenExplicit(@TempDir Path directory) {
            var result = harness(directory, "@Find @RestExpose @OrderBy(\"name\") "
                    + "CursoredPage<Item> findByName(String name, PageRequest r);").compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(error -> error.contains("not provably unique"));
        }

        @Test
        @DisplayName("Should accept an ordering declared in the method name")
        void shouldAcceptMethodNameOrdering(@TempDir Path directory) {
            var result = harness(directory,
                    "CursoredPage<Item> findByNameOrderByNameAscIdAsc(String name, PageRequest pageRequest);").compile();

            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("Should accept an ordering declared in a query")
        void shouldAcceptQueryOrdering(@TempDir Path directory) {
            var result = harness(directory, "@Query(\"WHERE name = :name ORDER BY name, id\") "
                    + "CursoredPage<Item> byName(String name, PageRequest pageRequest);").compile();

            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("Should reject a missing ordering")
        void shouldRejectMissingOrdering(@TempDir Path directory) {
            var result = harness(directory, "@Find CursoredPage<Item> findByName(String name, PageRequest r);")
                    .compile();

            assertThat(result.warnings()).anyMatch(warning -> warning.contains("ordering"));
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("findByName");
        }

        @Test
        @DisplayName("Should reject an ordering that is not provably unique")
        void shouldRejectAmbiguousOrdering(@TempDir Path directory) {
            var result = harness(directory, "@Find @OrderBy(\"name\") "
                    + "CursoredPage<Item> findByName(String name, PageRequest r);").compile();

            assertThat(result.warnings()).anyMatch(warning -> warning.contains("not provably unique"));
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("findByName");
        }

        @Test
        @DisplayName("Should generate a paginated restriction search with a pagination member")
        void shouldGenerateSearch(@TempDir Path directory) {
            var result = harness(directory, "@Find @RestExpose(path = \"/search\") @OrderBy(\"name\") @OrderBy(\"id\") "
                    + "CursoredPage<Item> search(jakarta.data.restrict.Restriction<Item> r, PageRequest p);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositorySearchRequest"))
                    .contains("PaginationRequest pagination")
                    .contains("public String criteria()");
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@QUERY")
                    .contains("Pagination.context(\"search\", request.criteria())")
                    .contains("Pagination.cursor(request.pagination(), cursorContext)")
                    .contains("repository.search(request.toRestriction(), p)");
        }

        @Test
        @DisplayName("Should generate an offset search")
        void shouldGenerateOffsetSearch(@TempDir Path directory) {
            var result = harness(directory, "@Find @RestExpose(path = \"/search\") "
                    + "Page<Item> search(jakarta.data.restrict.Restriction<Item> r, PageRequest p);").compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("Pagination.offset(request.pagination())");
        }

        @Test
        @DisplayName("Should reject searches that disagree on pagination")
        void shouldRejectMixedSearches(@TempDir Path directory) {
            var result = harness(directory, "@Find @RestExpose(path = \"/a\") "
                    + "Page<Item> a(jakarta.data.restrict.Restriction<Item> r, PageRequest p);"
                    + "@Find @RestExpose(path = \"/b\") List<Item> b(jakarta.data.restrict.Restriction<Item> r);")
                    .compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(error -> error.contains("paginate"));
        }
    }
}
