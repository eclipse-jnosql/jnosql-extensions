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

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("@RestDTO request and response exclusions")
class RestDtoExclusionTest {

    private static final String ENTITY = """
            package demo;
            import jakarta.nosql.*;
            import java.math.BigDecimal;
            @Entity public class Product {
                @Id private String id;
                @Column private String name;
                @Column private BigDecimal price;
                @Column private BigDecimal supplierCost;
                @Column private String internalNotes;
                public String getId() { return id; }
                public void setId(String id) { this.id = id; }
                public String getName() { return name; }
                public void setName(String name) { this.name = name; }
                public BigDecimal getPrice() { return price; }
                public void setPrice(BigDecimal price) { this.price = price; }
                public BigDecimal getSupplierCost() { return supplierCost; }
                public void setSupplierCost(BigDecimal supplierCost) { this.supplierCost = supplierCost; }
                public String getInternalNotes() { return internalNotes; }
                public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
            }
            """;

    private static final String REPOSITORY = """
            package demo;
            import jakarta.data.repository.*;
            import org.eclipse.jnosql.rest.*;
            import java.util.*;
            @Repository @RestRepository(path = "/products") %s
            public interface ProductRepository extends CrudRepository<Product, String> {
            }
            """;

    private static CompilationHarness.Result compile(Path directory, String annotation) {
        return new CompilationHarness(directory).source("demo.Product", ENTITY)
                .source("demo.ProductRepository", REPOSITORY.formatted(annotation)).compile();
    }

    private static List<String> components(URLClassLoader loader, String type) throws Exception {
        return Arrays.stream(loader.loadClass(type).getRecordComponents()).map(RecordComponent::getName).toList();
    }

    private static URLClassLoader loader(CompilationHarness.Result result) throws Exception {
        return new URLClassLoader(new URL[] {result.classes().toUri().toURL()}, RestDtoExclusionTest.class
                .getClassLoader());
    }

    @Test
    @DisplayName("exclude removes the attribute from the response and every request DTO")
    void shouldExcludeFromBothDirections(@TempDir Path directory) throws Exception {
        var result = compile(directory, "@RestDTO(exclude = {\"internalNotes\"})");

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = loader(result)) {
            assertThat(components(loader, "demo.ProductDTO")).contains("id", "supplierCost")
                    .doesNotContain("internalNotes");
            assertThat(components(loader, "demo.ProductCreateRequest")).contains("id", "supplierCost")
                    .doesNotContain("internalNotes");
            assertThat(components(loader, "demo.ProductUpdateRequest")).contains("supplierCost")
                    .doesNotContain("internalNotes");
        }
    }

    @Test
    @DisplayName("excludeFromRequest keeps the attribute in the response")
    void shouldExcludeFromRequestsOnly(@TempDir Path directory) throws Exception {
        var result = compile(directory, "@RestDTO(excludeFromRequest = {\"id\"})");

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = loader(result)) {
            assertThat(components(loader, "demo.ProductDTO")).contains("id");
            assertThat(components(loader, "demo.ProductCreateRequest")).doesNotContain("id").contains("name");
            assertThat(components(loader, "demo.ProductUpdateRequest")).doesNotContain("id");
        }
        assertThat(result.generated("ProductRepositoryResource")).isTrue();
        assertThat(result.source("ProductRepositoryResource")).contains("@PathParam(\"id\")");
    }

    @Test
    @DisplayName("excludeFromResponse still maps request values into the entity")
    void shouldExcludeFromResponsesOnly(@TempDir Path directory) throws Exception {
        var result = compile(directory, "@RestDTO(excludeFromResponse = {\"supplierCost\"})");

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = loader(result)) {
            assertThat(components(loader, "demo.ProductDTO")).doesNotContain("supplierCost").contains("name");
            assertThat(components(loader, "demo.ProductCreateRequest")).contains("supplierCost");
            assertThat(components(loader, "demo.ProductUpdateRequest")).contains("supplierCost");

            Object mapper = loader.loadClass("demo.ProductDTOMapperImpl").getConstructor().newInstance();
            Class<?> request = loader.loadClass("demo.ProductCreateRequest");
            Class<?> entityType = loader.loadClass("demo.Product");
            Object[] arguments = Arrays.stream(request.getRecordComponents()).map(component ->
                    switch (component.getName()) {
                        case "supplierCost" -> new BigDecimal("12.50");
                        case "name" -> "Keyboard";
                        default -> null;
                    }).toArray();
            Class<?>[] types = Arrays.stream(request.getRecordComponents()).map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);
            Object entity = mapper.getClass().getMethod("toEntity", request)
                    .invoke(mapper, request.getDeclaredConstructor(types).newInstance(arguments));

            assertThat(entityType.getMethod("getSupplierCost").invoke(entity)).isEqualTo(new BigDecimal("12.50"));

            Object dto = mapper.getClass().getMethod("toDTO", entityType).invoke(mapper, entity);
            assertThat(dto.toString()).contains("Keyboard").doesNotContain("12.50");
        }
    }

    @Test
    @DisplayName("combined exclusions apply the union per direction")
    void shouldCombineExclusions(@TempDir Path directory) throws Exception {
        var result = compile(directory, """
                @RestDTO(exclude = {"internalNotes", "internalNotes"}, excludeFromRequest = {"id", "internalNotes"},
                         excludeFromResponse = {"supplierCost"})""");

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = loader(result)) {
            assertThat(components(loader, "demo.ProductDTO")).containsExactly("id", "name", "price");
            assertThat(components(loader, "demo.ProductCreateRequest"))
                    .containsExactly("name", "price", "supplierCost");
            assertThat(components(loader, "demo.ProductUpdateRequest"))
                    .containsExactly("name", "price", "supplierCost");
        }
    }

    @Test
    @DisplayName("a bare @RestDTO keeps every eligible attribute")
    void shouldKeepEverythingByDefault(@TempDir Path directory) throws Exception {
        var result = compile(directory, "@RestDTO");

        assertThat(result.errors()).isEmpty();
        try (URLClassLoader loader = loader(result)) {
            assertThat(components(loader, "demo.ProductDTO"))
                    .containsExactly("id", "name", "price", "supplierCost", "internalNotes");
            assertThat(components(loader, "demo.ProductCreateRequest"))
                    .containsExactly("id", "name", "price", "supplierCost", "internalNotes");
        }
    }

    @Test
    @DisplayName("unknown names are rejected in every list")
    void shouldRejectUnknownNames(@TempDir Path directory) {
        for (String attribute : List.of("exclude", "excludeFromRequest", "excludeFromResponse")) {
            var result = compile(directory.resolve(attribute).toFile().toPath(),
                    "@RestDTO(" + attribute + " = {\"missing\"})");

            assertThat(result.success()).as(attribute).isFalse();
            assertThat(String.join(" ", result.errors())).as(attribute).contains("missing");
        }
    }

    @Test
    @DisplayName("excluding every attribute from requests is rejected")
    void shouldRejectEmptyRequests(@TempDir Path directory) {
        var result = compile(directory, "@RestDTO(excludeFromRequest = {\"id\", \"name\", \"price\", "
                + "\"supplierCost\", \"internalNotes\"})");

        assertThat(result.success()).isFalse();
        assertThat(String.join(" ", result.errors())).contains("empty create request");
    }
}
