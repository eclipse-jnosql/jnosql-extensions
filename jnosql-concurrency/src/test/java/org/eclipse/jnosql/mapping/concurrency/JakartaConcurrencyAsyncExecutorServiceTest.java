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
package org.eclipse.jnosql.mapping.concurrency;

import org.eclipse.jnosql.mapping.core.repository.returns.RepositoryAsyncExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JakartaConcurrencyAsyncExecutor service registration")
class JakartaConcurrencyAsyncExecutorServiceTest {

    @Test
    @DisplayName("Should be the only RepositoryAsyncExecutor discovered by ServiceLoader")
    void shouldBeDiscoveredByServiceLoader() {
        var providers = ServiceLoader.load(RepositoryAsyncExecutor.class).stream().toList();

        assertThat(providers).hasSize(1);
        assertThat(providers.get(0).type()).isEqualTo(JakartaConcurrencyAsyncExecutor.class);
        assertThat(providers.get(0).get()).isInstanceOf(JakartaConcurrencyAsyncExecutor.class);
    }

    @Test
    @DisplayName("Should expose a public class with a public no-argument constructor")
    void shouldHavePublicNoArgumentConstructor() throws NoSuchMethodException {
        var type = JakartaConcurrencyAsyncExecutor.class;

        assertThat(Modifier.isPublic(type.getModifiers())).isTrue();
        assertThat(Modifier.isPublic(type.getConstructor().getModifiers())).isTrue();
    }
}
