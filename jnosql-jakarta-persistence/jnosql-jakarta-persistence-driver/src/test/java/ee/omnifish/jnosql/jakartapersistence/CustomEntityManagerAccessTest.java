/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Ondro Mihalyi
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.persistence.EntityManager;

import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

class CustomEntityManagerAccessTest {

    private SeContainer cdiContainer;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.customRepositories = Set.of(
                CustomEntityManagerAccessRepository.class, CustomQualifiedEntityManagerRepository.class);

        cdiContainer = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addPackages(Converters.class, EntityConverter.class)
                .addPackages(Reflections.class)
                .addExtensions(ReflectionEntityMetadataExtension.class, JakartaPersistenceExtension.class)
                .addPackages(PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class)
                .addBeanClasses(EntityManagerProducer.class)
                .initialize();
    }

    @AfterEach
    void tearDown() {
        if (cdiContainer != null) {
            cdiContainer.close();
        }
    }

    @Test
    void customRepositoryReturnsEntityManager() {
        CustomEntityManagerAccessRepository repository = cdiContainer.select(CustomEntityManagerAccessRepository.class).get();
        EntityManager defaultEntityManager = cdiContainer.select(EntityManager.class).get();

        EntityManager repositoryEntityManager = repository.getEntityManager();

        assertThat(repositoryEntityManager, notNullValue());
        assertThat(repositoryEntityManager, sameInstance(defaultEntityManager));
    }

    @Test
    void customRepositoryReturnsQualifiedEntityManager() {
        CustomQualifiedEntityManagerRepository repository = cdiContainer.select(CustomQualifiedEntityManagerRepository.class).get();
        EntityManager specialEntityManager = cdiContainer.select(EntityManager.class, SpecialEntityManager.Literal.INSTANCE).get();

        EntityManager repositoryEntityManager = repository.getEntityManager();

        assertThat(repositoryEntityManager, notNullValue());
        assertThat(repositoryEntityManager, sameInstance(specialEntityManager));
    }
}
