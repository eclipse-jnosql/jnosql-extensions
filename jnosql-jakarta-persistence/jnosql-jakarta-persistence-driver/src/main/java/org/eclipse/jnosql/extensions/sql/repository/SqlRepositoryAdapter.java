/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.nosql.Template;
import jakarta.persistence.EntityManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.SqlEntityMetadata;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class SqlRepositoryAdapter<T, K> extends PersistenceRepository<T, K> {

    private final Class<T> entityType;

    private final SqlTemplate sqlTemplate;

    private final SqlEntityMetadata metadata;

    private final LifecycleEventHandler lifecycleEventHandler;

    SqlRepositoryAdapter(
            Class<T> entityType,
            SqlTemplate sqlTemplate,
            LifecycleEventHandler lifecycleEventHandler) {
        this.entityType = entityType;
        this.sqlTemplate = sqlTemplate;
        this.metadata = SqlEntityMetadata.of(
                entityType,
                this.sqlTemplate.entityManager());
        this.lifecycleEventHandler = lifecycleEventHandler;
    }

    @Override
    public long countBy() {
        return sqlTemplate.count(entityType);
    }

    @Override
    public boolean existsById(K id) {
        Objects.requireNonNull(id, "id is required");
        return sqlTemplate.existsById(entityType, id);
    }

    @Override
    public void deleteByIdIn(Iterable<K> ids) {
        Objects.requireNonNull(ids, "ids is required");

        if (!ids.iterator().hasNext()) {
            return;
        }

        var query = DeleteQuery.delete()
                .from(metadata.name())
                .where(metadata.idName())
                .in(ids)
                .build();

        sqlTemplate.delete(query);
    }

    @Override
    public void deleteAll() {
        sqlTemplate.deleteAll(entityType);
    }

    @Override
    public void deleteById(K id) {
        Objects.requireNonNull(id, "id is required");
        sqlTemplate.delete(entityType, id);
    }

    @Override
    public void delete(T entity) {
        Objects.requireNonNull(entity, "entity is required");

        lifecycleEventHandler.preDelete(entity);

        sqlTemplate.delete(entity);

        lifecycleEventHandler.postDelete(entity);
    }

    @Override
    public void deleteAll(List<? extends T> entities) {
        Objects.requireNonNull(entities, "entities are required");

        if (entities.isEmpty()) {
            return;
        }

        entities.forEach(lifecycleEventHandler::preDelete);

        sqlTemplate.delete(entities);

        entities.forEach(lifecycleEventHandler::postDelete);
    }

    @Override
    public <S extends T> S insert(S entity) {
        Objects.requireNonNull(entity, "entity is required");

        lifecycleEventHandler.preInsert(entity);

        S result = sqlTemplate.insert(entity);

        lifecycleEventHandler.postInsert(result);

        return result;
    }

    @Override
    public <S extends T> List<S> insertAll(List<S> entities) {
        Objects.requireNonNull(entities, "entities are required");

        if (entities.isEmpty()) {
            return List.of();
        }

        entities.forEach(lifecycleEventHandler::preInsert);

        Iterable<S> inserted = sqlTemplate.insert(entities);
        List<S> result = toList(inserted);

        result.forEach(lifecycleEventHandler::postInsert);

        return result;
    }

    @Override
    public <S extends T> S update(S entity) {
        Objects.requireNonNull(entity, "entity is required");

        lifecycleEventHandler.preUpdate(entity);

        S result = sqlTemplate.update(entity);

        lifecycleEventHandler.postUpdate(result);

        return result;
    }

    @Override
    public <S extends T> List<S> updateAll(List<S> entities) {
        Objects.requireNonNull(entities, "entities are required");

        if (entities.isEmpty()) {
            return List.of();
        }

        entities.forEach(lifecycleEventHandler::preUpdate);

        Iterable<S> updated = sqlTemplate.update(entities);
        List<S> result = toList(updated);

        result.forEach(lifecycleEventHandler::postUpdate);

        return result;
    }

    @Override
    public <S extends T> S save(S entity) {
        Objects.requireNonNull(entity, "entity is required");

        lifecycleEventHandler.preUpsert(entity);

        S result = sqlTemplate.update(entity);

        lifecycleEventHandler.postUpsert(result);

        return result;
    }

    @Override
    public <S extends T> List<S> saveAll(List<S> entities) {
        Objects.requireNonNull(entities, "entities are required");

        if (entities.isEmpty()) {
            return List.of();
        }

        entities.forEach(lifecycleEventHandler::preUpsert);

        Iterable<S> saved = sqlTemplate.update(entities);
        List<S> result = toList(saved);

        result.forEach(lifecycleEventHandler::postUpsert);

        return result;
    }

    @Override
    public Optional<T> findById(K id) {
        Objects.requireNonNull(id, "id is required");
        return sqlTemplate.find(entityType, id);
    }

    @Override
    public Stream<T> findByIdIn(Iterable<K> ids) {
        Objects.requireNonNull(ids, "ids is required");

        if (!ids.iterator().hasNext()) {
            return Stream.empty();
        }

        var query = SelectQuery.select()
                .from(metadata.name())
                .where(metadata.idName())
                .in(ids)
                .build();

        return sqlTemplate.select(query);
    }

    @Override
    public Stream<T> findAll() {
        return sqlTemplate.findAll(entityType);
    }

    @Override
    public Page<T> findAll(
            PageRequest pageRequest,
            Order<T> sortBy) {
        Objects.requireNonNull(
                pageRequest,
                "pageRequest is required");
        Objects.requireNonNull(
                sortBy,
                "sortBy is required");

        SelectQuery selectQuery = SelectQuery.builder()
                .from(metadata.name())
                .sort(sortBy.sorts()
                        .toArray(new jakarta.data.Sort[0]))
                .build();

        return sqlTemplate.selectOffSet(
                selectQuery,
                pageRequest);
    }

    public SqlEntityMetadata metadata() {
        return metadata;
    }

    @Override
    public EntityManager entityManager() {
        return sqlTemplate.entityManager();
    }

    @Override
    protected Template template() {
        return sqlTemplate;
    }

    @Override
    protected org.eclipse.jnosql.mapping.metadata.EntityMetadata
    entityMetadata() {
        return metadata;
    }

    @Override
    protected LifecycleEventHandler lifeCycle() {
        return lifecycleEventHandler;
    }

    private <S extends T> List<S> toList(Iterable<S> entities) {
        if (entities instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<S> result = (List<S>) list;
            return result;
        }

        return StreamSupport.stream(
                        entities.spliterator(),
                        false)
                .toList();
    }
}