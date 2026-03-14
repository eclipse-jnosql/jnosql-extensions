/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

/**
 * Specialization of {@link SemiStructuredTemplate} that integrates JNoSQL
 * operations with Jakarta Persistence.
 *
 * <p>This interface focuses on persistence operations backed by the Jakarta
 * Persistence API. Implementations expose access to the underlying
 * {@link EntityManager} and {@link PersistenceUnitUtil}, allowing advanced
 * persistence features and metadata inspection to be used alongside the
 * JNoSQL template model.</p>
 *
 * <p>Query execution in this template aims to use the Jakarta Persistence
 * Query Language (JPQL) whenever possible. Depending on the database
 * provider and capabilities, implementations may also rely on the Jakarta
 * Common Query language.</p>
 *
 * <p>The additional methods exposed by this interface provide direct access
 * to persistence infrastructure and extended delete operations.</p>
 */
public interface SqlTemplate extends SemiStructuredTemplate {

    /**
     * Returns the {@link EntityManager} associated with the current persistence context.
     *
     * <p>This method exposes the underlying Jakarta Persistence infrastructure,
     * allowing advanced persistence operations, query execution, and access
     * to the persistence context when required.</p>
     *
     * @return the {@link EntityManager} used by the template
     */
    EntityManager entityManager();

    /**
     * Returns the {@link PersistenceUnitUtil} associated with the persistence unit.
     *
     * <p>This utility can be used to inspect persistence-related metadata,
     * such as checking whether an entity is loaded or retrieving identifier
     * values.</p>
     *
     * @return the {@link PersistenceUnitUtil} of the persistence unit
     */
    PersistenceUnitUtil persistenceUnitUtil();

    /**
     * Deletes entities that match the provided {@link DeleteQuery} and returns
     * the number of affected entities.
     *
     * <p>Unlike {@link #delete(DeleteQuery)}, this method reports the number of
     * entities removed from the datastore.</p>
     *
     * @param query the delete query
     * @return the number of deleted entities
     * @throws NullPointerException if the query is {@code null}
     */
    long deleteWithCount(DeleteQuery query);

    /**
     * Checks whether an entity of the specified type exists with the given identifier.
     *
     * @param <T>  the entity type
     * @param <K>  the identifier type
     * @param type the entity class
     * @param k    the entity identifier
     * @return {@code true} if an entity with the given identifier exists, {@code false} otherwise
     * @throws NullPointerException if {@code type} or {@code k} is {@code null}
     */
    <T, K> boolean existsById(Class<T> type, K k);
}
