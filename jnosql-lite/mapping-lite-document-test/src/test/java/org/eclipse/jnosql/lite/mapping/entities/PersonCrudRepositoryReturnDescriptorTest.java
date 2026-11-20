/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.lite.mapping.entities;

import org.eclipse.jnosql.lite.mapping.metadata.LiteRepositoriesMetadata;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.repository.MethodSignatureKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.ReturnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Return metadata of the generated Person repository")
class PersonCrudRepositoryReturnDescriptorTest {

    private static final ReturnType PERSON = ReturnType.of(Person.class);

    private static RepositoryMethod method(String name) {
        var key = new MethodSignatureKey(name, List.of(String.class));
        return LiteRepositoriesMetadata.INSTANCE.get(PersonCrudRepository.class)
                .flatMap(metadata -> metadata.find(key))
                .orElseThrow();
    }

    static Stream<Arguments> asyncMethods() {
        return Stream.of(
                Arguments.of("asyncPerson", CompletionStage.class, Person.class,
                        ReturnType.of(CompletionStage.class, PERSON)),
                Arguments.of("asyncOptionalPerson", CompletionStage.class, Person.class,
                        ReturnType.of(CompletionStage.class, ReturnType.of(Optional.class, PERSON))),
                Arguments.of("asyncPeople", CompletionStage.class, Person.class,
                        ReturnType.of(CompletionStage.class, ReturnType.of(List.class, PERSON))),
                Arguments.of("asyncLong", CompletionStage.class, Long.class,
                        ReturnType.of(CompletionStage.class, ReturnType.of(Long.class))),
                Arguments.of("asyncBoolean", CompletionStage.class, Boolean.class,
                        ReturnType.of(CompletionStage.class, ReturnType.of(Boolean.class))),
                Arguments.of("futurePerson", CompletableFuture.class, Person.class,
                        ReturnType.of(CompletableFuture.class, PERSON)),
                Arguments.of("futureOptionalPerson", CompletableFuture.class, Person.class,
                        ReturnType.of(CompletableFuture.class, ReturnType.of(Optional.class, PERSON))),
                Arguments.of("futurePeople", CompletableFuture.class, Person.class,
                        ReturnType.of(CompletableFuture.class, ReturnType.of(List.class, PERSON))),
                Arguments.of("futureLong", CompletableFuture.class, Long.class,
                        ReturnType.of(CompletableFuture.class, ReturnType.of(Long.class))),
                Arguments.of("futureBoolean", CompletableFuture.class, Boolean.class,
                        ReturnType.of(CompletableFuture.class, ReturnType.of(Boolean.class))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("asyncMethods")
    @DisplayName("Should keep the asynchronous wrapper and the nested types")
    void shouldDescribeAsyncReturnType(String name, Class<?> wrapper, Class<?> element, ReturnType expected) {
        var method = method(name);

        assertThat(method.returnType()).contains(wrapper);
        assertThat(method.elementType()).contains(element);
        assertThat(method.returnDescriptor()).contains(expected);
    }

    @Test
    @DisplayName("Should distinguish nested return types that share the same wrapper")
    void shouldDistinguishNestedTypes() {
        var plain = method("asyncPerson").returnDescriptor().orElseThrow();
        var optional = method("asyncOptionalPerson").returnDescriptor().orElseThrow();
        var list = method("asyncPeople").returnDescriptor().orElseThrow();

        assertThat(List.of(plain, optional, list)).doesNotHaveDuplicates();
        assertThat(optional.element()).isEqualTo(PERSON);
        assertThat(list.argument().orElseThrow().isA(List.class)).isTrue();
    }

    @Test
    @DisplayName("Should describe a synchronous parameterized return type")
    void shouldDescribeSynchronousReturnType() {
        assertThat(method("findByName").returnDescriptor())
                .contains(ReturnType.of(List.class, PERSON));
    }

    @Test
    @DisplayName("Should use the default descriptor when the return type has no arguments")
    void shouldUseDefaultDescriptor() {
        var method = method("existsByName");

        assertThat(method.returnDescriptor())
                .isEqualTo(method.returnType().map(ReturnType::of))
                .contains(ReturnType.of(boolean.class));
    }

    @Test
    @DisplayName("Should execute an asynchronous method with the core return handlers")
    void shouldCompleteAsyncReturn() throws Exception {
        var person = new Person();
        var method = method("asyncOptionalPerson");

        Object result = DynamicReturn.<Person>builder()
                .classSource(Person.class)
                .methodName(method.name())
                .returnType(method.returnType().orElseThrow())
                .returnDescriptor(method.returnDescriptor().orElse(null))
                .singleResult(() -> Optional.of(person))
                .result(Stream::empty)
                .build()
                .execute();

        assertThat(result).isInstanceOf(CompletionStage.class);
        var value = ((CompletionStage<?>) result).toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertThat(value).isEqualTo(Optional.of(person));
    }

    @Test
    @DisplayName("Should complete exceptionally when the operation fails")
    void shouldCompleteExceptionally() {
        var method = method("futurePerson");

        Object result = DynamicReturn.<Person>builder()
                .classSource(Person.class)
                .methodName(method.name())
                .returnType(method.returnType().orElseThrow())
                .returnDescriptor(method.returnDescriptor().orElse(null))
                .singleResult(() -> {
                    throw new IllegalStateException("database failure");
                })
                .result(Stream::empty)
                .build()
                .execute();

        assertThat(result).isInstanceOf(CompletableFuture.class);
        assertThatThrownBy(() -> ((CompletableFuture<?>) result).get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
