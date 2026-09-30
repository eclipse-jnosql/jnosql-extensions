/*
 * Copyright (c) 2024,2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 *  Renat R. Safiullin
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.annotation.PostConstruct;
import jakarta.data.page.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;

import java.util.concurrent.Callable;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
@MethodInterceptor.Repository
public class EnsureTransactionInterceptor implements MethodInterceptor {

    /**
     * Configuration property that makes a {@link Page} returned from a repository method load its content, and its
     * total if the {@link jakarta.data.page.PageRequest} requests it, before the method returns, also when the
     * transaction was started by the caller. Defaults to {@code false}.
     * <p>
     * The property is read from MicroProfile Config, or from system properties if MicroProfile Config is not available.
     */
    public static final String EAGER_PAGE_PROPERTY = "jnosql.jakarta.persistence.page.eager";

    @Inject
    private RunInGlobalTransaction runInGlobalTransaction;

    private boolean eagerPage;

    @PostConstruct
    void readConfiguration() {
        eagerPage = isEagerPageConfigured();
    }

    @Override
    public Object intercept(InvocationContext context) throws Exception {
        EntityManager entityManager = (EntityManager)context.getContextData().get(EntityManager.class.getName());
        final boolean transactionWillBeCreated = !entityManager.isJoinedToTransaction();

        return runInGlobalTransaction.execute(() -> runInNewOrExistingTransaction(entityManager, context, transactionWillBeCreated));
    }

    private Object runInNewOrExistingTransaction(EntityManager entityManager, InvocationContext context, boolean transactionWillBeCreated) throws Exception {
        try {
            boolean inTransaction = entityManager.isJoinedToTransaction();
            if (inTransaction) {
                final Object result = context.proceed();
                return fetchIfNeeded(result, transactionWillBeCreated);
            } else {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                try {
                    Object result = context.proceed();
                    result = fetchIfNeeded(result, transactionWillBeCreated);
                    transaction.commit();
                    return result;
                } catch (Exception e) {
                    transaction.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    private Object fetchIfNeeded(Object result, boolean transactionWillBeCreated) {
        // Without eager loading, a Page returned within a transaction started by the caller is loaded lazily
        // and must be read before that transaction ends
        if ((transactionWillBeCreated || eagerPage) && result instanceof Page page) {
            page.hasContent();
            if (page.hasTotals()) {
                page.totalElements();
            }
        }
        return result;
    }

    private static boolean isEagerPageConfigured() {
        try {
            return ConfigProvider.getConfig().getOptionalValue(EAGER_PAGE_PROPERTY, Boolean.class).orElse(false);
        } catch (IllegalStateException | NoClassDefFoundError e) {
            // No MicroProfile Config implementation or API
            return Boolean.getBoolean(EAGER_PAGE_PROPERTY);
        }
    }

    @ApplicationScoped
    @Transactional
    public static class RunInGlobalTransaction {
        public Object execute(Callable callable) throws Exception {
            return callable.call();
        }
    }
}
