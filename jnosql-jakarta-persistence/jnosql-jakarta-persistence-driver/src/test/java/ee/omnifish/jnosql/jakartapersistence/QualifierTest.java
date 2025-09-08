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
import jakarta.enterprise.inject.spi.CDI;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertThrows(Exception.class, () -> {
            CDI.current().select(QualifiedPersonRepository.class).get();
        });
    }
}
