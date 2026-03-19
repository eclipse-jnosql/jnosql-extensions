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
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.NoSQLRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class NoSQLRepositorySqlAdapter<T, K> implements NoSQLRepository<T, K> {

    private final Class<T> entityType;

    private final SqlTemplate sqlTemplate;

    public NoSQLRepositorySqlAdapter(Class<T> entityType, SqlTemplate sqlTemplate) {
        this.entityType = entityType;
        this.sqlTemplate = sqlTemplate;
    }

    @Override
    public void deleteAll() {
        sqlTemplate.deleteAll(entityType);
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
    public Stream<T> findByIdIn(Iterable<K> ids) {
        Objects.requireNonNull(ids, "ids is required");

        if (!ids.iterator().hasNext()) {
            return Stream.empty();
        }

        var entityManager = sqlTemplate.entityManager();
        var metamodel = entityManager.getMetamodel().entity(entityType);

        var entityName = metamodel.getName();
        var idAttribute = metamodel.getId(metamodel.getIdType().getJavaType());
        var idFieldName = idAttribute.getName();

        var query = SelectQuery.select()
                .from(entityName)
                .where(idFieldName)
                .in(ids).build();

        return sqlTemplate.select(query);
    }

    @Override
    public void deleteByIdIn(Iterable<K> ids) {
        Objects.requireNonNull(ids, "ids is required");

        if (!ids.iterator().hasNext()) {
            return;
        }

        var entityManager = sqlTemplate.entityManager();
        var metamodel = entityManager.getMetamodel().entity(entityType);

        var entityName = metamodel.getName();
        var idAttribute = metamodel.getId(metamodel.getIdType().getJavaType());
        var idFieldName = idAttribute.getName();

        var query = DeleteQuery.delete()
                .from(entityName)
                .where(idFieldName)
                .in(ids).build();

        sqlTemplate.delete(query);
    }

    @Override
    public <S extends T> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends T> List<S> insertAll(List<S> entities) {
        return List.of();
    }

    @Override
    public <S extends T> S update(S entity) {
        return null;
    }

    @Override
    public <S extends T> List<S> updateAll(List<S> entities) {
        return List.of();
    }

    @Override
    public <S extends T> S save(S entity) {
        return null;
    }

    @Override
    public <S extends T> List<S> saveAll(List<S> entities) {
        return List.of();
    }

    @Override
    public Optional<T> findById(K id) {
        return Optional.empty();
    }

    @Override
    public Stream<T> findAll() {
        return Stream.empty();
    }

    @Override
    public Page<T> findAll(PageRequest pageRequest, Order<T> sortBy) {
        return null;
    }

    @Override
    public void deleteById(K id) {

    }

    @Override
    public void delete(T entity) {

    }

    @Override
    public void deleteAll(List<? extends T> entities) {

    }
}
