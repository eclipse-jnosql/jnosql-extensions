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

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;

class SelectQueryParser extends BaseQueryParser {

    public SelectQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    public long count(String entity) {
        EntityType<?> entityType = findEntityType(entity);
        return count(entityType.getJavaType());
    }

    public <T> long count(Class<T> type) {
        TypedQuery<Long> query = buildQuery(type, Long.class, ctx -> ctx.query().select(ctx.builder().count(ctx.root())));
        return query.getSingleResult();
    }

    public <T> Stream<T> findAll(Class<T> type) {
        TypedQuery<T> query = buildQuery(type, type, ctx -> ctx.query().select((Root<T>) ctx.root()));
        return query.getResultStream();
    }

    public <T> Stream<T> query(String query) {
        return buildQuery(query).getResultStream();
    }

    public <T> Stream<T> query(String query, String entity) {
        return query(query);
    }

    public <T> Optional<T> singleResult(String query) {
        return Optional.ofNullable((T) buildQuery(query).getSingleResultOrNull());
    }

    public <T> Optional<T> singleResult(String query, String entity) {
        return singleResult(query);
    }

    public <T, K> Optional<T> find(Class<T> type, K k) {
        return Optional.ofNullable(entityManager().find(type, k));
    }

    public <T> Stream<T> select(SelectQuery selectQuery) {
        final String entityName = selectQuery.name();
        final EntityType<T> entityType = findEntityType(entityName);
        if (selectQuery.condition().isEmpty()) {
            return findAll(entityType.getJavaType());
        } else {
            final CriteriaCondition criteria = selectQuery.condition().get();
            TypedQuery<T> query = buildQuery(entityType.getJavaType(), entityType.getJavaType(), ctx -> {
                CriteriaQuery<T> q = ctx.query().select(ctx.root());
                q = q.where(parseCriteria(criteria, ctx.queryContext()));
                return q;
            });
            return query.getResultStream();
        }
    }

    public <T> Optional<T> singleResult(SelectQuery selectQuery) {
        final String entityName = selectQuery.name();
        final EntityType<T> entityType = findEntityType(entityName);
        final Class<T> type = entityType.getJavaType();
        if (selectQuery.condition().isEmpty()) {
            TypedQuery<T> query = buildQuery(type, type, ctx -> ctx.query().select((Root<T>) ctx.root()));
            return Optional.ofNullable(query.getSingleResultOrNull());
        } else {
            final CriteriaCondition criteria = selectQuery.condition().get();
            TypedQuery<T> query = buildQuery(type, type, ctx -> {
                CriteriaQuery<T> q = ctx.query().select(ctx.root());
                q = q.where(parseCriteria(criteria, ctx.queryContext()));
                return q;
            });
            return Optional.ofNullable(query.getSingleResultOrNull());
        }
    }

    public long count(SelectQuery selectQuery) {
        final String entityName = selectQuery.name();
        if (selectQuery.condition().isEmpty()) {
            return count(entityName);
        } else {
            final EntityType<?> entityType = findEntityType(entityName);
            final CriteriaCondition criteria = selectQuery.condition().get();
            TypedQuery<Long> query = buildQuery(entityType.getJavaType(), Long.class, ctx -> {
                CriteriaQuery<Long> q = ctx.query().select(ctx.builder().count(ctx.root()));
                q = q.where(parseCriteria(criteria, ctx.queryContext()));
                return q;
            });
            return query.getSingleResult();
        }
    }

    public Query buildQuery(String query) {
        if (query.startsWith("WHERE")) {
            query = "SELECT this FROM Coordinate " + query;
        }
        EntityManager em = entityManager();
        return em.createQuery(query);
    }

    private <FROM, RESULT> TypedQuery<RESULT> buildQuery(Class<FROM> fromType, Class<RESULT> resultType,
            Function<SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> queryModifier) {
        EntityManager em = entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<RESULT> criteriaQuery = criteriaBuilder.createQuery(resultType);
        Root<FROM> from = criteriaQuery.from(fromType);
        criteriaQuery = queryModifier.apply(
                new SelectQueryContext(criteriaQuery,
                        new QueryContext(from, criteriaBuilder)));
        return em.createQuery(criteriaQuery);
    }

    record SelectQueryContext<FROM, RESULT>(CriteriaQuery<RESULT> query, QueryContext<FROM> queryContext) {

        public Root<FROM> root() {
            return queryContext.root();
        }

        public CriteriaBuilder builder() {
            return queryContext.builder();
        }

    }

}
