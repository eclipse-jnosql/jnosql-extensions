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

import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.eclipse.jnosql.mapping.core.repository.returns.RepositoryAsyncExecutor;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * A {@link RepositoryAsyncExecutor} that schedules asynchronous repository operations, the ones declared with
 * {@link CompletionStage} or {@link CompletableFuture}, on a Jakarta Concurrency {@link ManagedExecutorService}.
 *
 * <p>The provider is registered with {@link java.util.ServiceLoader}, so adding this module to the runtime
 * classpath is enough to activate it. The managed executor is resolved through JNDI on every call, in the naming
 * context of the calling component, and is never cached, closed or shut down: it belongs to the container.
 * The JNDI name is {@value #DEFAULT_JNDI_NAME}, or the value of {@value #JNDI_NAME_PROPERTY} when it is set,
 * for example {@code java:app/concurrent/JNoSQLExecutor}.</p>
 *
 * <p>Whether the operation runs on a platform or a virtual thread, and which context, such as security or class
 * loader, is propagated to it, is decided by the managed executor's configuration. Failures to resolve the
 * executor or to schedule the operation complete the returned stage exceptionally; there is no fallback to a
 * Java SE executor.</p>
 */
public final class JakartaConcurrencyAsyncExecutor implements RepositoryAsyncExecutor {

    static final String JNDI_NAME_PROPERTY = "jnosql.repository.async.concurrency.jndi-name";

    static final String DEFAULT_JNDI_NAME = "java:comp/DefaultManagedExecutorService";

    private final Supplier<Optional<String>> configuredName;
    private final JndiLookup lookup;

    /**
     * Creates the provider, as required by {@link java.util.ServiceLoader}.
     */
    public JakartaConcurrencyAsyncExecutor() {
        this(JakartaConcurrencyAsyncExecutor::readConfiguredName, JakartaConcurrencyAsyncExecutor::lookupInJndi);
    }

    JakartaConcurrencyAsyncExecutor(Supplier<Optional<String>> configuredName, JndiLookup lookup) {
        this.configuredName = Objects.requireNonNull(configuredName, "configuredName is required");
        this.lookup = Objects.requireNonNull(lookup, "lookup is required");
    }

    @Override
    public <T> CompletionStage<T> execute(Supplier<T> operation) {
        Objects.requireNonNull(operation, "operation is required");
        try {
            return managedExecutor().supplyAsync(operation);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private ManagedExecutorService managedExecutor() {
        String name = configuredName.get().orElse(DEFAULT_JNDI_NAME);
        if (name.isBlank()) {
            throw new IllegalArgumentException("The property " + JNDI_NAME_PROPERTY
                    + " must not be blank, remove it to use " + DEFAULT_JNDI_NAME);
        }
        Object resource;
        try {
            resource = lookup.lookup(name.trim());
        } catch (NamingException e) {
            throw new IllegalStateException("Cannot resolve the managed executor service '" + name.trim()
                    + "' configured by " + JNDI_NAME_PROPERTY, e);
        }
        if (resource instanceof ManagedExecutorService executor) {
            return executor;
        }
        throw new IllegalStateException("The JNDI resource '" + name.trim() + "' must be a "
                + ManagedExecutorService.class.getName() + " but was "
                + (resource == null ? "null" : resource.getClass().getName()));
    }

    private static Optional<String> readConfiguredName() {
        return Optional.ofNullable(ConfigProvider.getConfig().getConfigValue(JNDI_NAME_PROPERTY).getRawValue());
    }

    private static Object lookupInJndi(String name) throws NamingException {
        return new InitialContext().lookup(name);
    }

    @FunctionalInterface
    interface JndiLookup {
        Object lookup(String name) throws NamingException;
    }
}
