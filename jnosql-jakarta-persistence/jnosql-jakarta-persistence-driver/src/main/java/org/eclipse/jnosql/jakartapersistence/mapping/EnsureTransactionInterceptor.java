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
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.data.page.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;

/**
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
@MethodInterceptor.Repository
public class EnsureTransactionInterceptor implements MethodInterceptor {

    /**
     * Entity managers for which an interception is already in progress on the current thread. A repository
     * default method that calls other repository methods re-enters this interceptor for the same entity manager.
     * The outer interception already ensured the transaction, so the nested one must not go through
     * {@link RunInGlobalTransaction} again: the container's {@code @Transactional} interceptor of this shared
     * bean is not re-entrant, and a nested call leaves the caller's {@code UserTransaction} unusable in GlassFish
     * ("Operation not allowed").
     */
    private static final ThreadLocal<Set<EntityManager>> ACTIVE_ENTITY_MANAGERS
            = ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    @Inject
    private RunInGlobalTransaction runInGlobalTransaction;

    @Override
    public Object intercept(InvocationContext context) throws Exception {
        EntityManager entityManager = (EntityManager)context.getContextData().get(EntityManager.class.getName());
        Set<EntityManager> active = ACTIVE_ENTITY_MANAGERS.get();
        if (!active.add(entityManager)) {
            return context.proceed();
        }
        try {
            final boolean transactionWillBeCreated = !entityManager.isJoinedToTransaction();
            return runInGlobalTransaction.execute(() -> runInNewOrExistingTransaction(entityManager, context, transactionWillBeCreated));
        } finally {
            active.remove(entityManager);
            if (active.isEmpty()) {
                ACTIVE_ENTITY_MANAGERS.remove();
            }
        }
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
        if (transactionWillBeCreated && result instanceof Page page) {
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
