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

    EntityManager entityManager();

    PersistenceUnitUtil persistenceUnitUtil();

    long deleteWithCount(DeleteQuery query);
}
