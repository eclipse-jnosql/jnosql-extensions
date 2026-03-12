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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import ee.omnifish.jnosql.jakartapersistence.Person;
import ee.omnifish.jnosql.jakartapersistence.TestJakartaPersistenceClassScanner;
import ee.omnifish.jnosql.jakartapersistence.TestSupport;
import jakarta.enterprise.inject.se.SeContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;


class PersistenceRepositoryProducerTest {

    private SeContainer cdiContainer;

    private ComputerRepository computerRepository;

    private Computers computers;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.customRepositories = Set.of(Computers.class);
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(ComputerRepository.class);
        cdiContainer = RepositorySupport.cdiInitializerWithDefaultEmProducer()
                .initialize();
        this.computerRepository = cdiContainer.select(ComputerRepository.class).get();
        this.computers = cdiContainer.select(Computers.class).get();
    }

    @AfterEach
    void tearDown() {
        if (cdiContainer != null) {
            cdiContainer.close();
        }
    }

    @Test
    void findAll() {
        var computers = computerRepository.findAll().toList();

    }
}