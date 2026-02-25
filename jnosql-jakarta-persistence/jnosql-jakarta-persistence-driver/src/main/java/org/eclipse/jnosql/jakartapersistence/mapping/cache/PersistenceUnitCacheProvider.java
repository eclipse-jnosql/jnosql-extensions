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
package org.eclipse.jnosql.jakartapersistence.mapping.cache;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * CDI-managed provider for PersistenceUnitCache instances.
 * This application-scoped bean manages cache instances per persistence unit,
 * ensuring that each EntityManagerFactory gets its own dedicated cache.
 *
 * <p>The provider uses the persistence unit name as the cache key to maintain
 * separate cache instances for different persistence units within the same application.
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
public class PersistenceUnitCacheProvider {

    /**
     * Map of cache instances indexed by persistence unit names.
     * Uses ConcurrentHashMap for thread-safe access across multiple threads.
     */
    private ConcurrentHashMap<String, PersistenceUnitCache> caches;

    @PostConstruct
    public void init() {
       caches = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves or creates a cache instance for the specified EntityManagerFactory.
     * Uses the persistence unit name as the cache key to ensure proper isolation
     * between different persistence units.
     *
     * @param entityManagerFactory the EntityManagerFactory to get cache for
     * @return dedicated PersistenceUnitCache instance for the persistence unit
     */
    public PersistenceUnitCache getCacheFor(EntityManagerFactory entityManagerFactory) {
        return caches.computeIfAbsent(entityManagerFactory.getName(), this::createQueryCache);
    }

    /**
     * Creates a new cache instance for the specified persistence unit.
     * Currently returns a MapBasedPersistenceUnitCache implementation.
     *
     * @param persistenceUnitName name of the persistence unit
     * @return new PersistenceUnitCache instance
     */
    private PersistenceUnitCache createQueryCache(String persistenceUnitName) {
        return new MapBasedPersistenceUnitCache();
    }

}
