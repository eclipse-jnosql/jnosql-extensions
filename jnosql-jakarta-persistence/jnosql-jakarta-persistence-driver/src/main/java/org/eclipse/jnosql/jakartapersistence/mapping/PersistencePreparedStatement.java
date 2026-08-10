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

import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.persistence.Query;

import java.util.Collection;
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
    private Limit limit;
    private Collection<Sort<?>> sorts;

    PersistencePreparedStatement(String queryString, final BaseQueryParser queryParser) {
        this.queryParser = queryParser;
        this.queryString = queryString;
    }

    PersistencePreparedStatement(String queryString, final BaseQueryParser selectParser, String entity) {
        this(queryString, selectParser);
        this.entity = entity;
    }

    @Override
    public PreparedStatement bind(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    @Override
    public <T> Stream<T> result() {
        if (queryParser instanceof SelectQueryParser selectParser) {
            return selectParser.query(queryString, entity, sorts, query -> {
                applyParameters(query);
                applyProjections(query);
            });
        } else {
            return queryParser.query(queryString, entity, sorts, this::applyParameters);
        }
    }

    @Override
    public <T> Optional<T> singleResult() {
        Query query = queryParser.buildQuery(queryString, entity);
        applyParameters(query);
        applyProjections(query);
        return Optional.ofNullable((T) query.getSingleResultOrNull())
                .map(this::refreshEntity);
    }

    private <T> T refreshEntity(T entity) {
        try {
            queryParser.entityManager().getMetamodel().entity(entity.getClass());
        } catch (IllegalArgumentException e) {
            return entity;
        }
        queryParser.entityManager().refresh(entity);
        return entity;
    }

    @Override
    public long count() {
        if (isCountQuery()) {
            Query query = QLUtil.hasFromClause(queryString)
                    ? queryParser.entityManager().createQuery(queryString)
                    : queryParser.buildQuery(queryString, entity);
            applyParameters(query);
            return ((Number) query.getSingleResult()).longValue();
        }

        if (queryParser instanceof DeleteQueryParser) {
            Query query = queryParser.buildQuery(queryString, entity);
            applyParameters(query);
            return query.executeUpdate();
        }

        throw new IllegalArgumentException("The count operation is only allowed for COUNT and DELETE queries");
    }

    @Override
    public boolean isCount() {
        return isCountQuery() || queryParser instanceof DeleteQueryParser;
    }

    private boolean isCountQuery() {
        return QLUtil.isCountQuery(queryString);
    }

    public void setSelectMapper(UnaryOperator<SelectQuery> selectMapper) {
        this.selectMapper = selectMapper;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    // TODO Apply sorts to the text query
    public void setSorts(Collection<Sort<?>> sorts) {
        this.sorts = sorts;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getEntity() {
        return entity;
    }

    public Collection<Sort<?>> getSorts() {
        return sorts;
    }

    public <T> Page<T> selectOffset(PageRequest pageRequest) {
        if (queryParser instanceof SelectQueryParser selectParser) {
            return selectParser.selectOffset(pageRequest, this.queryString, this.entity, this.sorts,
                    query -> {
                        applyParameters(query);
                        applyProjections(query);
                    },
                    query -> {
                        applyParameters(query);
                    });
        } else {
            throw new IllegalStateException("The current parser " + queryParser.getClass() + " is not suitable for select queries. Likely a defect to fix.");
        }
    }

    void applyParameters(Query query) {
        parameters.forEach((name, value) -> {
            if (name.startsWith("?")) {
                var position = Integer.parseInt(name, 1, name.length(), 10);
                query.setParameter(position, value);
            } else {
                query.setParameter(name, value);
            }
        });
    }

    void applyProjections(Query query) {
        if (limit != null) {
            query.setFirstResult(Math.toIntExact(limit.startAt()) - 1);
            query.setMaxResults(limit.maxResults());
        }
    }

}
