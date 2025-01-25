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

import jakarta.data.repository.Query;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.logging.Logger;

import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.QueryType;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.core.repository.DynamicQueryMethodReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.query.AbstractSemiStructuredRepository;
import org.eclipse.jnosql.mapping.semistructured.query.AbstractSemiStructuredRepositoryProxy;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

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

        var methodReturn = DynamicQueryMethodReturn.builder()
                .args(params)
                .method(method)
                .typeClass(type)
                .pageRequest(pageRequest)
                .prepareConverter(textQuery -> {
                    var prepare = template().prepare(textQuery, entity);
//                    prepare.setSelectMapper(query -> updateQueryDynamically(params, query));
                    return prepare;
                }).build();
        return methodReturn.execute();
    }

    @Override
    protected Object executeDeleteByAll(Object instance, Method method, Object[] params) {
        DeleteQuery deleteQuery = deleteQuery(method, params);
        return template().deleteWithCount(deleteQuery);
    }

    @Override
    protected Object executeCursorPagination(Object instance, Method method, Object[] params) {
        // We need to override this because SemiStructuredRepositoryProxy
        // expects the semistructured.PreparedStatement template
        throw new UnsupportedOperationException("Not supported yet.");
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
            Object id = getIdField().read(entity);
            if (nonNull(id) && existsById((K) id)) {
                return template().update(entity);
            } else {
                return template().insert(entity);
            }
        }

    }
}
