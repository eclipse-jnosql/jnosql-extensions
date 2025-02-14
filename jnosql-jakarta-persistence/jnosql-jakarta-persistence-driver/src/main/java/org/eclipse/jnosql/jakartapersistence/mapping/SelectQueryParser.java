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
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.data.SelectProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.Conditions;
import org.eclipse.jnosql.communication.semistructured.DefaultSelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.core.PersistencePage;

import org.eclipse.jnosql.jakartapersistence.mapping.parser.OptionalPartsParser;

class SelectQueryParser extends BaseQueryParser {

    public SelectQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    public long count(String entity) {
        final Class<?> type = entityClassFromEntityName(entity);
        return count(type);
    }

    public <T> long count(Class<T> type) {
        TypedQuery<Long> query = buildQuery(type, Long.class, QueryModifier.selectCount());
        return query.getSingleResult();
    }

    public <T> Stream<T> findAll(Class<T> type) {
        return buildQuery(type, type, QueryModifier.selectEntity())
                .getResultStream();
    }

    public <T> Stream<T> query(String queryString, String entity, Consumer<Query> queryModifier) {
        return query(queryString, entity, null, queryModifier);
    }

    public <T> Stream<T> query(String queryString, String entity, UnaryOperator<SelectQuery> selectMapper, Consumer<Query> queryModifier) {
        SelectQuery selectQuery = parseQuery(queryString, entity);
        if (selectMapper != null) {
            selectQuery = selectMapper.apply(selectQuery);
        }
        final TypedQuery<T> query = getSelectTypedQuery(selectQuery);
        if (queryModifier != null) {
            queryModifier.accept(query);
        }
        return query.getResultStream();
    }

    private SelectQuery parseQuery(String query, String entity) {

        CommunicationObserverParser noopObserver = new CommunicationObserverParser() {
        };

        var converter = SelectProvider.INSTANCE;
        var selectQuery = converter.apply(query, entity);
        var entityName = selectQuery.entity();
        var limit = selectQuery.limit();
        var skip = selectQuery.skip();
        var columns = selectQuery.fields();
        List<Sort<?>> sorts = selectQuery.orderBy();

        var params = Params.newParams();
        var condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, noopObserver, entityName)).orElse(null);

        if (params.isNotEmpty()) {
            throw new QueryException("To run a query with a parameter use a PrepareStatement instead.");
        }
        boolean count = selectQuery.isCount();
        return new DefaultSelectQuery(limit, skip, entityName, columns, sorts, condition, count);
    }

    public <T> Optional<T> singleResult(String queryString) {
        return singleResult(queryString, null);
    }

    public <T> Optional<T> singleResult(String queryString, String entity) {
        queryString = preProcessQuery(queryString, entity);
        return Optional.ofNullable((T) buildQuery(queryString).getSingleResultOrNull())
                .map(this::refreshEntity);
    }

    public <T, K> Optional<T> find(Class<T> type, K k) {
        return Optional.ofNullable(entityManager().find(type, k))
                .map(this::refreshEntity);
    }

    private <T> T refreshEntity(T entity) {
        entityManager().refresh(entity);
        return entity;
    }

    <T, K> boolean existsById(Class<T> type, K k) {
        return null != entityManager().find(type, k);
    }

    public <T> Stream<T> select(SelectQuery selectQuery) {
        final TypedQuery<T> query = getSelectTypedQuery(selectQuery);
        return query.getResultStream();
    }

    private <FROM,RESULT> TypedQuery<RESULT> getSelectTypedQuery(SelectQuery selectQuery) {
        Class<FROM> fromType = entityClassFromEntityName(selectQuery.name());
        TypedQuery<RESULT> query;
        if (selectQuery.columns().isEmpty()) {
            TypedQuery<FROM> queryEntity = buildQuery(fromType, fromType, QueryModifier.combine(
                    QueryModifier.selectEntity(),
                    QueryModifier.where(selectQuery.condition()),
                    QueryModifier.applySorts(selectQuery.sorts())
            ));
            query = (TypedQuery<RESULT>)queryEntity;
        } else {
            TypedQuery<RESULT> queryTuple = buildQuery(fromType, null, QueryModifier.combine(
                    QueryModifier.selectColumns(selectQuery.columns()),
                    QueryModifier.where(selectQuery.condition()),
                    QueryModifier.applySorts(selectQuery.sorts())
            ));
            query = (TypedQuery<RESULT>)queryTuple;
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
        final Class<T> type = entityClassFromEntityName(selectQuery.name());
        Optional<T> result;
        TypedQuery<T> query = buildQuery(type, type, QueryModifier.combine(
                QueryModifier.selectEntity(),
                QueryModifier.where(selectQuery.condition())
        ));
        result = Optional.ofNullable(toDataExceptions(query::getSingleResultOrNull));
        return result.map(this::refreshEntity);
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
            Class<?> type = entityClassFromEntityName(entityName);
            TypedQuery<Long> query = buildQuery(type, Long.class, QueryModifier.combine(
                    QueryModifier.selectCount(),
                    QueryModifier.where(selectQuery.condition())
            ));
            return query.getSingleResult();
        }
    }

    @Override
    protected String preProcessQuery(String queryString, String entity) {
        return new OptionalPartsParser(queryString, entity).getCompleteSelect();
    }

    private <FROM, RESULT> TypedQuery<RESULT> buildQuery(Class<FROM> fromType,
            Function<SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> queryModifier) {
        return buildQuery(fromType, null, queryModifier);
    }

    private <FROM, RESULT> TypedQuery<RESULT> buildQuery(Class<FROM> fromType, Class<RESULT> resultType,
            Function<SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> queryModifier) {
        EntityManager em = entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<RESULT> criteriaQuery = resultType != null
                ? criteriaBuilder.createQuery(resultType)
                : (CriteriaQuery<RESULT>)criteriaBuilder.createQuery();
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
