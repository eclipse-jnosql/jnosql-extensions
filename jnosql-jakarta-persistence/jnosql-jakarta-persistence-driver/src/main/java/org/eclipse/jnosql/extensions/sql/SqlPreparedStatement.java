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

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.QueryParams;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


/**
 * This class represents a SQL-specific implementation of {@link PreparedStatement}.
 * It provides methods for binding query parameters, retrieving results, and determining
 * if the query is a count query.
 *
 * <p>This implementation is designed to integrate with Jakarta Persistence and JNoSQL
 * to support SQL-based query execution in a type-safe manner.</p>
 *
 * <p>Methods marked with {@link Override} provide concrete implementations of the
 * {@link PreparedStatement} interface.</p>
 *
 * <p>Note: This class does not execute actual queries or interact with a database;
 * rather, it serves as a minimal implementation framework for managing SQL queries
 * in preparation for execution.</p>
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
        return null;
    }

    @Override
    public <T> Stream<T> result() {
        return Stream.empty();
    }

    @Override
    public <T> Optional<T> singleResult() {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean isCount() {
        return false;
    }
}
