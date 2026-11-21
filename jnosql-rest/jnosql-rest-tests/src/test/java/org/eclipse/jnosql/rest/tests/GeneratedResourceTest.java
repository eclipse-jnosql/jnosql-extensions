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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.jnosql.rest.tests.sample.CustomerDTOMapper;
import org.eclipse.jnosql.rest.tests.sample.CustomerRepository;
import org.eclipse.jnosql.rest.tests.sample.CustomerRepositoryResource;
import org.eclipse.jnosql.rest.tests.sample.ProductRepository;
import org.eclipse.jnosql.rest.tests.sample.ProductRepositoryResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Generated Jakarta REST resources")
class GeneratedResourceTest {

    private static Method method(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing method " + name));
    }

    @Nested
    @DisplayName("When inspecting the class-level Jakarta REST annotations")
    class WhenTheClassIsInspected {

        @Test
        @DisplayName("Should use the repository path, JSON media types and the request scope")
        void shouldDeclareResourceAnnotations() {
            Class<?> resource = ProductRepositoryResource.class;

            assertThat(resource.getAnnotation(Path.class).value()).isEqualTo("/products");
            assertThat(resource.getAnnotation(Produces.class).value()).containsExactly(MediaType.APPLICATION_JSON);
            assertThat(resource.getAnnotation(Consumes.class).value()).containsExactly(MediaType.APPLICATION_JSON);
            assertThat(resource.isAnnotationPresent(RequestScoped.class)).isTrue();
        }

        @Test
        @DisplayName("Should be a public class named after the repository")
        void shouldBePublicAndNamedAfterTheRepository() {
            assertThat(Modifier.isPublic(ProductRepositoryResource.class.getModifiers())).isTrue();
            assertThat(ProductRepositoryResource.class.getSimpleName()).isEqualTo("ProductRepositoryResource");
        }
    }

    @Nested
    @DisplayName("When inspecting the standard operations")
    class WhenTheStandardOperationsAreInspected {

        @Test
        @DisplayName("Should map findById to GET /{id}")
        void shouldMapFindById() {
            Method method = method(ProductRepositoryResource.class, "findById");

            assertThat(method.isAnnotationPresent(GET.class)).isTrue();
            assertThat(method.getAnnotation(Path.class).value()).isEqualTo("/{id}");
            assertThat(method.getParameters()[0].getAnnotation(PathParam.class).value()).isEqualTo("id");
            assertThat(method.getParameters()[0].getType()).isEqualTo(UUID.class);
        }

        @Test
        @DisplayName("Should map findAll to GET on the resource root")
        void shouldMapFindAll() {
            Method method = method(ProductRepositoryResource.class, "findAll");

            assertThat(method.isAnnotationPresent(GET.class)).isTrue();
            assertThat(method.isAnnotationPresent(Path.class)).isFalse();
        }

        @Test
        @DisplayName("Should map insert to POST with a response")
        void shouldMapInsert() {
            Method method = method(ProductRepositoryResource.class, "insert");

            assertThat(method.isAnnotationPresent(POST.class)).isTrue();
        }

        @Test
        @DisplayName("Should map update to PUT and deleteById to DELETE")
        void shouldMapUpdateAndDelete() {
            assertThat(method(ProductRepositoryResource.class, "update").isAnnotationPresent(PUT.class)).isTrue();
            assertThat(method(ProductRepositoryResource.class, "deleteById")
                    .isAnnotationPresent(DELETE.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("When inspecting the repository-specific operations")
    class WhenTheDeclaredOperationsAreInspected {

        @Test
        @DisplayName("Should expose a @Find method as GET with a query parameter")
        void shouldMapFindMethod() {
            Method method = method(ProductRepositoryResource.class, "findByCategory");

            assertThat(method.isAnnotationPresent(GET.class)).isTrue();
            assertThat(method.getAnnotation(Path.class).value()).isEqualTo("/findByCategory");
            assertThat(method.getParameters()[0].getAnnotation(QueryParam.class).value()).isEqualTo("category");
            assertThat(method.getGenericReturnType().getTypeName())
                    .isEqualTo("java.util.List<org.eclipse.jnosql.rest.tests.sample.Product>");
        }

        @Test
        @DisplayName("Should expose count and exists queries as GET with scalar results")
        void shouldMapCountAndExists() {
            assertThat(method(ProductRepositoryResource.class, "countByCategory").getReturnType())
                    .isEqualTo(long.class);
            assertThat(method(ProductRepositoryResource.class, "existsByName").getReturnType())
                    .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("Should expose deleteBy only through @RestExpose with the custom path and DELETE")
        void shouldMapExplicitDeleteQuery() {
            Method method = method(ProductRepositoryResource.class, "deleteByCategory");

            assertThat(method.isAnnotationPresent(DELETE.class)).isTrue();
            assertThat(method.getAnnotation(Path.class).value()).isEqualTo("/purge/{category}");
            assertThat(method.getParameters()[0].getAnnotation(PathParam.class).value()).isEqualTo("category");
        }

        @Test
        @DisplayName("Should not expose mutating queries without @RestExpose nor excluded methods")
        void shouldSkipUnexposedMethods() {
            List<String> names = Arrays.stream(ProductRepositoryResource.class.getDeclaredMethods())
                    .map(Method::getName).toList();

            assertThat(names).doesNotContain("purgeQuery", "findByNameLike");
        }
    }

    @Nested
    @DisplayName("When inspecting the constructors")
    class WhenTheConstructorsAreInspected {

        @ParameterizedTest(name = "{0}")
        @ValueSource(classes = {ProductRepositoryResource.class, CustomerRepositoryResource.class})
        @DisplayName("Should declare a package-private no-argument constructor")
        void shouldDeclarePackagePrivateNoArgumentConstructor(Class<?> resource) throws Exception {
            Constructor<?> constructor = resource.getDeclaredConstructor();
            int modifiers = constructor.getModifiers();

            assertThat(Modifier.isPublic(modifiers)).isFalse();
            assertThat(Modifier.isProtected(modifiers)).isFalse();
            assertThat(Modifier.isPrivate(modifiers)).isFalse();
            assertThat(constructor.getAnnotations()).isEmpty();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(classes = {ProductRepositoryResource.class, CustomerRepositoryResource.class})
        @DisplayName("Should initialize every dependency field to null in the no-argument constructor")
        void shouldInitializeDependenciesToNull(Class<?> resource) throws Exception {
            Constructor<?> constructor = resource.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            for (Field field : resource.getDeclaredFields()) {
                field.setAccessible(true);
                assertThat(field.get(instance)).as(field.getName()).isNull();
            }
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(classes = {ProductRepositoryResource.class, CustomerRepositoryResource.class})
        @DisplayName("Should declare exactly one public @Inject constructor")
        void shouldDeclareOnePublicInjectConstructor(Class<?> resource) {
            List<Constructor<?>> injected = Arrays.stream(resource.getDeclaredConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Inject.class)).toList();

            assertThat(injected).hasSize(1);
            assertThat(Modifier.isPublic(injected.get(0).getModifiers())).isTrue();
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(classes = {ProductRepositoryResource.class, CustomerRepositoryResource.class})
        @DisplayName("Should declare only final dependency fields and no setters")
        void shouldDeclareFinalFields(Class<?> resource) {
            assertThat(resource.getDeclaredFields()).isNotEmpty()
                    .allSatisfy(field -> {
                        assertThat(Modifier.isFinal(field.getModifiers())).isTrue();
                        assertThat(field.isAnnotationPresent(Inject.class)).isFalse();
                    });
            assertThat(Arrays.stream(resource.getDeclaredMethods()).map(Method::getName))
                    .noneMatch(name -> name.startsWith("set"));
        }

        @Test
        @DisplayName("Should inject only the repository when DTOs are not used")
        void shouldInjectOnlyTheRepository() {
            Constructor<?> constructor = Arrays.stream(ProductRepositoryResource.class.getConstructors())
                    .findFirst().orElseThrow();

            assertThat(constructor.getParameterTypes()).containsExactly(ProductRepository.class);
            assertThat(ProductRepositoryResource.class.getDeclaredFields()).hasSize(1);
        }

        @Test
        @DisplayName("Should inject the repository and the mapper interface when DTOs are used")
        void shouldInjectRepositoryAndMapper() {
            Constructor<?> constructor = Arrays.stream(CustomerRepositoryResource.class.getConstructors())
                    .findFirst().orElseThrow();

            assertThat(constructor.getParameterTypes())
                    .containsExactly(CustomerRepository.class, CustomerDTOMapper.class);
            assertThat(CustomerDTOMapper.class.isInterface()).isTrue();
        }
    }
}
