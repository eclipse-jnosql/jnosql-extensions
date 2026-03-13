/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import ee.omnifish.jnosql.jakartapersistence.TestJakartaPersistenceClassScanner;
import jakarta.enterprise.inject.se.SeContainer;

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.extensions.sql.infrastructure.RepositorySupport;
import org.eclipse.jnosql.extensions.sql.model.ComputerRepository;
import org.eclipse.jnosql.extensions.sql.model.Computers;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.PersistenceRepositoryProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;


class PersistenceRepositoryProducerTest {

    private SeContainer cdiContainer;


    private PersistenceRepositoryProducer producer;

    private PersistenceDatabaseManager persistenceDatabaseManager;



    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.customRepositories = Set.of(Computers.class);
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(ComputerRepository.class);
        cdiContainer = RepositorySupport.cdiInitializerWithDefaultEmProducer()
                .initialize();

        producer = cdiContainer.select(PersistenceRepositoryProducer.class).get();
        persistenceDatabaseManager = cdiContainer.select(PersistenceDatabaseManager.class).get();

    }

    @AfterEach
    void tearDown() {
        if (cdiContainer != null) {
            cdiContainer.close();
        }
    }

    @Test
    @DisplayName("should create standard repository")
    void shouldCreateStandardRepository() {
        ComputerRepository repository = producer.get(ComputerRepository.class, persistenceDatabaseManager);
        Assertions.assertThat(repository).isNotNull();
    }


    @Test
    @DisplayName("should create custom repository")
     void shouldCreateCustomRepository() {
        Computers repository = producer.get(Computers.class, persistenceDatabaseManager);
        Assertions.assertThat(repository).isNotNull();
     }

}