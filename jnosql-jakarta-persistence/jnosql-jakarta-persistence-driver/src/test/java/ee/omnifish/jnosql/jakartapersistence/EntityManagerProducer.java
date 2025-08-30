/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class EntityManagerProducer {
    @Produces
    @ApplicationScoped
    public EntityManager createEntityManager() {
        return Persistence.createEntityManagerFactory("testPersistenceUnit")
                .createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager entityManager) {
        entityManager.close();
    }

    @Produces
    @SpecialEntityManager
    @ApplicationScoped
    public EntityManager createSpecialEntityManager() {
        return Persistence.createEntityManagerFactory("testPersistenceUnit")
                .createEntityManager();
    }

    public void closeSpecialEntityManager(@Disposes @SpecialEntityManager EntityManager entityManager) {
        entityManager.close();
    }
}
