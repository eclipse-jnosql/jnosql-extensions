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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.extensions.sql.model.ComputerDefaultMethodRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@EnableWeld
class DefaultMethodTransactionTest extends AbstractTestRepository {

    @Inject
    private SqlRepositoryProducer producer;

    private EntityManager delegate;
    private EntityTransaction callerTransaction;
    private ComputerDefaultMethodRepository repository;

    @BeforeEach
    void setUp() {
        delegate = Persistence.createEntityManagerFactory("testPersistenceUnit").createEntityManager();
        repository = producer.get(ComputerDefaultMethodRepository.class, SqlTemplate.of(containerManaged(delegate)));
        callerTransaction = delegate.getTransaction();
        callerTransaction.begin();
    }

    @AfterEach
    void tearDown() {
        if (callerTransaction.isActive()) {
            callerTransaction.rollback();
        }
        delegate.close();
    }

    @Nested
    @DisplayName("When a default method runs inside a transaction managed by the caller")
    class WhenTheDefaultMethodIsInvoked {

        @Test
        @DisplayName("Should leave the caller transaction untouched so it can be committed")
        void shouldCommitCallerTransaction() {
            repository.addIfMissing(Computer.of("MacBook", 2024));

            assertThat(callerTransaction.isActive()).isTrue();
            assertThatCode(callerTransaction::commit).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow the caller to roll back the transaction")
        void shouldRollbackCallerTransaction() {
            repository.addIfMissing(Computer.of("MacBook", 2024));

            assertThatCode(callerTransaction::rollback).doesNotThrowAnyException();
        }
    }

    /**
     * Mimics a container-managed (JTA) entity manager: the transaction is owned by the caller, so
     * {@code getTransaction()} is forbidden and {@code isJoinedToTransaction()} reports the active transaction.
     */
    private static EntityManager containerManaged(EntityManager target) {
        return (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
                new Class[]{EntityManager.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getTransaction" ->
                                throw new IllegalStateException("A JTA EntityManager cannot use getTransaction()");
                        case "isJoinedToTransaction" -> {
                            return target.getTransaction().isActive();
                        }
                        default -> {
                            try {
                                return method.invoke(target, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        }
                    }
                });
    }
}
