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

import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SQL-specific implementation of {@link SelectQuery} that adapts a datastore-agnostic query
 * into a structure suitable for SQL execution. It encapsulates sorting, pagination, filtering,
 * entity (table) resolution, and column selection, acting as a bridge between the generic query
 * model and SQL-oriented concerns.
 * The {@code projector} defines the target result type, enabling mapping into entities or
 * projections (e.g., DTOs). This record is immutable; {@link #sorts()} returns an unmodifiable
 * view, and {@link #condition()} safely exposes the optional filtering criteria.
 */
public record SqlSelectQuery(List<Sort<?>> sorts,
                             long limit,
                             long skip,
                             CriteriaCondition criteriaCondition,
                             String entity,
                             List<String> columns, Class<?> projector)  implements SelectQuery {

    @Override
    public String name() {
        return entity;
    }

    @Override
    public Optional<CriteriaCondition> condition() {
        return Optional.ofNullable(criteriaCondition);
    }

    @Override
    public List<Sort<?>> sorts() {
        return Collections.unmodifiableList(sorts);
    }


    /**
     * Creates a {@code SqlSelectQuery} from a generic {@link SelectQuery}.
     * This method adapts a datastore-agnostic query into an SQL-specific representation,
     * preserving all query attributes while introducing a projection target.
     *
     * @param query the source query abstraction
     * @param projector the projection type used to map results
     * @return a new {@code SqlSelectQuery} instance
     * @throws NullPointerException if {@code query} or {@code projector} is {@code null}
     */
    public static  SqlSelectQuery of(SelectQuery query, Class<?> projector) {
        return new SqlSelectQuery(query.sorts(),
                query.limit(),
                query.skip(),
                query.condition().orElse(null),
                query.name(), query.columns(), projector);
    }
}
