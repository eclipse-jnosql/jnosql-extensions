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

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the REST processor accepts exactly the member visibilities that MapStruct maps, as established by
 * {@link MapStructVisibilityTest}, and explains the rest instead of generating an empty mapper.
 */
@DisplayName("DTO property discovery follows MapStruct visibility")
class VisibilityRegressionTest {

    private static final String REPOSITORY = """
            package demo;
            import jakarta.data.repository.*;
            import org.eclipse.jnosql.rest.*;
            import java.util.*;
            @Repository @RestRepository(path = "/products") @RestDTO
            public interface ProductRepository {
                @Insert Product insert(Product product);
                @Find Optional<Product> findById(@By("id") String id);
            }
            """;

    private static final String PACKAGE_PRIVATE = """
            package demo;
            import jakarta.nosql.*;
            @Entity public class Product {
                @Id private String id;
                @Column private String name;
                String getId() { return id; }
                String getName() { return name; }
                void setName(String name) { this.name = name; }
            }
            """;

    private static final String PUBLIC_FIELDS = """
            package demo;
            import jakarta.nosql.*;
            @Entity public class Product {
                @Id public String id;
                @Column public String name;
            }
            """;

    private static final String PUBLIC_GETTERS_PACKAGE_SETTERS = """
            package demo;
            import jakarta.nosql.*;
            @Entity public class Product {
                @Id private String id;
                @Column private String name;
                public String getId() { return id; }
                public String getName() { return name; }
                void setName(String name) { this.name = name; }
            }
            """;

    private static CompilationHarness.Result compile(Path directory, String entity) {
        return new CompilationHarness(directory).source("demo.Product", entity)
                .source("demo.ProductRepository", REPOSITORY).compile();
    }

    @Test
    @DisplayName("package-private accessors are explained, not silently mapped to an empty DTO")
    void shouldExplainPackagePrivateAccessors(@TempDir Path directory) {
        var result = compile(directory, PACKAGE_PRIVATE);

        assertThat(result.success()).isFalse();
        assertThat(String.join(" ", result.errors())).contains("has no readable attributes", "public");
        assertThat(result.generated("ProductDTO")).isFalse();
    }

    @Test
    @DisplayName("public fields are mapped in both directions without accessors")
    void shouldMapPublicFields(@TempDir Path directory) throws Exception {
        var result = compile(directory, PUBLIC_FIELDS);

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {result.classes().toUri().toURL()},
                getClass().getClassLoader())) {
            Object mapper = loader.loadClass("demo.ProductDTOMapperImpl").getConstructor().newInstance();
            Class<?> request = loader.loadClass("demo.ProductCreateRequest");
            Class<?> entityType = loader.loadClass("demo.Product");
            Object entity = mapper.getClass().getMethod("toEntity", request).invoke(mapper,
                    request.getDeclaredConstructor(String.class, String.class).newInstance("1", "Keyboard"));
            assertThat(entityType.getField("name").get(entity)).isEqualTo("Keyboard");

            Object dto = mapper.getClass().getMethod("toDTO", entityType).invoke(mapper, entity);
            assertThat(dto.getClass().getMethod("name").invoke(dto)).isEqualTo("Keyboard");
        }
    }

    @Test
    @DisplayName("public getters with package-private setters map the response only")
    void shouldMapResponseWithPublicGetters(@TempDir Path directory) throws Exception {
        var result = compile(directory, PUBLIC_GETTERS_PACKAGE_SETTERS);

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {result.classes().toUri().toURL()},
                getClass().getClassLoader())) {
            Object mapper = loader.loadClass("demo.ProductDTOMapperImpl").getConstructor().newInstance();
            Class<?> entityType = loader.loadClass("demo.Product");
            Object entity = entityType.getConstructor().newInstance();
            var setter = entityType.getDeclaredMethod("setName", String.class);
            setter.setAccessible(true);
            setter.invoke(entity, "Mouse");

            Object dto = mapper.getClass().getMethod("toDTO", entityType).invoke(mapper, entity);
            assertThat(dto.getClass().getMethod("name").invoke(dto)).isEqualTo("Mouse");
        }
    }
}
