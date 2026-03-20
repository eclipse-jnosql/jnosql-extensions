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

import java.util.Objects;
import java.util.function.Function;

/**
 * Factory responsible for creating {@link SqlTemplate} instances from a given {@link EntityManager}.
 *
 * <p>This class acts as an integration point between the Jakarta Persistence API and
 * the JNoSQL SQL extension, adapting a JPA {@link EntityManager} into a {@link SqlTemplate}
 * that follows the JNoSQL {@code SemiStructuredTemplate} model.</p>
 *
 * <p>The produced {@link SqlTemplate} enables repository-style operations and query execution
 * using JNoSQL abstractions while delegating persistence operations to the underlying
 * Jakarta Persistence provider (e.g., Hibernate or EclipseLink).</p>
 *
 * <p>This factory is stateless and thread-safe, and can be reused across multiple invocations.</p>
 *
 * @see SqlTemplate
 * @see EntityManager
 */
public class SqlTemplateFactory {

    /**
     * Creates a {@link SqlTemplate} backed by the provided {@link EntityManager}.
     *
     * <p>The returned template delegates persistence operations to the given
     * {@link EntityManager}, allowing JNoSQL-style interactions with relational databases.</p>
     *
     * @param entityManager the JPA {@link EntityManager} used for persistence operations
     * @return a {@link SqlTemplate} instance associated with the provided entity manager
     * @throws NullPointerException if {@code entityManager} is {@code null}
     */
    public SqlTemplate create(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager is required");
        return DefaultSqlTemplate.of(entityManager);
    }

}
