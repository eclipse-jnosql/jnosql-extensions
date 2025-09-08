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

import java.util.Set;

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

        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer()
                .addBeanClasses(CallCountInterceptor.class, CallCounter.class)
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
