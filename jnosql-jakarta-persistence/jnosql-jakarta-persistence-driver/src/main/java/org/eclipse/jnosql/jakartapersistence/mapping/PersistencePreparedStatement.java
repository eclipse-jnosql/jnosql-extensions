/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.persistence.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;

/**
 *
 * @author Ondro Mihalyi
 */
public class PersistencePreparedStatement implements PreparedStatement {

    private final String queryString;
    private final BaseQueryParser queryParser;
    private final Map<String, Object> parameters = new HashMap<>();
    private String entity = null;
    private UnaryOperator<SelectQuery> selectMapper;

    PersistencePreparedStatement(String queryString, final BaseQueryParser queryParser) {
        this.queryParser = queryParser;
        this.queryString = queryString;
    }

    PersistencePreparedStatement(String queryString, final BaseQueryParser selectParser, String entity) {
        this(queryString, selectParser);
        this.entity = entity;
    }

    private void applyParameters(Query query) {
        parameters.forEach((name, value) -> {
            if (name.startsWith("?")) {
                var position = Integer.parseInt(name, 1, name.length(), 10);
                query.setParameter(position, value);
            } else {
                query.setParameter(name, value);
            }
        });
    }

    @Override
    public PreparedStatement bind(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    @Override
    public <T> Stream<T> result() {
        if (queryParser instanceof SelectQueryParser selectParser) {
            return selectParser.query(queryString, entity, this.selectMapper, this::applyParameters);
        } else {
            return queryParser.query(queryString, entity, this::applyParameters);
        }
    }

    @Override
    public <T> Optional<T> singleResult() {
        Query query = queryParser.buildQuery(queryString, entity);
        applyParameters(query);
        return Optional.ofNullable((T) query.getSingleResultOrNull())
                .map(this::refreshEntity);
    }

    private <T> T refreshEntity(T entity) {
        queryParser.entityManager().refresh(entity);
        return entity;
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCount() {
        return false;
    }

    public void setSelectMapper(UnaryOperator<SelectQuery> selectMapper) {
        this.selectMapper = selectMapper;
    }
}
