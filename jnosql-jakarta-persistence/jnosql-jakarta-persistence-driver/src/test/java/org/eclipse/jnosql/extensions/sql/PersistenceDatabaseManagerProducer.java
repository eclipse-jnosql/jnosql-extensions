/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;

@ApplicationScoped
public class PersistenceDatabaseManagerProducer {

    private final  PersistenceDatabaseManager persistenceDatabaseManager;

    @Inject
    public PersistenceDatabaseManagerProducer(EntityManagerProvider entityManagerProvider,
                                              PersistenceDatabaseManagerProvider persistenceDatabaseManagerProvider) {

        EntityManager entityManager = entityManagerProvider.produceMatchingEntityManager(null, null).orElseThrow();
        this.persistenceDatabaseManager = persistenceDatabaseManagerProvider.getManager(entityManager);
    }

    @Produces
    public PersistenceDatabaseManager getPersistenceDatabaseManager() {
        return persistenceDatabaseManager;
    }
}
