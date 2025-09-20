/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.communication;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCache;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCacheProvider;

/**
 * Provides a {@link PersistenceDatabaseManager} for a specific
 * {@link EntityManager}.
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
public class PersistenceDatabaseManagerProvider {

    private Map<String, Map<String, EntityType<?>>> entityTypesByNameForPersistenceUnit;

    @Inject
    private PersistenceUnitCacheProvider persistenceUnitCacheProvider;

    @PostConstruct
    public void init() {
        entityTypesByNameForPersistenceUnit = new ConcurrentHashMap<>();
    }

    private Map<String, EntityType<?>> collectEntityTypesByName(EntityManagerFactory entityManagerFactory) {
        Map<String, EntityType<?>> entityTypesByName = new ConcurrentHashMap<>();
        entityManagerFactory.getMetamodel().getEntities().forEach(type -> {
            entityTypesByName.put(type.getName(), type);
        });
        return entityTypesByName;
    }

    public PersistenceDatabaseManager getManager(EntityManager entityManager) {
        EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        final PersistenceUnitCache persistenceUnitCache = persistenceUnitCacheProvider.getCacheFor(entityManagerFactory);
        persistenceUnitCache.setEntityTypesByNameSupplier(
                () -> collectEntityTypesByName(entityManagerFactory)
        );
        return new PersistenceDatabaseManager(entityManager, persistenceUnitCache);
    }

}
