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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache interface for persistence unit-level caching of metadata and queries.
 * This cache is shared across all EntityManagers within the same EntityManagerFactory
 * to improve performance by avoiding repeated computation of expensive operations.
 *
 * @author Ondro Mihalyi
 */
public interface PersistenceUnitCache {

    /**
     * Retrieves a map of entity types indexed by their simple names.
     * Uses lazy initialization with double-checked locking pattern. The supplier must be set first,
     * using the {@link #setEntityTypesByNameSupplier(java.util.function.Supplier) } method.
     *
     * @return map of entity names to their corresponding EntityType metadata
     */
    Map<String, EntityType<?>> getEntityTypesByName();

    /**
     * Sets the supplier function used to compute entity types by name.
     * This supplier is called only once when the cache is first accessed.
     *
     * @param supplier function that computes the entity types map
     */
    void setEntityTypesByNameSupplier(Supplier<Map<String, EntityType<?>>> supplier);

    /**
     * Retrieves or creates a cached CriteriaQuery for SELECT operations.
     * Uses the provided key for cache lookup and the supplier to create new queries.
     *
     * @param <T> the result type of the query
     * @param key cache key used to identify the query
     * @param supplier function to create the query if not found in cache
     * @return cached or newly created CriteriaQuery
     */
    <T> CriteriaQuery<T> getOrCreateSelectQuery(Object key, Function<Object, CriteriaQuery<T>> supplier);

    /**
     * Retrieves or creates a cached string query.
     * Used for caching processed query strings to avoid repeated parsing.
     *
     * @param key cache key used to identify the query string
     * @param supplier function to create the query string if not found in cache
     * @return cached or newly created query string
     */
    String getOrCreateStringQuery(Object key, Function<Object, String> supplier);
}
