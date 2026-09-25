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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;

class CustomRepositoryMethodOperatorBuilderTest {

    @Test
    @DisplayName("Should build successfully with resolver")
    void shouldBuildSuccessfully() {
        CustomRepositoryResolver resolver = mock(CustomRepositoryResolver.class);
        CustomRepositoryMethodOperator result = DefaultCustomRepositoryMethodOperatorBuilder.builder()
                .withResolver(resolver)
                .build();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(CustomRepositoryMethodOperator.class);
    }

    @Test
    @DisplayName("Should throw when resolver is null")
    void shouldThrowWhenResolverIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> DefaultCustomRepositoryMethodOperatorBuilder.builder()
                        .withResolver(null));
    }

    @Test
    @DisplayName("Should throw when building without resolver")
    void shouldThrowWhenBuildingWithoutResolver() {
        assertThatNullPointerException()
                .isThrownBy(() -> DefaultCustomRepositoryMethodOperatorBuilder.builder()
                        .build());
    }

    @Test
    @DisplayName("Should allow step redefinition")
    void shouldAllowStepRedefinition() {
        CustomRepositoryResolver resolver1 = mock(CustomRepositoryResolver.class);
        CustomRepositoryResolver resolver2 = mock(CustomRepositoryResolver.class);
        var step1 = DefaultCustomRepositoryMethodOperatorBuilder.builder();
        var step1With1 = step1.withResolver(resolver1);
        var step1With2 = step1.withResolver(resolver2);

        assertThat(step1With1).isNotSameAs(step1With2);
    }
}