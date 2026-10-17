/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Produces an entity manager that mimics a container-managed (JTA) one: the transaction is owned by the caller,
 * so {@code getTransaction()} is forbidden and {@code isJoinedToTransaction()} reports the active transaction.
 */
@ApplicationScoped
public class ContainerManagedEntityManagerProducer {

    private static volatile EntityManager target;

    /** The transaction owned by the "container", which the test begins and completes like a UserTransaction. */
    static EntityTransaction callerTransaction() {
        return target.getTransaction();
    }

    @Produces
    @ApplicationScoped
    public EntityManager createEntityManager() {
        EntityManager created = Persistence.createEntityManagerFactory("testPersistenceUnit").createEntityManager();
        target = created;
        return (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
                new Class[]{EntityManager.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getTransaction" ->
                                throw new IllegalStateException("A JTA EntityManager cannot use getTransaction()");
                        case "isJoinedToTransaction" -> {
                            return created.getTransaction().isActive();
                        }
                        case "close" -> {
                            created.getEntityManagerFactory().close();
                            return null;
                        }
                        default -> {
                            try {
                                return method.invoke(created, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        }
                    }
                });
    }

    public void closeEntityManager(@Disposes EntityManager entityManager) {
        entityManager.close();
    }
}
