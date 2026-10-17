/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.core.repository.operations;

import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryResolver;
import org.eclipse.jnosql.mapping.core.repository.ProviderQueryHandlerResolver;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreProviderOperationBuilderTest {

    @Test
    @DisplayName("Should build successfully with resolver")
    void shouldBuildSuccessfully() {
        ProviderQueryHandlerResolver resolver = mock(ProviderQueryHandlerResolver.class);
        var result = CoreProviderOperationBuilder.builder()
                .withResolver(resolver)
                .build();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should throw when resolver is null")
    void shouldThrowWhenResolverIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> CoreProviderOperationBuilder.builder()
                        .withResolver(null));
    }

    @Test
    @DisplayName("Should throw when building without resolver")
    void shouldThrowWhenBuildingWithoutResolver() {
        assertThatNullPointerException()
                .isThrownBy(() -> CoreProviderOperationBuilder.builder()
                        .build());
    }

    @Test
    @DisplayName("Should allow step redefinition")
    void shouldAllowStepRedefinition() {
        ProviderQueryHandlerResolver resolver1 = mock(ProviderQueryHandlerResolver.class);
        ProviderQueryHandlerResolver resolver2 = mock(ProviderQueryHandlerResolver.class);
        var step1 = CoreProviderOperationBuilder.builder();
        var step1With1 = step1.withResolver(resolver1);
        var step1With2 = step1.withResolver(resolver2);

        assertThat(step1With1).isNotSameAs(step1With2);
    }
}

class DefaultInfrastructureOperatorProviderBuilderTest {

    @Test
    @DisplayName("Should build successfully with all operators")
    void shouldBuildSuccessfully() {
        var builtIn = mock(org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator.class);
        var object = mock(org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator.class);
        var custom = mock(CustomRepositoryMethodOperator.class);
        var defaultOp = mock(org.eclipse.jnosql.mapping.core.repository.DefaultMethodOperator.class);
        
        var result = InfrastructureOperatorProviderBuilder.builder()
                .withBuiltIn(builtIn)
                .withObject(object)
                .withCustom(custom)
                .withDefault(defaultOp)
                .build();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should throw when builtIn is null")
    void shouldThrowWhenBuiltInIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> InfrastructureOperatorProviderBuilder.builder()
                        .withBuiltIn(null));
    }

    @Test
    @DisplayName("Should throw when object is null")
    void shouldThrowWhenObjectIsNull() {
        var builtIn = mock(org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator.class);
        assertThatNullPointerException()
                .isThrownBy(() -> InfrastructureOperatorProviderBuilder.builder()
                        .withBuiltIn(builtIn)
                        .withObject(null));
    }

    @Test
    @DisplayName("Should throw when custom is null")
    void shouldThrowWhenCustomIsNull() {
        var builtIn = mock(org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator.class);
        var object = mock(org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator.class);
        assertThatNullPointerException()
                .isThrownBy(() -> InfrastructureOperatorProviderBuilder.builder()
                        .withBuiltIn(builtIn)
                        .withObject(object)
                        .withCustom(null));
    }

    @Test
    @DisplayName("Should throw when default is null")
    void shouldThrowWhenDefaultIsNull() {
        var builtIn = mock(org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator.class);
        var object = mock(org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator.class);
        var custom = mock(CustomRepositoryMethodOperator.class);
        assertThatNullPointerException()
                .isThrownBy(() -> InfrastructureOperatorProviderBuilder.builder()
                        .withBuiltIn(builtIn)
                        .withObject(object)
                        .withCustom(custom)
                        .withDefault(null));
    }
}

class DefaultCustomRepositoryMethodOperatorTest {

    @Test
    @DisplayName("Should use resolver to invoke custom repository")
    void shouldUseResolverToInvoke() throws Exception {
        Object expectedRepository = new Object();
        CustomRepositoryResolver resolver = repoClass -> expectedRepository;
        DefaultCustomRepositoryMethodOperator operator = new DefaultCustomRepositoryMethodOperator(resolver);

        Method method = Object.class.getMethod("toString");
        Object[] params = {};

        Object result = operator.invokeCustomRepository(method, params);

        assertThat(result).isEqualTo(expectedRepository.toString());
    }

    @Test
    @DisplayName("Should throw when resolver returns null")
    void shouldThrowWhenResolverReturnsNull() throws Exception {
        CustomRepositoryResolver resolver = repoClass -> null;
        DefaultCustomRepositoryMethodOperator operator = new DefaultCustomRepositoryMethodOperator(resolver);

        Method method = Object.class.getMethod("toString");
        Object[] params = {};

        org.assertj.core.api.AbstractThrowableAssert<?, ? extends Throwable> assertion = 
            assertThatThrownBy(() -> operator.invokeCustomRepository(method, params));
        assertion.isInstanceOf(NullPointerException.class);
    }
}