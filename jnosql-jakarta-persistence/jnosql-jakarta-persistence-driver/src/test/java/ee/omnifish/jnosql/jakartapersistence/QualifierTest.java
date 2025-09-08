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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;

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

public class QualifierTest {

    private SeContainer cdiContainer;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(QualifiedPersonRepository.class);

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
    void cleanup() {
        cdiContainer.close();
    }

    @Test
    void repositoryCanBeInjectedWithQualifier() {
        var repository = CDI.current().select(QualifiedPersonRepository.class, SpecialRepository.Literal.INSTANCE).get();
        assertThat(repository, notNullValue());
    }

    @Test
    void repositoryCannotBeInjectedWithoutQualifier() {
        assertThrows(Exception.class, () -> {
            CDI.current().select(QualifiedPersonRepository.class).get();
        });
    }
}
