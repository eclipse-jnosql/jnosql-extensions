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
 *   Renat Safiullin
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Begins a transaction before the intercepted method and commits it after the method returns,
 * like {@code jakarta.transaction.Transactional} does with a JTA transaction.
 */
@InTransaction
@Interceptor
@Priority(1000)
public class InTransactionInterceptor {

    @Inject
    private EntityManager entityManager;

    @AroundInvoke
    public Object runInTransaction(InvocationContext context) throws Exception {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            Object result = context.proceed();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
