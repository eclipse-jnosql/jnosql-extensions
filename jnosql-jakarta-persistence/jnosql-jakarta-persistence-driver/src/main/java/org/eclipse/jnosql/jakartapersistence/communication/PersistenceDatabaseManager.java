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

// TODO Cache metadata for the same persistence unit
public class PersistenceDatabaseManager {

    private final EntityManager em;

    private final PersistenceUnitCache persistenceUnitCache;

    public PersistenceDatabaseManager(EntityManager em, PersistenceUnitCache persistenceUnitCache) {
        this.em = em;
        this.persistenceUnitCache = persistenceUnitCache;
    }

    public EntityManager getEntityManager() {
        return em;
    }

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

    public EntitiesMetadata getEntitiesMetadata() {
        return new JakataPersistenceEntitiesMetadata(this);
    }
}
