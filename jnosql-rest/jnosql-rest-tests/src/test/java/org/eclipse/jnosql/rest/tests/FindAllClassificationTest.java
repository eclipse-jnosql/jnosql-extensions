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

@DisplayName("Classification of findAll methods")
class FindAllClassificationTest {

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

    private static CompilationHarness.Result compile(Path directory, String supertype, String body) {
        return new CompilationHarness(directory).source("demo.Item", ENTITY).source("demo.ItemRepository", """
                package demo;
                import jakarta.data.Order;
                import jakarta.data.page.*;
                import jakarta.data.repository.*;
                import jakarta.ws.rs.Path;
                import org.eclipse.jnosql.rest.*;
                import java.util.*;
                import java.util.stream.Stream;
                @Repository
                @RestRepository(path = "/items")
                public interface ItemRepository %s {
                    %s
                }
                """.formatted(supertype, body)).compile();
    }

    @Test
    @DisplayName("Should keep the inherited unbounded findAll unsupported")
    void shouldKeepInheritedUnboundedFindAllUnsupported(@TempDir Path directory) {
        var result = compile(directory, "extends BasicRepository<Item, Long>", "");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("repository.findAll()")
                .contains("repository.findAll(pageRequest, order)");
        assertThat(result.notes()).anyMatch(note -> note.contains("findAll")
                && note.contains("unbounded"));
    }

    @Test
    @DisplayName("Should keep the unbounded findAll redeclared from a standard repository unsupported")
    void shouldKeepRedeclaredStandardFindAllUnsupported(@TempDir Path directory) {
        var result = compile(directory, "extends BasicRepository<Item, Long>", "@Override Stream<Item> findAll();");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).doesNotContain("repository.findAll()");
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("findAll")
                && warning.contains("unbounded"));
    }

    @Test
    @DisplayName("Should route an annotated custom findAll as a custom method")
    void shouldRouteAnnotatedCustomFindAll(@TempDir Path directory) {
        var result = compile(directory, "", "@Find List<Item> findAll();");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/findAll\")")
                .contains("repository.findAll()");
    }

    @Test
    @DisplayName("Should let an explicit @Path override the inferred findAll route")
    void shouldOverrideRouteWithPath(@TempDir Path directory) {
        var result = compile(directory, "", "@Find @Path(\"/all\") List<Item> findAll();");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/all\")")
                .doesNotContain("\"/findAll\"").contains("repository.findAll()");
    }

    @Test
    @DisplayName("Should route an annotated custom paginated findAll as a custom method")
    void shouldRouteAnnotatedPaginatedFindAll(@TempDir Path directory) {
        var result = compile(directory, "", "@Find Page<Item> findAll(PageRequest pageRequest);");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/findAll\")")
                .contains("repository.findAll(");
    }

    @Test
    @DisplayName("Should route an unannotated custom findAll, which JNoSQL resolves by name, as a custom method")
    void shouldRouteUnannotatedCustomFindAll(@TempDir Path directory) {
        var result = compile(directory, "", "List<Item> findAll();");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("@Path(\"/findAll\")")
                .contains("repository.findAll()");
    }

    @Test
    @DisplayName("Should keep the inherited paginated findAll on the collection root beside an annotated override")
    void shouldKeepPaginatedRootBesideAnnotatedOverride(@TempDir Path directory) {
        var result = compile(directory, "extends CrudRepository<Item, Long>", "@Find @Path(\"/all\") Stream<Item> findAll();");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("repository.findAll(pageRequest, order)")
                .contains("@Path(\"/all\")");
    }

    @Test
    @DisplayName("Should serve a custom findAll with an empty @Path at the collection root")
    void shouldServeCustomFindAllAtRoot(@TempDir Path directory) {
        var result = compile(directory, "", "@Find @Path(\"\") List<Item> findAll();");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("repository.findAll()")
                .doesNotContain("\"/findAll\"").doesNotContain("@Path(\"\")");
    }

    @Test
    @DisplayName("Should serve a custom findAll with an empty @Path at the root of a DataRepository")
    void shouldServeCustomFindAllAtRootOfDataRepository(@TempDir Path directory) {
        var result = compile(directory, "extends DataRepository<Item, Long>", "@Find @Path(\"\") List<Item> findAll();\n"
                + "@Find @Path(\"/paged\") Page<Item> findAll(PageRequest pageRequest);");

        assertThat(result.errors()).isEmpty();
        assertThat(result.source("ItemRepositoryResource")).contains("repository.findAll()")
                .contains("@Path(\"/paged\")");
    }

    @Test
    @DisplayName("Should report a collision between a root findAll and the inherited paginated root")
    void shouldReportRootCollision(@TempDir Path directory) {
        var result = compile(directory, "extends CrudRepository<Item, Long>",
                "@Find @Path(\"\") Stream<Item> findAll();");

        assertThat(result.errors()).anyMatch(error -> error.contains("Both"));
    }
}
