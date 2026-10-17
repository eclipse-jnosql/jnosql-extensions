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

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CallerManagedTransactionTest {

    private SeContainer cdiContainer;
    private DefaultMethodPersonRepository repository;
    private EntityTransaction callerTransaction;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(DefaultMethodPersonRepository.class);
        cdiContainer = TestSupport.cdiInitializerWithoutEmProducer()
                .addBeanClasses(ContainerManagedEntityManagerProducer.class)
                .initialize();
        repository = cdiContainer.select(DefaultMethodPersonRepository.class).get();
        cdiContainer.select(jakarta.persistence.EntityManager.class).get().isOpen();
        callerTransaction = ContainerManagedEntityManagerProducer.callerTransaction();
        callerTransaction.begin();
    }

    @AfterEach
    void cleanup() {
        if (callerTransaction != null && callerTransaction.isActive()) {
            callerTransaction.rollback();
        }
        cdiContainer.close();
    }

    @Nested
    @DisplayName("When a default method of a CDI repository runs inside a caller-managed transaction")
    class WhenTheDefaultMethodIsInvoked {

        @Test
        @DisplayName("Should allow the caller to commit the transaction")
        void shouldCommitCallerTransaction() {
            repository.addIfMissing(person());

            assertThatCode(callerTransaction::commit).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow the caller to roll back the transaction")
        void shouldRollbackCallerTransaction() {
            repository.addIfMissing(person());

            assertThat(callerTransaction.isActive()).isTrue();
            assertThatCode(callerTransaction::rollback).doesNotThrowAnyException();
        }

        private Person person() {
            Person person = new Person();
            person.setName("Alice");
            return person;
        }
    }
}
