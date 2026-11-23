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

@DisplayName("OpenAPI schemas of generated resources")
class OpenApiSchemaTest {

    private static final String ITEM = """
            package demo;
            import jakarta.nosql.*;
            @Entity public class Item {
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

    private static CompilationHarness harness(Path directory, String annotations, String body) {
        return new CompilationHarness(directory).source("demo.Item", ITEM).source("demo.ItemRepository", """
                package demo;
                import jakarta.data.page.*;
                import jakarta.data.repository.*;
                import org.eclipse.jnosql.rest.*;
                import java.util.*;
                @Repository @RestRepository(path = "/items") %s
                public interface ItemRepository { %s }
                """.formatted(annotations, body));
    }

    @Test
    @DisplayName("Should type the pagination parameters in the OpenAPI schema")
    void shouldTypePaginationParameters(@TempDir Path directory) {
        var result = harness(directory, "", "@Find Page<Item> findAll(PageRequest request);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("schema = @Schema(type = SchemaType.INTEGER, minimum = \"0\", defaultValue = \"0\")")
                .contains("schema = @Schema(type = SchemaType.INTEGER, minimum = \"1\", maximum = \"100\", "
                        + "defaultValue = \"20\")")
                .contains("schema = @Schema(type = SchemaType.BOOLEAN, defaultValue = \"false\")")
                .contains("@QueryParam(\"offset\") String offset");
    }

    @Test
    @DisplayName("Should not reference OpenAPI when it is not on the classpath")
    void shouldStayOptional(@TempDir Path directory) {
        var result = harness(directory, "", "@Insert Item insert(Item item);\n"
                + "@Find Page<Item> findAll(PageRequest request);").withoutOpenApi().compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("openapi").contains("Response.Status.CREATED");
    }

    @Test
    @DisplayName("Should document the created entity when no DTO is generated")
    void shouldDocumentEntity(@TempDir Path directory) {
        var result = harness(directory, "", "@Insert Item insert(Item item);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("@APIResponse(responseCode = \"201\", description = \"Created\"")
                .contains("mediaType = \"application/json\"")
                .contains("@Schema(implementation = Item.class)")
                .contains("Response.Status.CREATED");
    }

    @Test
    @DisplayName("Should reference the generated response DTO, not the entity, and honour exclusions")
    void shouldDocumentDto(@TempDir Path directory) {
        var result = harness(directory, "@RestDTO(exclude = {\"secret\"})", "@Insert Item insert(Item item);")
                .compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("@Schema(implementation = ItemDTO.class)")
                .doesNotContain("implementation = Item.class");
        assertThat(result.source("ItemDTO")).doesNotContain("secret");
    }

    @Test
    @DisplayName("Should add the DTO schema to a 201 response declared without content")
    void shouldCompleteDeclaredCreated(@TempDir Path directory) {
        var result = harness(directory, "@RestDTO(exclude = {\"secret\"})", "@Insert "
                + "@org.eclipse.microprofile.openapi.annotations.responses.APIResponse(responseCode = \"201\", "
                + "description = \"Item created\") Item insert(Item item);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource"))
                .contains("description = \"Item created\", content = @Content(mediaType = \"application/json\"")
                .contains("@Schema(implementation = ItemDTO.class)");
        assertThat(result.source("ItemRepositoryResource").split("responseCode = \"201\"", -1)).hasSize(2);
    }

    @Test
    @DisplayName("Should respect a response declared by the developer")
    void shouldKeepDeclaredResponse(@TempDir Path directory) {
        var result = harness(directory, "", "@Insert @org.eclipse.microprofile.openapi.annotations.responses."
                + "APIResponse(responseCode = \"200\", description = \"Mine\") Item insert(Item item);").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("Mine").doesNotContain("\"Created\"");
    }
}
