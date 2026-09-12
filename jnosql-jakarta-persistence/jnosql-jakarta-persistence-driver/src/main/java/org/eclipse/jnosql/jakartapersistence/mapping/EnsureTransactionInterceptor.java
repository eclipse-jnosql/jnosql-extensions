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

import jakarta.data.page.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;

import java.util.concurrent.Callable;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;

/**
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
@MethodInterceptor.Repository
public class EnsureTransactionInterceptor implements MethodInterceptor {

    @Inject
    private RunInGlobalTransaction runInGlobalTransaction;

    @Override
    public Object intercept(InvocationContext context) throws Exception {
        EntityManager entityManager = (EntityManager)context.getContextData().get(EntityManager.class.getName());

        return runInGlobalTransaction.execute(() -> runInNewOrExistingTransaction(entityManager, context));
    }

    private Object runInNewOrExistingTransaction(EntityManager entityManager, InvocationContext context) throws Exception {
        try {
            boolean inTransaction = entityManager.isJoinedToTransaction();
            if (inTransaction) {
                final Object result = context.proceed();
                return fetchIfNeeded(result);
            } else {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                try {
                    Object result = context.proceed();
                    result = fetchIfNeeded(result);
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

    private Object fetchIfNeeded(Object result) {
        // A transaction-scoped EntityManager is closed when the transaction ends, whether this interceptor
        // or the caller started it, and the returned Page outlives it
        if (result instanceof Page page) {
            page.hasContent();
            if (page.hasTotals()) {
                page.totalElements();
            }
        }
        return result;
    }

    @ApplicationScoped
    @Transactional
    public static class RunInGlobalTransaction {
        public Object execute(Callable callable) throws Exception {
            return callable.call();
        }
    }
}
