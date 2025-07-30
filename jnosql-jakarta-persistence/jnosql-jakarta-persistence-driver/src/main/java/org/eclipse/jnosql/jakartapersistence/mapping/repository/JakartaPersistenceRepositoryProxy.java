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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.exceptions.EmptyResultException;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.Query;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.QueryType;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.mapping.DataExceptions;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistencePreparedStatement;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.core.query.RepositoryType;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.semistructured.query.AbstractSemiStructuredRepository;
import org.eclipse.jnosql.mapping.semistructured.query.AbstractSemiStructuredRepositoryProxy;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import static org.eclipse.jnosql.mapping.core.query.RepositoryType.ORDER_BY;

public class JakartaPersistenceRepositoryProxy<T, K> extends AbstractSemiStructuredRepositoryProxy<T, K> {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceRepositoryProxy.class.getName());

    private final PersistenceDocumentTemplate template;

    private final JakartaPersistenceStructuredRepository<T, K> repository;

    private final EntityMetadata entityMetadata;

    private final Converters converters;

    private final Class<?> repositoryType;

    public JakartaPersistenceRepositoryProxy(PersistenceDocumentTemplate template, EntitiesMetadata entities, Class<?> repositoryType, Converters converters) {
        this.template = template;
        Class<T> typeClass = (Class) ((ParameterizedType) repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
        this.entityMetadata = entities.get(typeClass);
        this.repository = new JakartaPersistenceStructuredRepository<>(template, entityMetadata);
        this.converters = converters;
        this.repositoryType = repositoryType;
    }

    public JakartaPersistenceRepositoryProxy(PersistenceDocumentTemplate template, EntityMetadata entity, Class<?> repositoryType, Converters converters) {
        this.template = template;
        this.entityMetadata = entity;
        this.repository = new JakartaPersistenceStructuredRepository<>(template, entityMetadata);
        this.converters = converters;
        this.repositoryType = repositoryType;
    }

    @Override
    protected Object invokeForMethodType(final RepositoryType type, Object instance, Method method, Object[] params) throws Throwable {
        Map<? extends String, ? extends Object> contextData = Map.of(EntityManager.class.getName(), template.entityManager());
        InterceptorInvocationContext context
                = new InterceptorInvocationContext(params, instance, method, contextData) {
            @Override
            protected Instance<MethodInterceptor> selectInterceptor() {
                return CDI.current().select(MethodInterceptor.class, MethodInterceptor.Repository.INSTANCE);
            }

            @Override
            protected Object invoke(Object instance, Method method, Object[] params) throws Throwable {
                if (ORDER_BY == type) {
                    return executeOrderByQuery(instance, method, params);
                }
                return JakartaPersistenceRepositoryProxy.super.invokeForMethodType(type, instance, method, params);
            }

        };
        return DataExceptions.handlePersistenceException(context::execute);
    }

    @Override
    protected Object executeQuery(Object instance, Method method, Object[] params) {
        LOGGER.finest(() -> "Executing query on method: " + method);
        Class<?> type = entityMetadata().type();
        var entity = entityMetadata().name();
        var pageRequest = DynamicReturn.findPageRequest(params);
        var queryValue = method.getAnnotation(Query.class).value();
        var queryType = QueryType.parse(queryValue);
        var returnType = method.getReturnType();
        LOGGER.finest(() -> "Query: " + queryValue + " with type: " + queryType + " and return type: " + returnType);
        queryType.checkValidReturn(returnType, queryValue);

        var methodReturn = JakartaPersistenceDynamicQueryMethodReturn.builder()
                .args(params)
                .method(method)
                .typeClass(type)
                .pageRequest(pageRequest)
                .prepareConverter(textQuery -> {
                    var prepare = template().prepare(textQuery, entity);
                    prepare.setSelectMapper(query -> updateQueryDynamically(params, query));
                    setProjections(prepare, params);
                    return prepare;
                }).build();
        return methodReturn.execute();
    }

    @SuppressWarnings("unchecked")
    protected Object executeFindByQuery(Method method, Object[] args, Class<?> typeClass, SelectQuery query) {
        // TODO: Perform type check on return type during deployment and fail deployment if not supported.
        // Currently, different types are supported by implementations of RepositoryReturn via service loader
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodSource(method)
                .result(() -> template().select(query))
                .singleResult(() -> template().singleResult(query))
                .pagination(DynamicReturn.findPageRequest(args))
                .streamPagination(streamPagination(query))
                .singleResultPagination(getSingleResult(query))
                .page(getPage(query))
                .build();
        Object result = dynamicReturn.execute();
        if (result != null) {
            return result;
        } else {
            throw new EmptyResultException("Call to method " + method + " found no matching results.");
        }
    }

    @Override
    protected Object executeDeleteByAll(Object instance, Method method, Object[] params) {
        DeleteQuery deleteQuery = deleteQuery(method, params);
        return template().deleteWithCount(deleteQuery);
    }

    private Object executeOrderByQuery(Object instance, Method method, Object[] params) {
        // TODO - revise, maybe use something else than SemiStructuredParameterBasedQuery with no params - use query(method, params) plus sorts
//        Class<?> type = entityMetadata().type();
//        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(Map.of(), getSorts(method, entityMetadata()), entityMetadata());
//        return template().select(query);
        SelectQuery selectQuery = query(method, params);
        final List<Sort<?>> sorts = getSorts(method, entityMetadata());
        selectQuery = modifySelectQuery(sorts, selectQuery);
        return template().select(selectQuery);
    }

    private static MappingQuery modifySelectQuery(List<Sort<?>> sorts, SelectQuery selectQuery) {
        return new MappingQuery(sorts, selectQuery.limit(), selectQuery.skip(), selectQuery.condition().orElse(null), selectQuery.name(), selectQuery.columns());
    }

    @Override
    protected Object executeCursorPagination(Object instance, Method method, Object[] params) {
        /* TODO We need to override this because SemiStructuredRepositoryProxy
         expects the semistructured.PreparedStatement template */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Function<PageRequest, Page<T>> getPage(org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        return p -> template().selectOffSet(query, p);
    }

    @Override
    protected PersistenceDocumentTemplate template() {
        return template;
    }

    @Override
    protected AbstractRepository<T, K> repository() {
        return repository;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return entityMetadata;
    }

    @Override
    protected Converters converters() {
        return converters;
    }

    @Override
    protected Class<?> repositoryType() {
        return repositoryType;
    }

    private void setProjections(PersistencePreparedStatement prepare, Object[] params) {
        SpecialParameters special = DynamicReturn.findSpecialParameters(params, sortParser());
        Limit limit = special.pageRequest()
                .map(pageRequest -> {
                    long size = pageRequest.size();
                    long startAt = pageRequest.size() * pageRequest.page();
                    return new Limit(Math.toIntExact(size), startAt);
                })
                .or(() -> special.limit())
                .orElse(null);
        prepare.setLimit(limit);
        prepare.setSorts(special.sorts());
    }

    /**
     * Repository implementation for column-based repositories.
     *
     * @param <T> The entity type managed by the repository.
     * @param <K> The key type used for column-based operations.
     */
    public static class JakartaPersistenceStructuredRepository<T, K> extends AbstractSemiStructuredRepository<T, K> {

        private final PersistenceDocumentTemplate template;

        private final EntityMetadata entityMetadata;

        JakartaPersistenceStructuredRepository(PersistenceDocumentTemplate template, EntityMetadata entityMetadata) {
            this.template = template;
            this.entityMetadata = entityMetadata;
        }

        /**
         * Creates a new instance of ColumnRepository.
         *
         * @param <T> The entity type managed by the repository.
         * @param <K> The key type used for column-based operations.
         * @param template The SemistructuredTemplate used for column database
         * operations. Must not be {@code null}.
         * @param metadata The metadata of the entity. Must not be {@code null}.
         * @return A new instance of ColumnRepository.
         * @throws NullPointerException If either the template or metadata is
         * {@code null}.
         */
        public static <T, K> JakartaPersistenceStructuredRepository<T, K> of(PersistenceDocumentTemplate template, EntityMetadata metadata) {
            Objects.requireNonNull(template, "template is required");
            Objects.requireNonNull(metadata, "metadata is required");
            return new JakartaPersistenceStructuredRepository<>(template, metadata);
        }

        @Override
        protected PersistenceDocumentTemplate template() {
            return template;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entityMetadata;
        }

        @Override
        public <S extends T> S save(S entity) {
            requireNonNull(entity, "Entity is required");

            K id = getEntityId(entity);
            if (nonNull(id) && existsById(id)) {
                return template().update(entity);
            } else {
                return template().insert(entity);
            }
        }

        @Override
        public boolean existsById(K id) {
            return template().existsById(type(), id);
        }

        @Override
        public void delete(T entity) {
            requireNonNull(entity, "Entity is required");

            K id = getEntityId(entity);
            template().deleteEntity(entity);
        }

        private K getEntityId(T entity) {
            return (K)template.getPersistenceUnitUtil().getIdentifier(entity);
        }

    }
}
