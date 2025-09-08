/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
 *   Ondro Mihalyi
 */
package ee.omnifish.jnosql.jakartapersistence;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.persistence.EntityManager;

import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityManagerAccessTest {

    private SeContainer cdiContainer;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(
                EntityManagerAccessRepository.class, QualifiedEntityManagerRepository.class);

        cdiContainer = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addExtensions(JakartaPersistenceExtension.class)
                .addPackages(PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class)
                .addBeanClasses(EntityManagerProducer.class)
                .initialize();

    }

    @AfterEach
    void cleanup() {
        cdiContainer.close();
    }

    @Test
    void repositoryReturnsEntityManager() {
        EntityManagerAccessRepository repository = cdiContainer.select(EntityManagerAccessRepository.class).get();
        EntityManager entityManager = cdiContainer.select(EntityManager.class).get();
        EntityManager repositoryEntityManager = repository.getEntityManager();

        assertThat(repositoryEntityManager, notNullValue());
        assertThat(repositoryEntityManager, sameInstance(entityManager));
    }

    @Test
    void repositoryReturnsQualifiedEntityManager() {
        QualifiedEntityManagerRepository qualifiedRepository = cdiContainer.select(QualifiedEntityManagerRepository.class).get();
        EntityManager specialEntityManager = cdiContainer.select(EntityManager.class, SpecialEntityManager.Literal.INSTANCE).get();

        EntityManager repositoryEntityManager = qualifiedRepository.getEntityManager();

        assertThat(repositoryEntityManager, notNullValue());
        assertThat(repositoryEntityManager, sameInstance(specialEntityManager));
    }
}
