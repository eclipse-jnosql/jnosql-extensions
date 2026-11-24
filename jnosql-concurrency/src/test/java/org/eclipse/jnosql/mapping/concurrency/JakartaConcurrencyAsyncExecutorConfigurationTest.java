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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DisplayName("JakartaConcurrencyAsyncExecutor configuration read from MicroProfile Config")
class JakartaConcurrencyAsyncExecutorConfigurationTest {

    private static final String[] PROPERTIES = {
            "jnosql.repository.async.concurrency.jndi-name",
            "jnosql.repository.async.executor",
            "jnosql.repository.async.pool-size",
            "jnosql.repository.async.queue-capacity"};

    @AfterEach
    void clearProperties() {
        for (String property : PROPERTIES) {
            System.clearProperty(property);
        }
    }

    private static Throwable failureOfExecution() {
        return catchThrowable(() -> new JakartaConcurrencyAsyncExecutor().execute(() -> "value")
                .toCompletableFuture().get(10, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should look up the default managed executor when the property is absent")
    void shouldLookUpDefaultName() {
        var error = failureOfExecution();

        assertThat(error).isInstanceOf(ExecutionException.class);
        assertThat(error.getCause()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("java:comp/DefaultManagedExecutorService");
    }

    @Test
    @DisplayName("Should look up the managed executor named by the property")
    void shouldLookUpConfiguredName() {
        System.setProperty("jnosql.repository.async.concurrency.jndi-name", "java:app/concurrent/JNoSQLExecutor");

        assertThat(failureOfExecution().getCause()).hasMessageContaining("java:app/concurrent/JNoSQLExecutor");
    }

    @Test
    @DisplayName("Should reject a blank property")
    void shouldRejectBlankProperty() {
        System.setProperty("jnosql.repository.async.concurrency.jndi-name", "   ");

        assertThat(failureOfExecution().getCause()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should ignore the core executor properties and never fall back to a Java SE executor")
    void shouldIgnoreCoreExecutorProperties() {
        System.setProperty("jnosql.repository.async.executor", "virtual");
        System.setProperty("jnosql.repository.async.pool-size", "4");
        System.setProperty("jnosql.repository.async.queue-capacity", "10");

        assertThat(failureOfExecution().getCause()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("managed executor service");
    }
}
