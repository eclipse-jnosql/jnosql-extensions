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

import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.DeploymentException;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


class AmbiguousEntityManagerTest {

    // TODO Assert that an exception is throw at container initialization time, not when the repository is actually created later
    @Test
    void repositoryWithMultipleEntityManagerMethodsThrowsAmbiguousException() {

        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(AmbiguousEntityManagerRepository.class);

        final SeContainerInitializer cdiInitializer = TestSupport.cdiInitializerWithDefaultEmProducer();
        assertThrows(DeploymentException.class, () -> {
            cdiInitializer.initialize();
        });
    }
}
