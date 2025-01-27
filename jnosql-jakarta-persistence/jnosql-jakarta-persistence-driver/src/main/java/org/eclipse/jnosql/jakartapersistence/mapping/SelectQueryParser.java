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

import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.core.PersistencePage;

import static org.eclipse.jnosql.jakartapersistence.mapping.BaseQueryParser.parseCriteria;

class SelectQueryParser extends BaseQueryParser {

    public SelectQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    private static interface QueryModifier<FROM, RESULT> extends Function<SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> {

        static <ENTITY> QueryModifier<ENTITY, ENTITY> findAll() {
            return ctx -> ctx.query().select((Root<ENTITY>) ctx.root());
        }

        static <ENTITY> QueryModifier<ENTITY, ENTITY> selectWhere(CriteriaCondition criteria) {
            return ctx -> {
                CriteriaQuery<ENTITY> query = ctx.query().select(ctx.root());
                query = query.where(parseCriteria(criteria, ctx.queryContext()));
                return query;
            };
        }

        static <ENTITY> QueryModifier<ENTITY, ENTITY> applySorts(List<Sort<?>> sorts) {
            return ctx -> {
                CriteriaQuery<ENTITY> query = ctx.query();
                if (sorts != null && !sorts.isEmpty()) {
                    List<Order> orders = new ArrayList<>();
                    for (Sort sort : sorts) {
                        // Handle nested properties (e.g., "category.name")
                        Path<?> path = ctx.root();
                        String[] fields = sort.property().split("\\.");
                        for (String field : fields) {
                            path = path.get(getFieldName(field));
                        }
                        // Create order based on direction
                        Order order = sort.isAscending()
                                ? ctx.builder().asc(path)
                                : ctx.builder().desc(path);
                        orders.add(order);
                    }
                    query.select(ctx.root()).orderBy(orders);
                }

                // Apply sorting to query
                return query;
            };
        }

        static <ENTITY> QueryModifier<ENTITY, ENTITY> combine(QueryModifier<ENTITY, ENTITY>... modifiers) {
            return ctx -> {
                CriteriaQuery<ENTITY> query = ctx.query();
                for (QueryModifier<ENTITY, ENTITY> modifier : modifiers) {
                    query = modifier.apply(new SelectQueryContext<>(query, ctx.queryContext()));
                }
                return query;
            };
        }

        static <ENTITY> QueryModifier<ENTITY, Long> countAll() {
            return ctx -> ctx.query().select(ctx.builder().count(ctx.root()));
        }

        static <ENTITY> QueryModifier<ENTITY, Long> countWhere(CriteriaCondition criteria) {
            return ctx -> {
                CriteriaQuery<Long> query = ctx.query().select(ctx.builder().count(ctx.root()));
                query = query.where(parseCriteria(criteria, ctx.queryContext()));
                return query;
            };
        }

    }

    public long count(String entity) {
        final Class<?> type = entityTypeFromEntityName(entity);
        return count(type);
    }

    public <T> long count(Class<T> type) {
        TypedQuery<Long> query = buildQuery(type, Long.class, QueryModifier.countAll());
        return query.getSingleResult();
    }

    public <T> Stream<T> findAll(Class<T> type) {
        return buildQuery(type, type, QueryModifier.findAll())
                .getResultStream();
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
        final TypedQuery<T> query = getSelectTypedQuery(selectQuery);
        return query.getResultStream();
    }

    private <T> TypedQuery<T> getSelectTypedQuery(SelectQuery selectQuery) {
        Class<T> type = entityTypeFromEntityName(selectQuery.name());
        TypedQuery<T> query;
        if (selectQuery.condition().isEmpty()) {
            query = buildQuery(type, type, QueryModifier.combine(
                    QueryModifier.findAll(),
                    QueryModifier.applySorts(selectQuery.sorts())
            ));
        } else {
            final CriteriaCondition criteria = selectQuery.condition().get();
            query = buildQuery(type, type, QueryModifier.combine(
                    QueryModifier.selectWhere(criteria),
                    QueryModifier.applySorts(selectQuery.sorts())
            ));
        }
        if (selectQuery.limit() > 0) {
            try {
                query.setMaxResults(Math.toIntExact(selectQuery.limit()));
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("The limit:" + selectQuery.limit() + " is too big, query: " + selectQuery, e);
            }
        }
        if (selectQuery.skip() > 0) {
            try {
                query.setFirstResult(Math.toIntExact(selectQuery.skip()));
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("The skip:" + selectQuery.skip() + " is too big, query: " + selectQuery, e);
            }
        }
        return query;
    }

    public <T> Optional<T> singleResult(SelectQuery selectQuery) {
        final Class<T> type = entityTypeFromEntityName(selectQuery.name());
        if (selectQuery.condition().isEmpty()) {
            TypedQuery<T> query = buildQuery(type, type, ctx -> ctx.query().select((Root<T>) ctx.root()));
            return Optional.ofNullable(toDataExceptions(query::getSingleResultOrNull));
        } else {
            final CriteriaCondition criteria = selectQuery.condition().get();
            TypedQuery<T> query = buildQuery(type, type, QueryModifier.selectWhere(criteria));
            return Optional.ofNullable(toDataExceptions(query::getSingleResultOrNull));
        }
    }

    private static <T> T toDataExceptions(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NonUniqueResultException e) {
            throw new jakarta.data.exceptions.NonUniqueResultException(e);
        }
    }

    public <T> long count(SelectQuery selectQuery) {
        final String entityName = selectQuery.name();
        if (selectQuery.condition().isEmpty()) {
            return count(entityName);
        } else {
            Class<?> type = entityTypeFromEntityName(entityName);
            final CriteriaCondition criteria = selectQuery.condition().get();
            TypedQuery<Long> query = buildQuery(type, Long.class, QueryModifier.countWhere(criteria));
            return query.getSingleResult();
        }
    }

    public Query buildQuery(String query) {
        return buildQuery(query, null);
    }

    public Query buildQuery(String query, String entity) {
        if (query.startsWith("WHERE") && entity != null) {
            query = "SELECT this FROM " + entity + " " + query;
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

    public <T> Page<T> selectOffset(SelectQuery selectQuery, PageRequest pageRequest) {
        final TypedQuery<T> query = getSelectTypedQuery(selectQuery);
        if (PageRequest.Mode.OFFSET.equals(pageRequest.mode())) {
            try {
                query.setFirstResult(Math.toIntExact(pageRequest.page() - 1) * pageRequest.size());
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("The offset of the first element is too big, page request: " + pageRequest, e);
            }
            query.setMaxResults(Math.min(query.getMaxResults(), pageRequest.size()));
            return PersistencePage.of(query.getResultList(), pageRequest);
        } else {
            throw new UnsupportedOperationException("'selectOffSet(SelectQuery sq, PageRequest pr)' not supported on CURSOR modes");
        }
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
