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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.persistence.EntityManager;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;

abstract class PersistenceRepository<T, K> extends AbstractRepository<T, K> implements NoSQLRepository<T, K> {

    /**
     * Retrieves the {@link EntityManager} instance associated with the repository.
     *
     * @return the {@code EntityManager} responsible for managing the persistence context.
     */
    abstract EntityManager entityManager();
}
