/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Ondro Mihalyi
 */
package ee.omnifish.jnosql.jakartapersistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import jakarta.enterprise.inject.se.SeContainer;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InterceptorTest {

    private SeContainer cdiContainer;
    private CountedPersonRepository repository;
    private CallCounter callCounter;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(CountedPersonRepository.class);

        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer()
                .addBeanClasses(CallCountInterceptor.class, CallCounter.class)
                .initialize();

        repository = cdiContainer.select(CountedPersonRepository.class).get();
        callCounter = cdiContainer.select(CallCounter.class).get();
        callCounter.reset();
    }

    @AfterEach
    void cleanup() {
        cdiContainer.close();
    }

    @Test
    void interceptorCountsMethodCalls() {
        assertThat(callCounter.getCallCount(), is(0));

        repository.countAll();

        assertThat(callCounter.getCallCount(), greaterThan(0));
    }
}
