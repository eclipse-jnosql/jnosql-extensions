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

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.CDI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QualifierTest {

    private SeContainer cdiContainer;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(QualifiedPersonRepository.class);

        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer()
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
        assertThrows(Exception.class, () -> CDI.current().select(QualifiedPersonRepository.class).get());
    }
}
