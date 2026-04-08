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
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class NoopRepository<T, K>  extends PersistenceRepository<T, K> {

    private final SqlTemplate sqlTemplate;

    NoopRepository(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public long countBy() {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public boolean existsById(K id) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public Stream<T> findByIdIn(Iterable<K> ids) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public void deleteByIdIn(Iterable<K> ids) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public <S extends T> S insert(S entity) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public <S extends T> List<S> insertAll(List<S> entities) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public <S extends T> S update(S entity) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public <S extends T> List<S> updateAll(List<S> entities) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    protected Template template() {
        return sqlTemplate;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public <S extends T> S save(S entity) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public <S extends T> List<S> saveAll(List<S> entities) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public Optional<T> findById(K id) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public Stream<T> findAll() {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public Page<T> findAll(PageRequest pageRequest, Order<T> sortBy) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public void deleteById(K id) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public void delete(T entity) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public void deleteAll(List<? extends T> entities) {
        throw new UnsupportedOperationException("The operation is not supported when entity is not defined");
    }

    @Override
    public EntityManager entityManager() {
        return sqlTemplate.entityManager();
    }
}
