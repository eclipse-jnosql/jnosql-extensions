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

import org.eclipse.jnosql.rest.tests.sample.CustomerRepositoryRepository;
import org.eclipse.jnosql.rest.tests.sample.ProductRepositoryRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MicroProfile OpenAPI propagation to generated resources")
class OpenApiPropagationTest {

    private Method findByCategory() {
        return Arrays.stream(ProductRepositoryRepository.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("findByCategory")).findFirst().orElseThrow();
    }

    @Nested
    @DisplayName("When the repository carries OpenAPI annotations")
    class WhenTheRepositoryIsAnnotated {

        @Test
        @DisplayName("Should copy the repository @Tag to the resource class")
        void shouldCopyTheTag() {
            Tag tag = ProductRepositoryRepository.class.getAnnotation(Tag.class);

            assertThat(tag).isNotNull();
            assertThat(tag.name()).isEqualTo("Products");
            assertThat(tag.description()).isEqualTo("Product catalog operations");
        }

        @Test
        @DisplayName("Should copy @Operation with its explicit values")
        void shouldCopyTheOperation() {
            Operation operation = findByCategory().getAnnotation(Operation.class);

            assertThat(operation.summary()).isEqualTo("Find products by category");
            assertThat(operation.description()).isEqualTo("Returns products belonging to a category");
        }

        @Test
        @DisplayName("Should copy @APIResponse with its explicit values")
        void shouldCopyTheResponse() {
            APIResponse response = findByCategory().getAnnotation(APIResponse.class);

            assertThat(response.responseCode()).isEqualTo("200");
            assertThat(response.description()).isEqualTo("Products retrieved successfully");
        }

        @Test
        @DisplayName("Should copy @Parameter metadata, including enum values, to the query parameter")
        void shouldCopyTheParameter() {
            Parameter parameter = findByCategory().getParameters()[0].getAnnotation(Parameter.class);

            assertThat(parameter.description()).isEqualTo("Category name");
            assertThat(parameter.in()).isEqualTo(ParameterIn.QUERY);
            assertThat(parameter.required()).isTrue();
        }
    }

    @Nested
    @DisplayName("When the repository carries no OpenAPI annotations")
    class WhenTheRepositoryIsNotAnnotated {

        @Test
        @DisplayName("Should not invent OpenAPI annotations")
        void shouldNotInventAnnotations() {
            assertThat(CustomerRepositoryRepository.class.getAnnotation(Tag.class)).isNull();
            assertThat(Arrays.stream(CustomerRepositoryRepository.class.getDeclaredMethods()))
                    .noneMatch(method -> method.isAnnotationPresent(Operation.class));
        }

        @Test
        @DisplayName("Should return the generated DTO type so MicroProfile OpenAPI can infer its schema")
        void shouldExposeDtoTypesInSignatures() throws Exception {
            Method method = Arrays.stream(CustomerRepositoryRepository.class.getDeclaredMethods())
                    .filter(candidate -> candidate.getName().equals("findById")).findFirst().orElseThrow();

            assertThat(method.getReturnType().getSimpleName()).isEqualTo("CustomerDTO");
        }
    }
}
