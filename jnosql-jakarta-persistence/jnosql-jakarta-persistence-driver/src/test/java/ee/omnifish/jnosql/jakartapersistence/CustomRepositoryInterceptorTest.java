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
import static org.hamcrest.Matchers.equalTo;

class CustomRepositoryInterceptorTest {

    private SeContainer cdiContainer;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.customRepositories = Set.of(CustomPersonRepository.class);

        cdiContainer = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addPackages(Converters.class, EntityConverter.class)
                .addPackages(Reflections.class)
                .addExtensions(ReflectionEntityMetadataExtension.class, JakartaPersistenceExtension.class)
                .addPackages(PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class)
                .addBeanClasses(EntityManagerProducer.class, CallCountInterceptor.class, CallCounter.class)
                .initialize();
    }

    @AfterEach
    void tearDown() {
        if (cdiContainer != null) {
            cdiContainer.close();
        }
    }

    @Test
    void customRepositorySupportsInterceptors() {
        CustomPersonRepository repository = cdiContainer.select(CustomPersonRepository.class).get();
        CallCounter callCounter = cdiContainer.select(CallCounter.class).get();

        callCounter.reset();

        // Call method with both class-level and method-level interceptor
        repository.findAll();
        assertThat(callCounter.getCallCount(), equalTo(1));

        // Call method with only class-level interceptor
        repository.findByName("Data");
        assertThat(callCounter.getCallCount(), equalTo(2));
    }
}
