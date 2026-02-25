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
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.data.SelectProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.Conditions;
import org.eclipse.jnosql.communication.semistructured.DefaultSelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.core.PersistencePage;

import org.eclipse.jnosql.jakartapersistence.mapping.parser.OptionalPartsParser;

/**
 * Parser for SELECT queries in the Jakarta Persistence driver.
 * This class handles the conversion of JNoSQL queries to JPA CriteriaQuery objects
 * and provides caching mechanisms to improve performance.
 *
 * <p>Key features:
 * <ul>
 *   <li>Caches CriteriaQuery objects to avoid repeated query compilation</li>
 *   <li>Supports both entity and projection queries</li>
 *   <li>Handles pagination with offset-based page requests</li>
 *   <li>Provides count queries for efficient pagination</li>
 * </ul>
 */
class SelectQueryParser extends BaseQueryParser {

    /**
     * @param manager the PersistenceDatabaseManager providing access to EntityManager and cache
     */
    public SelectQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    /**
     * Counts the total number of entities of the specified type.

     *
     * @param entity the entity name to count
     * @return total count of entities
     */
    public long count(String entity) {
        final Class<?> type = entityClassFromEntityName(entity);
        return count(type);
    }

    /**
     * Counts the total number of entities of the specified type.
     *
     * @param <T> the entity type
     * @param type the entity class to count
     * @return total count of entities
     */
    public <T> long count(Class<T> type) {
        List<Object> selectQueryKey = Arrays.asList("count", type);
        CriteriaQuery<Long> criteriaQuery = manager.getPersistenceUnitCache().getOrCreateSelectQuery(selectQueryKey,
                key -> buildQuery(type, Long.class, QueryModifier.selectCount())
        );
        return entityManager()
                .createQuery(criteriaQuery)
                .getSingleResult();
    }

    /**
     * Finds all entities of the specified type.
     *
     * @param <T> the entity type
     * @param type the entity class to find
     * @return stream of all entities of the specified type
     */
    public <T> Stream<T> findAll(Class<T> type) {
        List<Object> selectQueryKey = Arrays.asList("findAll", type);
        CriteriaQuery<T> criteriaQuery = manager.getPersistenceUnitCache().getOrCreateSelectQuery(selectQueryKey,
                key -> buildQuery(type, type, QueryModifier.selectEntity())
        );
        return entityManager()
                .createQuery(criteriaQuery)
                .getResultStream();
    }

    @Override
    protected <T> Stream<T> query(String queryString, String entity, Collection<Sort<?>> sorts, Consumer<Query> queryModifier) {
        final Query query = buildQuery(queryString, entity, sorts);
        if (queryModifier != null) {
            queryModifier.accept(query);
        }
        return query.getResultStream();
    }

    @Override
    protected Query buildQuery(String queryString, String entity, Collection<Sort<?>> sorts) {
        queryString = preProcessQuery(queryString, entity, sorts);
        return super.buildQuery(queryString, entity, sorts);
    }

    /* Alternative way to parse JDQL query using the JNoSQL parser. Supports annotations like @OrderBy,
     * doesn't support full JPQL. If not useful, remove this and related methods,
     * including setSelectMapper on prepared statement.
     */
    private <T> Stream<T> queryJNoSQLParser(String queryString, String entity, UnaryOperator<SelectQuery> selectMapper,
            Map<String, Object> parameters, Consumer<Query> queryModifier) {
        SelectQuery selectQuery = parseQuery(queryString, entity, parameters);
        if (selectMapper != null) {
            selectQuery = selectMapper.apply(selectQuery);
        }
        final TypedQuery<T> query = getSelectTypedQuery(selectQuery);
        if (queryModifier != null) {
            queryModifier.accept(query);
        }
        return query.getResultStream();
    }

    private SelectQuery parseQuery(String query, String entity, Map<String, Object> parameters) {

        CommunicationObserverParser noopObserver = new CommunicationObserverParser() {
        };

        var converter = SelectProvider.INSTANCE;
        var selectQuery = converter.apply(query, entity);
        var entityName = selectQuery.entity();
        var limit = selectQuery.limit();
        var skip = selectQuery.skip();
        var columns = selectQuery.fields();
        List<Sort<?>> sorts = selectQuery.orderBy();

        Params params = Params.newParams();

        var condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, noopObserver, entityName)).orElse(null);

        parameters.forEach(params::bind);

        boolean count = selectQuery.isCount();
        return new DefaultSelectQuery(limit, skip, entityName, columns, sorts, condition, count);
    }

    public <T> Optional<T> singleResult(String queryString) {
        return singleResult(queryString, null);
    }

    public <T> Optional<T> singleResult(String queryString, String entity) {
        queryString = preProcessQuery(queryString, entity, null);
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

    private <FROM, RESULT> TypedQuery<RESULT> getSelectTypedQuery(SelectQuery selectQuery) {
        Class<FROM> fromType = entityClassFromEntityName(selectQuery.name());
        TypedQuery<RESULT> query;
        List<Object> selectQueryKey = Arrays.asList(selectQuery.name(), selectQuery.condition(), selectQuery.sorts(), selectQuery.columns());
        if (selectQuery.columns().isEmpty()) {
            CriteriaQuery<FROM> criteriaQuery = manager.getPersistenceUnitCache().getOrCreateSelectQuery(selectQueryKey,
                    key -> buildQuery(fromType, fromType, QueryModifier.combine(
                            QueryModifier.selectEntity(),
                            QueryModifier.where(selectQuery.condition()),
                            QueryModifier.applySorts(selectQuery.sorts())
                    ))
            );
            TypedQuery<FROM> queryEntity = entityManager().createQuery(criteriaQuery);
            query = (TypedQuery<RESULT>) queryEntity;
        } else {
            CriteriaQuery<RESULT> criteriaQuery = manager.getPersistenceUnitCache().getOrCreateSelectQuery(selectQueryKey,
                    key -> buildQuery(fromType, null, QueryModifier.combine(
                            QueryModifier.selectColumns(selectQuery.columns()),
                            QueryModifier.where(selectQuery.condition()),
                            QueryModifier.applySorts(selectQuery.sorts())
                    ))
            );
            TypedQuery<RESULT> queryColumns = entityManager().createQuery(criteriaQuery);
            query = queryColumns;
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

    /*
     * To be used if we want to retrieve paginate lazily and retrieve the number of elements
     * without loading all the results.
     */
    private <FROM> TypedQuery<Long> getCountQuery(SelectQuery selectQuery) {
        Class<FROM> fromType = entityClassFromEntityName(selectQuery.name());
        List<Object> selectQueryKey = Arrays.asList("count", selectQuery.name(), selectQuery.condition());
        CriteriaQuery<Long> criteriaQuery = manager.getPersistenceUnitCache().getOrCreateSelectQuery(selectQueryKey,
                key -> buildQuery(fromType, Long.class, QueryModifier.combine(
                        QueryModifier.selectCount(),
                        QueryModifier.where(selectQuery.condition())
                ))
        );
        return entityManager().createQuery(criteriaQuery);
    }

    /*
     * To be used if we want to retrieve paginate lazily and retrieve the number of elements
     * without loading all the results.
     */
    private TypedQuery<Long> getCountQuery(String selectQuery, String entity, Collection<Sort<?>> sorts, Consumer<Query> queryModifier) {
        String countQuery = QLUtil.convertToCount(selectQuery);
        final TypedQuery<Long> query = buildQuery(countQuery, entity, Long.class, sorts);
        queryModifier.accept(query);
        return query;
    }

    public <T> Optional<T> singleResult(SelectQuery selectQuery) {
        TypedQuery<T> query = getSelectTypedQuery(selectQuery);
        return Optional.ofNullable(query.getSingleResultOrNull())
                .map(this::refreshEntity);
    }

    public long count(SelectQuery selectQuery) {
        final String entityName = selectQuery.name();
        if (selectQuery.condition().isEmpty()) {
            return count(entityName);
        } else {
            Class<?> type = entityClassFromEntityName(entityName);
            List<Object> selectQueryKey = Arrays.asList("count", entityName, selectQuery.condition());
            CriteriaQuery<Long> criteriaQuery = manager.getPersistenceUnitCache().getOrCreateSelectQuery(selectQueryKey,
                    key -> buildQuery(type, Long.class, QueryModifier.combine(
                            QueryModifier.selectCount(),
                            QueryModifier.where(selectQuery.condition())
                    ))
            );
            return entityManager().createQuery(criteriaQuery).getSingleResult();
        }
    }

    /**
     * Checks if there exists an entity matching the provided query conditions.
     *
     * <p>Implementation note: Since JPA CriteriaQuery requires an entity class for selection,
     * this method selects the literal value 1 instead of the full entity to minimize
     * database load. The query is limited to 1 result for efficiency.
     *
     * @param selectQuery query conditions to check for entity existence
     * @return true if at least one entity matches the query conditions, false otherwise
     */
    public boolean exists(SelectQuery selectQuery) {
        final String entityName = selectQuery.name();
        Class<?> type = entityClassFromEntityName(entityName);
        CriteriaQuery<Integer> criteriaQuery = buildQuery(type, Integer.class, QueryModifier.combine(
                QueryModifier.selectLiteral(1),
                QueryModifier.where(selectQuery.condition())
        ));
        Integer resultOrNull = entityManager().createQuery(criteriaQuery)
                .setMaxResults(1) // succeed if there is at least 1 entity, no need to find all
                .getSingleResultOrNull(); // the result is either 1 (found) or null (not found)
        return resultOrNull != null;
    }

    private String preProcessQuery(String queryString, String entity, final Collection<Sort<?>> sortsArg) {
        return manager.getPersistenceUnitCache().getOrCreateStringQuery(Arrays.asList(queryString, entity, sortsArg),
                key -> {
                    Collection<Sort<?>> sorts = sortsArg;
                    if (sorts != null) {
                        sorts = sorts.stream()
                                .map(this::correctSortPropertyName)
                                .toList();
                    }
                    return new OptionalPartsParser(queryString, entity, sorts)
                            .getCompleteSelect();
                }
        );
    }

    private Sort<?> correctSortPropertyName(Sort<?> sort) {
        final String property = sort.property();
        final int lastDotIndex = property.lastIndexOf(".");
        final String idField = "_id";
        final int idFieldLength = idField.length();
        if (property.length() == lastDotIndex + 1 + idFieldLength
                && property.regionMatches(true, lastDotIndex + 1, idField, 0, idFieldLength)) {
            String newPropertyName = property.substring(0, lastDotIndex + 1) + getFieldName(idField);
            return new Sort(newPropertyName, sort.isAscending(), sort.ignoreCase());
        }
        return sort;
    }

    private <FROM, RESULT> CriteriaQuery<RESULT> buildQuery(Class<FROM> fromType,
            Function<SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> queryModifier) {
        return buildQuery(fromType, null, queryModifier);
    }

    private <FROM, RESULT> CriteriaQuery<RESULT> buildQuery(Class<FROM> fromType, Class<RESULT> resultType,
            Function<SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> queryModifier) {
        EntityManager em = entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<RESULT> criteriaQuery = resultType != null
                ? criteriaBuilder.createQuery(resultType)
                : (CriteriaQuery<RESULT>) criteriaBuilder.createQuery();
        Root<FROM> from = criteriaQuery.from(fromType);
        return queryModifier.apply(
                new SelectQueryContext(criteriaQuery,
                        new QueryContext(from, criteriaBuilder)));
    }

    public <T> Page<T> selectOffset(SelectQuery selectQuery, PageRequest pageRequest) {
        if (PageRequest.Mode.OFFSET.equals(pageRequest.mode())) {
            final TypedQuery<T> query = getSelectTypedQuery(selectQuery);
            try {
                query.setFirstResult(Math.toIntExact(pageRequest.page() - 1) * pageRequest.size());
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("The offset of the first element is too big, page request: " + pageRequest, e);
            }
            query.setMaxResults(Math.min(query.getMaxResults(), pageRequest.size()));
            Supplier<TypedQuery<Long>> countQuerySupplier = pageRequest.requestTotal() ? () -> getCountQuery(selectQuery) : null;
            return new PersistencePage(query, countQuerySupplier, pageRequest);
        } else {
            throw new UnsupportedOperationException("'selectOffSet(SelectQuery sq, PageRequest pr)' not supported on CURSOR modes");
        }
    }

    public <T> Page<T> selectOffset(PageRequest pageRequest, String queryStringParam, String entity, Collection<Sort<?>> sorts,
            Consumer<Query> queryModifier, Consumer<Query> countQueryModifier) {

        if (PageRequest.Mode.OFFSET.equals(pageRequest.mode())) {
            final String queryString = preProcessQuery(queryStringParam, entity, sorts);
            Query query = buildQuery(queryString, entity, sorts);
            queryModifier.accept(query);
            try {
                query.setFirstResult(Math.toIntExact(pageRequest.page() - 1) * pageRequest.size());
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("The offset of the first element is too big, page request: " + pageRequest, e);
            }
            query.setMaxResults(Math.min(query.getMaxResults(), pageRequest.size()));
            Supplier<TypedQuery<Long>> countQuerySupplier = pageRequest.requestTotal()
                    ? () -> getCountQuery(queryString, entity, sorts, countQueryModifier)
                    : null;
            return new PersistencePage(query, countQuerySupplier, pageRequest);
        } else {
            throw new UnsupportedOperationException("'selectOffSet(SelectQuery sq, PageRequest pr)' not supported on CURSOR modes");
        }

    }

    /**
     * Context record that encapsulates CriteriaQuery and QueryContext for SELECT operations.
     * Provides convenient access to the query builder components needed for constructing
     * JPA CriteriaQuery objects.
     *
     * @param <FROM> the entity type being queried from
     * @param <RESULT> the result type of the query
     * @param query the CriteriaQuery being built
     * @param queryContext the QueryContext containing root and builder references
     */
    record SelectQueryContext<FROM, RESULT>(CriteriaQuery<RESULT> query, QueryContext<FROM> queryContext) {

        /**
         * Returns the root entity for the FROM clause.
         *
         * @return the Root entity reference
         */
        public Root<FROM> root() {
            return queryContext.root();
        }

        /**
         * Returns the CriteriaBuilder for constructing query predicates and expressions.
         *
         * @return the CriteriaBuilder instance
         */
        public CriteriaBuilder builder() {
            return queryContext.builder();
        }

    }

}
