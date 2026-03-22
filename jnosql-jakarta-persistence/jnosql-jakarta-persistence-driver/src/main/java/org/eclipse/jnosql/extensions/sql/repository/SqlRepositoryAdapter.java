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
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class SqlRepositoryAdapter<T, K> extends AbstractRepository<T, K> implements NoSQLRepository<T, K> {

    private final Class<T> entityType;

    private final SqlTemplate sqlTemplate;

    private final EntityMetadata metadata;

    SqlRepositoryAdapter(Class<T> entityType, SqlTemplate sqlTemplate) {
        this.entityType = entityType;
        this.sqlTemplate = sqlTemplate;
        this.metadata = resolveMetadata();
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
                .in(ids).build();

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
        sqlTemplate.delete(entity);
    }

    @Override
    public void deleteAll(List<? extends T> entities) {
        Objects.requireNonNull(entities, "entities are required");

        if (entities.isEmpty()) {
            return;
        }

        sqlTemplate.delete(entities);
    }

    @Override
    public <S extends T> S insert(S entity) {
        Objects.requireNonNull(entity, "entity is required");
        return sqlTemplate.insert(entity);
    }

    @Override
    public <S extends T> List<S> insertAll(List<S> entities) {
        Objects.requireNonNull(entities, "entities are required");

        var result = sqlTemplate.insert(entities);
        return (result instanceof List)
                ? (List<S>) result
                : StreamSupport.stream(result.spliterator(), false)
                .toList();
    }

    @Override
    public <S extends T> S update(S entity) {
        Objects.requireNonNull(entity, "entity is required");
        return sqlTemplate.update(entity);
    }

    @Override
    public <S extends T> List<S> updateAll(List<S> entities) {
        Objects.requireNonNull(entities, "entities are required");

        var result = sqlTemplate.update(entities);

        return (result instanceof List)
                ? (List<S>) result
                : StreamSupport.stream(result.spliterator(), false)
                .toList();
    }

    @Override
    protected Template template() {
        return sqlTemplate;
    }

    @Override
    protected org.eclipse.jnosql.mapping.metadata.EntityMetadata entityMetadata() {
        return null;
    }

    @Override
    public <S extends T> S save(S entity) {
        Objects.requireNonNull(entity, "entity is required");
        return sqlTemplate.update(entity);
    }

    @Override
    public <S extends T> List<S> saveAll(List<S> entities) {
        Objects.requireNonNull(entities, "entities are required");
        if (entities.isEmpty()) {
            return List.of();
        }
        var result = sqlTemplate.update(entities);

        return (result instanceof List)
                ? (List<S>) result
                : StreamSupport.stream(result.spliterator(), false)
                .toList();
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
                .in(ids).build();

        return sqlTemplate.select(query);
    }

    @Override
    public Stream<T> findAll() {
        return sqlTemplate.findAll(entityType);
    }

    @Override
    public Page<T> findAll(PageRequest pageRequest, Order<T> sortBy) {
        Objects.requireNonNull(pageRequest, "pageRequest is required");
        Objects.requireNonNull(sortBy, "sortBy is required");

        SelectQuery selectQuery = SelectQuery.builder().from(metadata.name())
                .sort(sortBy.sorts().toArray(new jakarta.data.Sort[0]))
                .build();
        return sqlTemplate.selectOffSet(selectQuery, pageRequest);
    }


    private EntityMetadata resolveMetadata() {

        var entityManager = sqlTemplate.entityManager();
        var metamodel = entityManager.getMetamodel().entity(entityType);

        String entityName = metamodel.getName();

        // Find the @Id field manually (provider-safe)
        String idFieldName = Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(jakarta.persistence.Id.class))
                .findFirst()
                .map(Field::getName)
                .orElseThrow(() -> new IllegalStateException(
                        "No @Id field found on entity " + entityType.getName()
                ));

        return new EntityMetadata(entityName, idFieldName);
    }

    private record EntityMetadata(String name, String idName) {
    }
}
