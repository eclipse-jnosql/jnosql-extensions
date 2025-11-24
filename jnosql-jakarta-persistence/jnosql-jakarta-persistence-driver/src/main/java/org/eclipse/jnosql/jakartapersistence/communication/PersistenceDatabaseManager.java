/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.jakartapersistence.communication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCache;

/**
 * Database manager that provides access to Jakarta Persistence EntityManager
 * and associated caching functionality. This class serves as a bridge between
 * the JNoSQL framework and Jakarta Persistence, providing cached access to
 * entity metadata and query objects.
 */
// TODO Cache metadata for the same persistence unit
public class PersistenceDatabaseManager {

    private final EntityManager em;

    private final PersistenceUnitCache persistenceUnitCache;

    /**
     * Constructs a new PersistenceDatabaseManager with the specified EntityManager and cache.
     *
     * @param em the EntityManager to use for persistence operations
     * @param persistenceUnitCache the cache instance for this persistence unit
     */
    public PersistenceDatabaseManager(EntityManager em, PersistenceUnitCache persistenceUnitCache) {
        this.em = em;
        this.persistenceUnitCache = persistenceUnitCache;
    }

    /**
     * Returns the underlying EntityManager for direct JPA operations.
     *
     * @return the EntityManager instance
     */
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Finds an EntityType by entity name, with fallback to cached metadata lookup.
     * This method first attempts to use the standard JPA metamodel lookup,
     * and if that fails (e.g., with EclipseLink requiring full class names),
     * it falls back to the cached entity types by name.
     *
     * @param <T> the entity type
     * @param entityName the simple name of the entity
     * @return the EntityType metadata for the specified entity
     * @throws IllegalArgumentException if the entity is not found
     */
    public <T> EntityType<T> findEntityType(String entityName) {
        try {
            return (EntityType<T>) em.getMetamodel().entity(entityName);
        } catch (IllegalArgumentException e) {
            // EclipseLink expects full class name in MM.entity() method. We need to find out the type otherwise
            EntityType<?> entityType = persistenceUnitCache.getEntityTypesByName().get(entityName);
            if (entityType != null) {
                return (EntityType<T>)entityType;
            } else {
                final IllegalArgumentException ex = new IllegalArgumentException("Entity with name " + entityName + " not found in the list of known entities");
                ex.addSuppressed(e);
                throw ex;
            }
        }
    }

    /**
     * Returns the entities metadata wrapper for this database manager.
     *
     * @return EntitiesMetadata instance providing access to entity information
     */
    public EntitiesMetadata getEntitiesMetadata() {
        return new JakartaPersistenceEntitiesMetadata(this);
    }

    /**
     * Returns the persistence unit cache associated with this database manager.
     *
     * @return the PersistenceUnitCache instance
     */
    public PersistenceUnitCache getPersistenceUnitCache() {
        return persistenceUnitCache;
    }

    
}
