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

import jakarta.enterprise.inject.AmbiguousResolutionException;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AmbiguousEntityManagerTest {

    // TODO Assert that an exception is throw at container initialization time, not when the repository is actually created later
    @Test
    void repositoryWithMultipleEntityManagerMethodsThrowsAmbiguousException() {
        Weld weld = new Weld();
        weld.addBeanClass(AmbiguousEntityManagerRepository.class);
        weld.addBeanClass(EntityManagerProducer.class);

        try (WeldContainer container = weld.initialize()) {
            assertThrows(AmbiguousResolutionException.class, () -> {
                AmbiguousEntityManagerRepository repository = container.select(AmbiguousEntityManagerRepository.class).get();
            });
        }
    }
}
