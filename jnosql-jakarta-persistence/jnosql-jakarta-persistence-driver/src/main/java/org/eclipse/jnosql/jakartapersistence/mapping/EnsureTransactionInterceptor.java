/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 *
 * @author Ondro Mihalyi
 */
final public class EnsureTransactionInterceptor {

    private EnsureTransactionInterceptor() {
    }

    @FunctionalInterface
    public interface CallableWithThrowable<T> {
        T call() throws Throwable;
    }

    public static Object invokeInTransaction(EntityManager entityManager, CallableWithThrowable<Object> action) throws Throwable {
        // TODO: Support JTA transactions
        boolean inTransaction = entityManager.isJoinedToTransaction();
        if (inTransaction) {
            return action.call();
        } else {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            try {
                Object result = action.call();
                transaction.commit();
                return result;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}
