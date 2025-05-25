/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;


/**
 *
 * @author Ondro Mihalyi
 */
@Interceptor
@EnsureTransaction
@Priority(100)
public class EnsureTransactionInterceptor {

    @Inject
    PersistenceDatabaseManager manager;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        EntityManager entityManager = manager.getEntityManager();
        boolean inTransaction = entityManager.isJoinedToTransaction();
        if (inTransaction) {
            return ctx.proceed();
        } else {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            try {
                Object result = ctx.proceed();
                transaction.commit();
                return result;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}
