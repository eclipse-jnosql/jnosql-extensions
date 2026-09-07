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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderQueryHandlerResolverTest {

    @Test
    @DisplayName("Should resolve ProviderQueryHandler when resolver returns instance")
    void shouldResolveProviderQueryHandler() {
        ProviderQueryHandler expectedHandler = mock(ProviderQueryHandler.class);
        ProviderQueryHandlerResolver resolver = () -> expectedHandler;

        ProviderQueryHandler result = resolver.resolve();

        assertThat(result).isSameAs(expectedHandler);
    }

    @Test
    @DisplayName("Should return null when resolver returns null")
    void shouldReturnNullWhenResolverReturnsNull() {
        ProviderQueryHandlerResolver resolver = () -> null;

        ProviderQueryHandler result = resolver.resolve();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should throw exception from resolver when handler throws")
    void shouldThrowWhenResolverThrows() {
        ProviderQueryHandlerResolver resolver = () -> {
            throw new IllegalStateException("Resolver error");
        };

        assertThatThrownBy(resolver::resolve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Resolver error");
    }
}

class CustomRepositoryResolverTest {

    @Test
    @DisplayName("Should resolve repository when resolver returns instance")
    void shouldResolveRepository() {
        Object expectedRepository = mock(Object.class);
        CustomRepositoryResolver resolver = repoClass -> expectedRepository;

        Object result = resolver.resolve(Object.class);

        assertThat(result).isSameAs(expectedRepository);
    }

    @Test
    @DisplayName("Should return null when resolver returns null")
    void shouldReturnNullWhenResolverReturnsNull() {
        CustomRepositoryResolver resolver = repoClass -> null;

        Object result = resolver.resolve(Object.class);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should throw exception from resolver when handler throws")
    void shouldThrowWhenResolverThrows() {
        CustomRepositoryResolver resolver = repoClass -> {
            throw new IllegalStateException("Resolver error");
        };

        assertThatThrownBy(() -> resolver.resolve(Object.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Resolver error");
    }
}