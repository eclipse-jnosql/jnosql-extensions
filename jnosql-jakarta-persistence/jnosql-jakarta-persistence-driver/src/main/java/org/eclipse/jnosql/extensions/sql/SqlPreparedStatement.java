/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.data.exceptions.NonUniqueResultException;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


/**
 * This class represents a SQL-specific implementation of {@link PreparedStatement}. It provides methods for binding
 * query parameters, retrieving results, and determining if the query is a count query.
 *
 * <p>This implementation is designed to integrate with Jakarta Persistence and JNoSQL
 * to support SQL-based query execution in a type-safe manner.</p>
 *
 * <p>Methods marked with {@link Override} provide concrete implementations of the
 * {@link PreparedStatement} interface.</p>
 *
 * <p>Note: This class does not execute actual queries or interact with a database;
 * rather, it serves as a minimal implementation framework for managing SQL queries in preparation for execution.</p>
 */
public final class SqlPreparedStatement implements PreparedStatement {

    private static final UnaryOperator<SelectQuery> SELECT_MAPPER_DEFAULT = s -> s;

    private final SelectQuery selectQuery;

    private final DeleteQuery deleteQuery;

    private final UpdateQuery updateQuery;

    private final CommunicationPreparedStatement.PreparedStatementType type;

    private final Params params;

    private final String query;

    private final List<String> paramsLeft;

    private final SqlTemplate manager;

    private UnaryOperator<SelectQuery> selectMapper = SELECT_MAPPER_DEFAULT;

    private SqlPreparedStatement(SelectQuery selectQuery, DeleteQuery deleteQuery,
                                 UpdateQuery updateQuery,
                                 CommunicationPreparedStatement.PreparedStatementType type,
                                 Params params,
                                 String query,
                                 List<String> paramsLeft,
                                 SqlTemplate manager) {

        this.selectQuery = selectQuery;
        this.deleteQuery = deleteQuery;
        this.updateQuery = updateQuery;
        this.type = type;
        this.params = params;
        this.query = query;
        this.paramsLeft = paramsLeft;
        this.manager = manager;
    }

    @Override
    public PreparedStatement bind(String name, Object value) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(value, "value is required");

        paramsLeft.remove(name);
        params.bind(name, value);
        return this;
    }

    public PreparedStatement bind(int index, Object value) {
        Objects.requireNonNull(value, "value is required");

        if (index < 1) {
            throw new IllegalArgumentException("The index should be greater than zero");
        }

        var name = "?" + index;
        paramsLeft.remove("?" + index);
        params.bind(name, value);
        return this;
    }

    public Optional<SelectQuery> select() {
        return Optional.ofNullable(selectQuery);
    }

    public CommunicationPreparedStatement.PreparedStatementType getType() {
        return type;
    }

    @Override
    public <T> Stream<T> result() {
        if (!paramsLeft.isEmpty()) {
            throw new QueryException("Check all the parameters before execute the query, params left: " + paramsLeft);
        }
        switch (type) {
            case SELECT -> {
                return manager.select(operator().apply(selectQuery));
            }
            case DELETE -> {
                manager.delete(deleteQuery);
                return Stream.empty();
            }
            case UPDATE -> {
                manager.update(updateQuery);
                return Stream.empty();
            }
            default -> throw new UnsupportedOperationException("there is not support to operation type: " + type);
        }
    }

    @Override
    public <T> Optional<T> singleResult() {
        Stream<T> entities = result();
        final Iterator<T> iterator = entities.iterator();

        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        final T next = iterator.next();
        if (!iterator.hasNext()) {
            return Optional.of(next);
        }
        throw new NonUniqueResultException("The select returns more than one entity, select: " + query);
    }

    @Override
    public long count() {
        if (!paramsLeft.isEmpty()) {
            throw new QueryException("Check all the parameters before execute the query, params left: " + paramsLeft);
        }
        if (CommunicationPreparedStatement.PreparedStatementType.COUNT.equals(type)) {
            return manager.count(selectQuery);
        }
        throw new IllegalArgumentException("The count operation is only allowed for COUNT queries");
    }

    @Override
    public boolean isCount() {
        return false;
    }

    public UnaryOperator<SelectQuery> operator() {
        return this.selectMapper;
    }

    public void setSelectMapper(UnaryOperator<SelectQuery> selectMapper) {
        Objects.requireNonNull(selectMapper, "selectMapper is required");
        this.selectMapper = selectMapper;
    }

    static SqlPreparedStatement select(
            SelectQuery selectQuery,
            Params params,
            String query,
            SqlTemplate template) {
        if (selectQuery.isCount()) {
            return new SqlPreparedStatement(selectQuery,
                    null, null, CommunicationPreparedStatement.PreparedStatementType.COUNT, params, query,
                    params.getParametersNames(), template);
        } else {
            return new SqlPreparedStatement(selectQuery,
                    null, null, CommunicationPreparedStatement.PreparedStatementType.SELECT, params, query,
                    params.getParametersNames(), template);
        }

    }

    static SqlPreparedStatement delete(DeleteQuery deleteQuery,
                                       Params params,
                                       String query,
                                       SqlTemplate template) {

        return new SqlPreparedStatement(null,
                deleteQuery, null, CommunicationPreparedStatement.PreparedStatementType.DELETE, params,
                query,
                params.getParametersNames(),
                template);

    }

    static SqlPreparedStatement update(UpdateQuery updateQuery,
                                       Params params,
                                       String query,
                                       SqlTemplate template) {
        return new SqlPreparedStatement(null, null,
                updateQuery,
                CommunicationPreparedStatement.PreparedStatementType.UPDATE, params, query,
                params.getParametersNames(), template);

    }
}
