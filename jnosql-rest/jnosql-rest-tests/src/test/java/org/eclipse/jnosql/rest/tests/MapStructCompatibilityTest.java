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

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compiles repositories whose entities use different construction patterns with the real REST and MapStruct
 * processors, then runs the compiled mapper. MapStruct alone decides whether a model can be mapped.
 */
@DisplayName("MapStruct compatibility of generated mappers")
class MapStructCompatibilityTest {

    private static final String HEAD = """
            package demo;
            import jakarta.data.repository.*;
            import jakarta.nosql.*;
            import org.eclipse.jnosql.rest.*;
            import java.math.BigDecimal;
            import java.util.*;
            """;

    private static final String REPOSITORY = HEAD + """
            @Repository @RestRepository(path = "/products") %s
            public interface ProductRepository {
                @Insert Product insert(Product product);
                @Find Optional<Product> findById(@By(ID) Long id);
            }
            """;

    private static final String MANUFACTURER = HEAD + """
            @Embeddable
            public record Manufacturer(String name, String country) { }
            """;

    private static final String BEAN = HEAD + """
            @Entity public class Product {
                @Id private Long id;
                @Column private String name;
                @Column private BigDecimal price;
                @Column private String internalNotes;
                @Column private Manufacturer manufacturer;
                public Product() { }
                public Long getId() { return id; }
                public void setId(Long id) { this.id = id; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public BigDecimal getPrice() { return price; }
                public void setPrice(BigDecimal price) { this.price = price; }
                public String getInternalNotes() { return internalNotes; }
                public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
                public Manufacturer getManufacturer() { return manufacturer; }
                public void setManufacturer(Manufacturer manufacturer) { this.manufacturer = manufacturer; }
            }
            """;

    private static final String IMMUTABLE = HEAD + """
            @Entity public class Product {
                @Id private final Long id;
                @Column private final String name;
                @Column private final BigDecimal price;
                public Product(Long id, String name, BigDecimal price) {
                    this.id = id; this.name = name; this.price = price;
                }
                public Long getId() { return id; }
                public String getName() { return name; }
                public BigDecimal getPrice() { return price; }
            }
            """;

    private static final String RECORD = HEAD + """
            @Entity public record Product(@Id Long id, @Column String name, @Column BigDecimal price,
                                          @Column Manufacturer manufacturer) { }
            """;

    private static final String BUILDER = HEAD + """
            @Entity public class Product {
                @Id private final Long id;
                @Column private final String name;
                @Column private final BigDecimal price;
                private Product(Builder builder) { id = builder.id; name = builder.name; price = builder.price; }
                public Long getId() { return id; }
                public String getName() { return name; }
                public BigDecimal getPrice() { return price; }
                public static Builder builder() { return new Builder(); }
                public static class Builder {
                    private Long id; private String name; private BigDecimal price;
                    public Builder id(Long id) { this.id = id; return this; }
                    public Builder name(String name) { this.name = name; return this; }
                    public Builder price(BigDecimal price) { this.price = price; return this; }
                    public Product build() { return new Product(this); }
                }
            }
            """;

    private static final String CUSTOM_BUILDER = BUILDER.replace("builder()", "newBuilder()")
            .replace("public Product build()", "public Product create()");

    private static final String STATIC_FACTORY = HEAD + """
            @Entity public class Product {
                @Id private final Long id;
                @Column private final String name;
                @Column private final BigDecimal price;
                private Product(Long id, String name, BigDecimal price) {
                    this.id = id; this.name = name; this.price = price;
                }
                public static Product of(Long id, String name, BigDecimal price) {
                    return new Product(id, name, price);
                }
                public Long getId() { return id; }
                public String getName() { return name; }
                public BigDecimal getPrice() { return price; }
            }
            """;

    private static CompilationHarness harness(Path directory, String entity, String annotations) {
        return new CompilationHarness(directory).source("demo.Product", entity)
                .source("demo.Manufacturer", MANUFACTURER)
                .source("demo.ProductRepository", REPOSITORY.formatted(annotations).replace("ID", "\"id\""));
    }

    private static Object instantiate(ClassLoader loader, String type, Map<String, Object> values) throws Exception {
        Class<?> record = loader.loadClass(type);
        RecordComponent[] components = record.getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        for (int index = 0; index < components.length; index++) {
            types[index] = components[index].getType();
            arguments[index] = values.get(components[index].getName());
        }
        return record.getDeclaredConstructor(types).newInstance(arguments);
    }

    private static Object read(Object target, String name) throws Exception {
        try {
            return target.getClass().getMethod(name).invoke(target);
        } catch (NoSuchMethodException exception) {
            return target.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1))
                    .invoke(target);
        }
    }

    private static Object call(Object mapper, String method, Object argument) throws Exception {
        for (var candidate : mapper.getClass().getMethods()) {
            if (candidate.getName().equals(method) && candidate.getParameterCount() == 1
                    && candidate.getParameterTypes()[0].isInstance(argument)
                    && !candidate.getParameterTypes()[0].equals(java.util.List.class)) {
                return candidate.invoke(mapper, argument);
            }
        }
        throw new NoSuchMethodException(method);
    }

    private static URLClassLoader loader(CompilationHarness.Result result) throws Exception {
        return new URLClassLoader(new URL[] {result.classes().toUri().toURL()},
                MapStructCompatibilityTest.class.getClassLoader());
    }

    /** Maps a request to an entity and back, returning the response DTO. */
    private static Object roundTrip(CompilationHarness.Result result, Map<String, Object> values) throws Exception {
        try (URLClassLoader loader = loader(result)) {
            Object mapper = loader.loadClass("demo.ProductDTOMapperImpl").getConstructor().newInstance();
            Object request = instantiate(loader, "demo.ProductCreateRequest", values);
            Object entity = call(mapper, "toEntity", request);
            assertThat(entity).isNotNull();
            return call(mapper, "toDTO", entity);
        }
    }

    private static final Map<String, Object> VALUES = Map.of("id", 7L, "name", "Keyboard",
            "price", new BigDecimal("19.90"));

    @Nested
    @DisplayName("Supported by MapStruct")
    class Supported {

        @Test
        @DisplayName("Case A: mutable JavaBean with a nested record and exclusions")
        void shouldMapJavaBean(@TempDir Path directory) throws Exception {
            var result = harness(directory, BEAN, "@RestDTO(exclude = {\"internalNotes\"})").compile();

            assertThat(result.errors()).isEmpty();
            try (URLClassLoader loader = loader(result)) {
                Object mapper = loader.loadClass("demo.ProductDTOMapperImpl").getConstructor().newInstance();
                Object manufacturer = instantiate(loader, "demo.ManufacturerDTO",
                        Map.of("name", "Acme", "country", "BR"));
                Object request = instantiate(loader, "demo.ProductCreateRequest", Map.of("name", "Keyboard",
                        "price", new BigDecimal("19.90"), "manufacturer", manufacturer));
                Object entity = call(mapper, "toEntity", request);
                Object dto = call(mapper, "toDTO", entity);

                assertThat(read(entity, "name")).isEqualTo("Keyboard");
                assertThat(result.source("ProductDTO")).doesNotContain("internalNotes");
                Object nested = read(entity, "manufacturer");
                assertThat(nested.getClass().getName()).isEqualTo("demo.Manufacturer");
                assertThat(read(nested, "country")).isEqualTo("BR");
                assertThat(read(dto, "price")).isEqualTo(new BigDecimal("19.90"));
                assertThat(read(read(dto, "manufacturer"), "name")).isEqualTo("Acme");
                assertThat(dto.getClass().getRecordComponents()).extracting(RecordComponent::getName)
                        .doesNotContain("internalNotes");
            }
        }

        @Test
        @DisplayName("Case H: excluded state is neither exposed nor copied from the entity")
        void shouldNotLeakExcludedState(@TempDir Path directory) throws Exception {
            var result = harness(directory, BEAN, "@RestDTO(exclude = {\"internalNotes\"})").compile();

            assertThat(result.errors()).isEmpty();
            try (URLClassLoader loader = loader(result)) {
                Object mapper = loader.loadClass("demo.ProductDTOMapperImpl").getConstructor().newInstance();
                Object entity = loader.loadClass("demo.Product").getConstructor().newInstance();
                entity.getClass().getMethod("setName", String.class).invoke(entity, "Mouse");
                entity.getClass().getMethod("setInternalNotes", String.class).invoke(entity, "margin 40%");
                Object dto = call(mapper, "toDTO", entity);

                assertThat(read(dto, "name")).isEqualTo("Mouse");
                assertThat(dto.toString()).doesNotContain("margin");
            }
        }

        @Test
        @DisplayName("Case B: constructor-based immutable class")
        void shouldMapConstructor(@TempDir Path directory) throws Exception {
            var result = harness(directory, IMMUTABLE, "@RestDTO").compile();

            assertThat(result.errors()).isEmpty();
            var dto = roundTrip(result, VALUES);
            assertThat(read(dto, "name")).isEqualTo("Keyboard");
            assertThat(read(dto, "id")).isEqualTo(7L);
        }

        @Test
        @DisplayName("Case C: Java record entity with a nested record")
        void shouldMapRecord(@TempDir Path directory) throws Exception {
            var result = harness(directory, RECORD, "@RestDTO").compile();

            assertThat(result.errors()).isEmpty();
            var dto = roundTrip(result, VALUES);
            assertThat(read(dto, "price")).isEqualTo(new BigDecimal("19.90"));
        }

        @Test
        @DisplayName("Case D: conventional builder without a no-argument constructor")
        void shouldMapBuilder(@TempDir Path directory) throws Exception {
            var result = harness(directory, BUILDER, "@RestDTO").compile();

            assertThat(result.errors()).isEmpty();
            var dto = roundTrip(result, VALUES);
            assertThat(read(dto, "name")).isEqualTo("Keyboard");
        }

        @Test
        @DisplayName("Case F: builder with unconventional method names")
        void shouldMapCustomBuilder(@TempDir Path directory) throws Exception {
            var result = harness(directory, CUSTOM_BUILDER, "@RestDTO").compile();

            assertThat(result.errors()).isEmpty();
            var dto = roundTrip(result, VALUES);
            assertThat(read(dto, "name")).isEqualTo("Keyboard");
        }
    }

    @Nested
    @DisplayName("Delegated to MapStruct")
    class Delegated {

        @Test
        @DisplayName("Case E: a static factory is reported by MapStruct, not by the REST processor")
        void shouldReportStaticFactory(@TempDir Path directory) {
            var result = harness(directory, STATIC_FACTORY, "@RestDTO").compile();

            assertThat(result.success()).isFalse();
            assertThat(result.errors()).isNotEmpty();
            assertThat(String.join(" ", result.errors())).contains("constructor");
        }
    }
}
