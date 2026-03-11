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

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.metamodel.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map-based implementation of PersistenceUnitCache using ConcurrentHashMap for thread-safe caching.
 * This implementation provides efficient caching of entity metadata and queries at the persistence unit level.
 *
 * <p>Features:
 * <ul>
 *   <li>Thread-safe lazy initialization of entity types using double-checked locking</li>
 *   <li>Concurrent caching of CriteriaQuery objects for SELECT operations</li>
 *   <li>Concurrent caching of processed query strings</li>
 * </ul>
 *
 * @author Ondro Mihalyi
 */
public class MapBasedPersistenceUnitCache implements PersistenceUnitCache {

    /**
     * Lazily initialized map of entity types by name. Uses volatile for fast publication to other threads.
     */
    private volatile Map<String, EntityType<?>> entityTypesByName = null;

    /**
     * Supplier function used to compute entity types map on first access.
     */
    private Supplier<Map<String, EntityType<?>>> entityTypesByNameSupplier;

    /**
     * Thread-safe cache for CriteriaQuery objects indexed by cache keys.
     */
    private Map<Object,CriteriaQuery<?>> selectQueryCache = new ConcurrentHashMap<>();

    /**
     * Thread-safe cache for processed query strings indexed by cache keys.
     */
    private Map<Object,String> stringQueryCache = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * <p>Implementation uses double-checked locking pattern for thread-safe lazy initialization.
     */
    @Override
    public Map<String, EntityType<?>> getEntityTypesByName() {
        Map<String, EntityType<?>> entityTypesByNameLocal = this.entityTypesByName;
        if (entityTypesByNameLocal == null) {
            synchronized (this) {
                entityTypesByNameLocal = this.entityTypesByName;
                if (entityTypesByNameLocal == null) {
                    this.entityTypesByName = entityTypesByNameLocal = entityTypesByNameSupplier.get();
                }
            }
        }
        return entityTypesByNameLocal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEntityTypesByNameSupplier(Supplier<Map<String, EntityType<?>>> supplier) {
        this.entityTypesByNameSupplier = supplier;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses ConcurrentHashMap.computeIfAbsent for atomic cache operations.
     */
    @Override
    public <T> CriteriaQuery<T> getOrCreateSelectQuery(Object key, Function<Object, CriteriaQuery<T>> supplier) {
        CriteriaQuery<?> valueFromCache = selectQueryCache.computeIfAbsent(key, supplier);
        return (CriteriaQuery<T>) valueFromCache;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses ConcurrentHashMap.computeIfAbsent for atomic cache operations.
     */
    @Override
    public String getOrCreateStringQuery(Object key, Function<Object, String> supplier) {
        return stringQueryCache.computeIfAbsent(key, supplier);
    }

}
