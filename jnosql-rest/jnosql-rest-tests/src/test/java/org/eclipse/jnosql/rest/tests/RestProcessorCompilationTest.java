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

@DisplayName("REST annotation processor compilation")
class RestProcessorCompilationTest {

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

    private static String repository(String annotations, String body) {
        return """
                package demo;
                import jakarta.data.repository.*;
                import org.eclipse.jnosql.rest.*;
                import java.util.*;
                @Repository
                %s
                public interface ItemRepository extends CrudRepository<Item, Long> {
                %s
                }
                """.formatted(annotations, body);
    }

    private CompilationHarness harness(Path directory, String annotations, String body) {
        return new CompilationHarness(directory)
                .source("demo.Item", ENTITY)
                .source("demo.ItemRepository", repository(annotations, body));
    }

    @Nested
    @DisplayName("When the configuration is valid")
    class WhenValid {

        @Test
        @DisplayName("Should generate a resource without OpenAPI or MapStruct on the classpath")
        void shouldCompileWithoutOptionalDependencies(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")", "List<Item> findByName(String name);")
                    .withoutOpenApi().withoutMapStruct().compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@Path(\"/items\")")
                    .doesNotContain("openapi")
                    .doesNotContain("mapstruct");
        }

        @Test
        @DisplayName("Should name the resource after the complete repository interface plus the Resource suffix")
        void shouldUseTheResourceSuffix(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")", "").compile();

            assertThat(result.success()).isTrue();
            assertThat(result.generated("ItemRepositoryResource")).isTrue();
            assertThat(result.generated("ItemRepositoryRepository")).isFalse();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("package demo;")
                    .contains("public class ItemRepositoryResource {")
                    .contains("    ItemRepositoryResource() {")
                    .contains("    public ItemRepositoryResource(ItemRepository repository) {")
                    .contains("@Path(\"/items\")");
        }

        @Test
        @DisplayName("Should generate only the explicitly exposed methods under EXPLICIT")
        void shouldHonourExplicitExposure(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\", exposure = RestExposure.EXPLICIT)",
                    "@jakarta.ws.rs.Path(\"/by-name\") List<Item> findByName(String name);\nList<Item> findBySecret(String secret);")
                    .compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("findByName")
                    .doesNotContain("findBySecret")
                    .doesNotContain("deleteById");
        }

        @Test
        @DisplayName("Should skip an excluded operation category")
        void shouldHonourOperationExclusions(@TempDir Path directory) {
            var result = harness(directory,
                    "@RestRepository(path = \"/items\", exclude = {RestOperation.DELETE})", "")
                    .compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("findById")
                    .doesNotContain("deleteById");
        }

        @Test
        @DisplayName("Should skip methods annotated with @RestExclude")
        void shouldHonourMethodExclusion(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")",
                    "@RestExclude List<Item> findBySecret(String secret);").compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("findBySecret");
        }

        @Test
        @DisplayName("Should generate nothing under NONE")
        void shouldGenerateNothingUnderNone(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\", exposure = RestExposure.NONE)", "")
                    .compile();

            assertThat(result.success()).isTrue();
            assertThat(result.generated("ItemRepositoryResource")).isFalse();
        }

        @Test
        @DisplayName("Should omit excluded attributes from the generated response DTO and let MapStruct implement the mapper")
        void shouldExcludeAttributes(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")\n@RestDTO(exclude = {\"secret\"})", "")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemDTO")).contains("name").doesNotContain("secret");
            assertThat(result.source("ItemDTOMapper"))
                    .contains("import org.mapstruct.MappingConstants;")
                    .contains("import org.mapstruct.ReportingPolicy;")
                    .contains("componentModel = MappingConstants.ComponentModel.JAKARTA_CDI")
                    .contains("unmappedTargetPolicy = ReportingPolicy.IGNORE")
                    .doesNotContain("\"jakarta-cdi\"");
            assertThat(result.generated("ItemDTOMapperImpl")).isTrue();
        }

        @Test
        @DisplayName("Should reuse a developer-defined DTO instead of overwriting it")
        void shouldReuseDeveloperDto(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")\n@RestDTO", "")
                    .source("demo.ItemDTO", "package demo;\npublic record ItemDTO(Long id, String name) { }\n")
                    .compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.generated("ItemDTO")).isFalse();
        }
    }

    @Nested
    @DisplayName("When the configuration is invalid")
    class WhenInvalid {

        @Test
        @DisplayName("Should let @RestExclude win over @Path")
        void shouldLetExcludeWin(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")",
                    "@jakarta.ws.rs.Path(\"/by-name\") @RestExclude List<Item> findByName(String name);")
                    .compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("findByName");
        }

        @Test
        @DisplayName("Should not let @Path override RestExposure.NONE")
        void shouldIgnorePathUnderNone(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\", exposure = RestExposure.NONE)",
                    "@jakarta.ws.rs.Path(\"/by-name\") List<Item> findByName(String name);").compile();

            assertThat(result.success()).isTrue();
            assertThat(result.generated("ItemRepositoryResource")).isFalse();
        }

        @Test
        @DisplayName("Should reject an unknown excluded attribute")
        void shouldRejectUnknownExclusion(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")\n@RestDTO(exclude = {\"missing\"})", "")
                    .compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(message -> message.contains("missing"));
        }

        @Test
        @DisplayName("Should reject a repository path collision")
        void shouldRejectPathCollision(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")",
                    "@jakarta.ws.rs.Path(\"/same\") List<Item> findByName(String name);\n"
                            + "@jakarta.ws.rs.Path(\"/same\") List<Item> findBySecret(String secret);").compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should reject @RestRepository on a type that is not a Jakarta Data repository")
        void shouldRejectMissingRepositoryAnnotation(@TempDir Path directory) {
            var result = new CompilationHarness(directory)
                    .source("demo.Item", ENTITY)
                    .source("demo.Plain", "package demo;\nimport org.eclipse.jnosql.rest.*;\n"
                            + "@RestRepository(path = \"/p\")\npublic interface Plain { }\n")
                    .compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should fail at build time when DTO mode is used without MapStruct")
        void shouldFailWithoutMapStruct(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")\n@RestDTO", "")
                    .withoutMapStruct().compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should fail at build time when the MapStruct processor does not run")
        void shouldFailWithoutMapStructProcessor(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")\n@RestDTO", "")
                    .withoutMapStructProcessor().compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).anyMatch(message -> message.contains("MapStruct"));
        }

        @Test
        @DisplayName("Should reject a mutating method that takes a Restriction, even when exposed")
        void shouldRejectMutatingRestriction(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")",
                    "@jakarta.ws.rs.Path(\"/purge\") @Delete void deleteMatching(jakarta.data.restrict.Restriction<Item> restriction);")
                    .compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should reject a Restriction of another entity or combined with other parameters")
        void shouldRejectMalformedSearch(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")",
                    "@jakarta.ws.rs.Path(\"/search\") @Find List<Item> search(jakarta.data.restrict.Restriction<Item> restriction, "
                            + "String extra);")
                    .compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should let @RestExclude win over @Path on a search")
        void shouldExcludeSearch(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")",
                    "@jakarta.ws.rs.Path(\"/search\") @RestExclude @Find "
                            + "List<Item> search(jakarta.data.restrict.Restriction<Item> r);")
                    .compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("search");
        }

        @Test
        @DisplayName("Should not expose a search under RestExposure.NONE even with @Path")
        void shouldIgnoreSearchUnderNone(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\", exposure = RestExposure.NONE)",
                    "@jakarta.ws.rs.Path(\"/search\") @Find "
                            + "List<Item> search(jakarta.data.restrict.Restriction<Item> r);").compile();

            assertThat(result.success()).isTrue();
            assertThat(result.generated("ItemRepositoryResource")).isFalse();
        }
    }

    @Nested
    @DisplayName("When a method takes a dynamic Restriction")
    class WhenSearching {

        private static final String SEARCH = "@Find List<Item> search(jakarta.data.restrict.Restriction<Item> r);";

        @Test
        @DisplayName("Should generate a QUERY endpoint and request only with @Path")
        void shouldGenerateWithExplicitExposure(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")", "@jakarta.ws.rs.Path(\"/search\") "
                    + SEARCH).compile();

            assertThat(result.success()).isTrue();
            assertThat(result.generated("ItemRepositorySearchRequest")).isTrue();
            assertThat(result.source("ItemRepositoryResource"))
                    .contains("@QUERY")
                    .contains("@Path(\"/search\")")
                    .contains("repository.search(request.toRestriction())");
        }

        @Test
        @DisplayName("Should not generate any search endpoint without @Path")
        void shouldNotGenerateWithoutOptIn(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")", SEARCH).compile();

            assertThat(result.success()).isTrue();
            assertThat(result.generated("ItemRepositorySearchRequest")).isFalse();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("@QUERY");
        }

        @Test
        @DisplayName("Should keep the search with EXPLICIT exposure when annotated")
        void shouldGenerateUnderExplicit(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\", exposure = RestExposure.EXPLICIT)",
                    "@jakarta.ws.rs.Path(\"/search\") " + SEARCH).compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource")).contains("@QUERY");
        }

        @Test
        @DisplayName("Should suppress the search with @RestExclude")
        void shouldSuppressWithExclude(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")", "@RestExclude " + SEARCH)
                    .compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("@QUERY");
        }

        @Test
        @DisplayName("Should not filter by attributes excluded from the DTO")
        void shouldNotSearchExcludedAttributes(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")\n@RestDTO(exclude = {\"secret\"})",
                    "@jakarta.ws.rs.Path(\"/search\") " + SEARCH).compile();

            assertThat(result.success()).isTrue();
            assertThat(result.source("ItemRepositorySearchRequest")).doesNotContain("secret");
        }

        @Test
        @DisplayName("Should compile the search without OpenAPI or MapStruct")
        void shouldCompileWithoutOptionalDependencies(@TempDir Path directory) {
            var result = harness(directory, "@RestRepository(path = \"/items\")", "@jakarta.ws.rs.Path(\"/search\") " + SEARCH)
                    .withoutOpenApi().withoutMapStruct().compile();

            assertThat(result.errors()).isEmpty();
            assertThat(result.source("ItemRepositoryResource")).doesNotContain("openapi");
        }
    }
}
