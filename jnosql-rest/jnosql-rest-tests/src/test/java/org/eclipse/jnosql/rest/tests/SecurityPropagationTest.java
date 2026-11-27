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

@DisplayName("Propagation of Jakarta authorization annotations")
class SecurityPropagationTest {

    private static final String ENTITY = """
            package demo;
            import jakarta.nosql.*;
            @Entity public class Item {
                @Id private Long id;
                @Column private String name;
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """;

    private static CompilationHarness harness(Path directory, String typeAnnotations, String supertype, String body) {
        return new CompilationHarness(directory).source("demo.Item", ENTITY).source("demo.ItemRepository", """
                package demo;
                import jakarta.annotation.security.*;
                import jakarta.data.page.*;
                import jakarta.data.repository.*;
                import org.eclipse.jnosql.rest.*;
                import java.util.*;
                @Repository
                @RestRepository(path = "/items")
                %s
                public interface ItemRepository %s {
                    %s
                }
                """.formatted(typeAnnotations, supertype, body));
    }

    private static String method(String source, String name) {
        int end = source.indexOf(" " + name + "(");
        int start = source.lastIndexOf("    /**", end);
        return source.substring(start, end);
    }

    @Test
    @DisplayName("Should generate no security metadata when the annotations are unused")
    void shouldGenerateNothingWhenUnused(@TempDir Path directory) {
        var result = harness(directory, "", "extends CrudRepository<Item, Long>", "").compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("RolesAllowed")
                .doesNotContain("PermitAll").doesNotContain("DenyAll").doesNotContain("jakarta.annotation.security");
    }

    @Test
    @DisplayName("Should compile without the security API when the annotations are unused")
    void shouldCompileWithoutSecurityApi(@TempDir Path directory) {
        var result = new CompilationHarness(directory).withoutSecurity().source("demo.Item", ENTITY)
                .source("demo.ItemRepository", """
                        package demo;
                        import jakarta.data.repository.*;
                        import org.eclipse.jnosql.rest.*;
                        @Repository
                        @RestRepository(path = "/items")
                        public interface ItemRepository extends CrudRepository<Item, Long> {
                        }
                        """).compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.success()).isTrue();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("jakarta.annotation.security");
    }

    @Test
    @DisplayName("Should copy repository-level RolesAllowed to the resource")
    void shouldCopyRepositoryRolesAllowed(@TempDir Path directory) {
        var result = harness(directory, "@RolesAllowed(\"USER\")", "extends CrudRepository<Item, Long>", "")
                .compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.success()).isTrue();
        var source = result.source("ItemRepositoryResource");
        assertThat(source).contains("import jakarta.annotation.security.RolesAllowed;")
                .containsPattern("@RolesAllowed\\(\"USER\"\\)\\s+public class ItemRepositoryResource");
        assertThat(source).doesNotContain("PermitAll").doesNotContain("DenyAll");
    }

    @Test
    @DisplayName("Should copy repository-level PermitAll and DenyAll to the resource")
    void shouldCopyRepositoryPermitAllAndDenyAll(@TempDir Path directory) {
        var permit = harness(directory.resolve("a"), "@PermitAll", "extends CrudRepository<Item, Long>", "")
                .compile();
        var deny = harness(directory.resolve("b"), "@DenyAll", "extends CrudRepository<Item, Long>", "").compile();

        assertThat(permit.success()).isTrue();
        assertThat(permit.source("ItemRepositoryResource")).containsPattern("@PermitAll\\s+public class");
        assertThat(deny.success()).isTrue();
        assertThat(deny.source("ItemRepositoryResource")).containsPattern("@DenyAll\\s+public class");
    }

    @Test
    @DisplayName("Should copy method-level annotations next to class-level ones")
    void shouldCopyMethodLevelAnnotations(@TempDir Path directory) {
        var result = harness(directory, "@RolesAllowed(\"USER\")", "", """
                @Find @PermitAll List<Item> findAll();
                @Find @Path("/secret") @DenyAll List<Item> findByName(String name);
                @Insert @RolesAllowed({"ADMIN", "MANAGER"}) Item insert(Item item);
                """.replace("@Path", "@jakarta.ws.rs.Path")).compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.success()).isTrue();
        var source = result.source("ItemRepositoryResource");
        assertThat(source).containsPattern("@RolesAllowed\\(\"USER\"\\)\\s+public class");
        assertThat(method(source, "findAll")).contains("@PermitAll").doesNotContain("@RolesAllowed")
                .doesNotContain("@DenyAll");
        assertThat(method(source, "findByName")).contains("@DenyAll").doesNotContain("@PermitAll");
        assertThat(method(source, "insert")).contains("@POST").contains("@RolesAllowed({\"ADMIN\", \"MANAGER\"})");
    }

    @Test
    @DisplayName("Should keep inherited standard operations free of method-level security")
    void shouldKeepInheritedMethodsPlain(@TempDir Path directory) {
        var result = harness(directory, "@RolesAllowed(\"USER\")", "extends CrudRepository<Item, Long>", "")
                .compile();

        var source = result.source("ItemRepositoryResource");
        assertThat(source.split("@RolesAllowed", -1)).hasSize(2);
        assertThat(source).contains("public ").contains("findById");
    }

    @Test
    @DisplayName("Should copy security from a redeclared standard method without duplicating the endpoint")
    void shouldCopyRedeclaredMethodSecurity(@TempDir Path directory) {
        var result = harness(directory, "@RolesAllowed(\"USER\")", "extends CrudRepository<Item, Long>", """
                @Override @DenyAll void deleteById(Long id);
                @Override @PermitAll Optional<Item> findById(Long id);
                """).compile();

        assertThat(result.errors()).isEmpty();
        assertThat(result.success()).isTrue();
        var source = result.source("ItemRepositoryResource");
        assertThat(method(source, "deleteById")).contains("@DELETE").contains("@DenyAll");
        assertThat(method(source, "findById")).contains("@GET").contains("@PermitAll");
        assertThat(source.split(" deleteById\\(", -1)).hasSize(2);
        assertThat(source.split(" findById\\(", -1)).hasSize(2);
    }

    @Test
    @DisplayName("Should reject conflicting annotations on a method")
    void shouldRejectConflictOnMethod(@TempDir Path directory) {
        var result = harness(directory, "", "", "@Find @PermitAll @DenyAll List<Item> findAll();").compile();

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("Conflicting authorization annotations")
                && error.contains("@PermitAll") && error.contains("@DenyAll"));
        assertThat(result.generated("ItemRepositoryResource")).isFalse();
    }

    @Test
    @DisplayName("Should reject conflicting annotations on a repository")
    void shouldRejectConflictOnRepository(@TempDir Path directory) {
        var result = harness(directory, "@RolesAllowed(\"USER\") @DenyAll", "extends CrudRepository<Item, Long>", "")
                .compile();

        assertThat(result.success()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("Conflicting authorization annotations")
                && error.contains("@RolesAllowed") && error.contains("@DenyAll"));
    }
}
