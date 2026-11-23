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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Establishes, with the real MapStruct processor and default configuration, which member visibilities MapStruct
 * maps. A mapping only counts when the compiled mapper copies the value: an empty implementation is not a mapping.
 * The REST processor is not involved; it must stay compatible with these results.
 */
@DisplayName("MapStruct visibility matrix (MapStruct only)")
class MapStructVisibilityTest {

    private static final String ENTITY = """
            package demo;
            public class Product {
                %s String name;
                %s
                %s
            }
            """;

    private static final String DTO = """
            package demo;
            public class ProductDto {
                private String name;
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
            }
            """;

    private static final String MAPPER = """
            package %s;
            import demo.*;
            @org.mapstruct.Mapper
            public interface ProductMapper {
                ProductDto toDto(Product product);
                Product toEntity(ProductDto dto);
            }
            """;

    private static String getter(String visibility) {
        return "absent".equals(visibility) ? ""
                : visibility("package".equals(visibility) ? "" : visibility) + "String getName() { return name; }";
    }

    private static String setter(String visibility) {
        return "absent".equals(visibility) ? ""
                : visibility("package".equals(visibility) ? "" : visibility)
                + "void setName(String name) { this.name = name; }";
    }

    private static String visibility(String modifier) {
        return modifier.isEmpty() ? "" : modifier + " ";
    }

    /** Returns "toDto=copied|empty toEntity=copied|empty", or "compile-error". */
    static String outcome(Path directory, String field, String getter, String setter, String mapperPackage)
            throws Exception {
        var entity = ENTITY.formatted(visibility("package".equals(field) ? "" : field), getter(getter),
                setter(setter));
        var result = new CompilationHarness(directory).source("demo.Product", entity)
                .source("demo.ProductDto", DTO)
                .source(mapperPackage + ".ProductMapper", MAPPER.formatted(mapperPackage))
                .withoutRestProcessor().compile();
        if (!result.success()) {
            return "compile-error";
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[] {result.classes().toUri().toURL()},
                MapStructVisibilityTest.class.getClassLoader())) {
            Object mapper = loader.loadClass(mapperPackage + ".ProductMapperImpl").getConstructor().newInstance();
            Class<?> entityType = loader.loadClass("demo.Product");
            Class<?> dtoType = loader.loadClass("demo.ProductDto");

            Object dto = dtoType.getConstructor().newInstance();
            dtoType.getMethod("setName", String.class).invoke(dto, "Keyboard");
            Object mapped = mapper.getClass().getMethod("toEntity", dtoType).invoke(mapper, dto);
            boolean toEntity = "Keyboard".equals(read(mapped, entityType));

            Object source = entityType.getConstructor().newInstance();
            write(source, entityType);
            Object response = mapper.getClass().getMethod("toDto", entityType).invoke(mapper, source);
            boolean toDto = "Keyboard".equals(dtoType.getMethod("getName").invoke(response));
            return "toDto=" + (toDto ? "copied" : "empty") + " toEntity=" + (toEntity ? "copied" : "empty");
        }
    }

    private static Object read(Object entity, Class<?> type) throws Exception {
        var field = type.getDeclaredField("name");
        field.setAccessible(true);
        return field.get(entity);
    }

    private static void write(Object entity, Class<?> type) throws Exception {
        var field = type.getDeclaredField("name");
        field.setAccessible(true);
        field.set(entity, "Keyboard");
    }

    @ParameterizedTest(name = "{0}: field={1} getter={2} setter={3} mapper in {4}")
    @DisplayName("default MapStruct property discovery")
    @CsvSource(delimiter = '|', textBlock = """
            A|private|public|public|demo|toDto=copied toEntity=copied
            A|private|public|public|other|toDto=copied toEntity=copied
            B|private|package|package|demo|toDto=empty toEntity=empty
            B|private|package|package|other|toDto=empty toEntity=empty
            C|private|protected|protected|demo|toDto=empty toEntity=empty
            C|private|protected|protected|other|toDto=empty toEntity=empty
            D|private|private|private|demo|toDto=empty toEntity=empty
            D|private|private|private|other|toDto=empty toEntity=empty
            E|public|absent|absent|demo|toDto=copied toEntity=copied
            E|public|absent|absent|other|toDto=copied toEntity=copied
            F|package|absent|absent|demo|toDto=empty toEntity=empty
            F|package|absent|absent|other|toDto=empty toEntity=empty
            G|protected|absent|absent|demo|toDto=empty toEntity=empty
            G|protected|absent|absent|other|toDto=empty toEntity=empty
            H|private|public|package|demo|toDto=copied toEntity=empty
            H|private|public|package|other|toDto=copied toEntity=empty
            I|private|package|public|demo|toDto=empty toEntity=copied
            I|private|package|public|other|toDto=empty toEntity=copied
            """)
    void shouldMatchMapStructDefaults(String id, String field, String getter, String setter, String mapperPackage,
                                      String expected, @TempDir Path directory) throws Exception {
        var actual = outcome(directory, field, getter, setter, mapperPackage);
        assertThat(actual).isEqualTo(expected);
    }
}
