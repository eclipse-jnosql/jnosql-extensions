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
package org.eclipse.jnosql.extensions.sql;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.nosql.Query;
import jakarta.nosql.QueryMapper;
import jakarta.nosql.TypedQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnitUtil;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

class DefaultSqlTemplate implements SqlTemplate {

    private final EntityManager entityManager;

    private DefaultSqlTemplate(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public PersistenceUnitUtil persistenceUnitUtil() {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    @Override
    public long deleteWithCount(DeleteQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T, K> boolean existsById(Class<T> type, K id) {
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(id, "id is required");
        return executeInTransaction(() -> entityManager().find(type, id) != null);
    }

    @Override
    public long count(String entity) {
        Objects.requireNonNull(entity, "entity is null");

        return executeInTransaction(() -> {
            String jpql = "SELECT COUNT(e) FROM " + entity + " e";
            return entityManager.createQuery(jpql, Long.class)
                    .getSingleResult();
        });
    }

    @Override
    public <T> long count(Class<T> type) {
        Objects.requireNonNull(type, "type is null");

        return executeInTransaction(() -> {
            var entityName = entityManager.getMetamodel()
                    .entity(type)
                    .getName();

            var jpql = "SELECT COUNT(e) FROM " + entityName + " e";
            return entityManager.createQuery(jpql, Long.class)
                    .getSingleResult();
        });
    }

    @Override
    public PreparedStatement prepare(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PreparedStatement prepare(String query, String entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(DeleteQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(UpdateQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> Stream<T> select(SelectQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long count(SelectQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean exists(SelectQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> Optional<T> singleResult(SelectQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> Stream<T> findAll(Class<T> type) {
        Objects.requireNonNull(type, "type is null");
        return executeInTransaction(() -> {
            String entityName = entityManager
                    .getMetamodel()
                    .entity(type)
                    .getName();

            return entityManager
                    .createQuery("SELECT e FROM " + entityName + " e", type)
                    .getResultStream();
        });
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        Objects.requireNonNull(type, "type is null");
        executeInTransaction(() -> {
            String entityName = entityManager
                    .getMetamodel()
                    .entity(type)
                    .getName();

            entityManager
                    .createQuery("DELETE FROM " + entityName)
                    .executeUpdate();
            return void.class;
        });
    }

    @Override
    public <T> CursoredPage<T> selectCursor(SelectQuery query, PageRequest pageRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> Page<T> selectOffSet(SelectQuery query, PageRequest pageRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T insert(T entity) {
        Objects.requireNonNull(entity, "entity is null");
        return executeInTransaction(() -> {
            entityManager.persist(entity);
            return entity;
        });
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities) {
        Objects.requireNonNull(entities, "entities is null");
        return executeInTransaction(() -> {
            for (T entity : entities) {
                Objects.requireNonNull(entity, "entity element is null");
                entityManager.persist(entity);
            }
            return entities;
        });
    }


    @Override
    public <T> T update(T entity) {
        Objects.requireNonNull(entity, "entity is null");
        return executeInTransaction(() -> entityManager.merge(entity));
    }

    @Override
    public <T> Iterable<T> update(Iterable<T> entities) {
        Objects.requireNonNull(entities, "entities is null");
        return executeInTransaction(() -> {
            List<T> merged = new ArrayList<>();
            for (T entity : entities) {
                Objects.requireNonNull(entity, "entity element is null");
                merged.add(entityManager.merge(entity));
            }

            return merged;
        });
    }

    @Override
    public <T> void delete(T entity) {
        Objects.requireNonNull(entity, "entity is null");
        executeInTransaction(() -> {
            T managed = entityManager.contains(entity)
                    ? entity
                    : entityManager.merge(entity);

            entityManager.remove(managed);
            return void.class;
        });
    }

    @Override
    public <T> void delete(Iterable<? extends T> entities) {
        Objects.requireNonNull(entities, "entities is null");
        executeInTransaction(() -> {
            for (T entity : entities) {
                Objects.requireNonNull(entity, "entity element is null");

                T managed = entityManager.contains(entity)
                        ? entity
                        : entityManager.merge(entity);

                entityManager.remove(managed);
            }
            return void.class;
        });
    }


    @Override
    public <T> T insert(T entity, Duration ttl) {
        throw new UnsupportedOperationException("SQL does not support TTL.");
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities, Duration ttl) {
        throw new UnsupportedOperationException("SQL does not support TTL.");
    }


    @Override
    public <T, K> Optional<T> find(Class<T> type, K id) {
        Objects.requireNonNull(type, "type is null");
        Objects.requireNonNull(id, "id is null");
        return executeInTransaction(() -> {
            T entity = entityManager.find(type, id);
            return Optional.ofNullable(entity);
        });
    }

    @Override
    public <T, K> void delete(Class<T> type, K id) {
        Objects.requireNonNull(type, "type is null");
        Objects.requireNonNull(id, "id is null");
        executeInTransaction(() -> {
            T entity = entityManager.find(type, id);

            if (entity != null) {
                entityManager.remove(entity);
            }
            return void.class;
        });
    }

    @Override
    public <T> QueryMapper.MapperFrom select(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> QueryMapper.MapperDeleteFrom delete(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> QueryMapper.MapperUpdateFrom update(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query query(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> typedQuery(String query, Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    <T> T executeInTransaction(Supplier<T> operation) {

        EntityTransaction tx = entityManager.getTransaction();

        boolean started = false;

        if (!tx.isActive()) {
            tx.begin();
            started = true;
        }

        try {
            T result = operation.get();

            if (started) {
                tx.commit();
            }

            return result;

        } catch (RuntimeException e) {

            if (started && tx.isActive()) {
                tx.rollback();
            }

            throw e;
        }
    }

    static SqlTemplate of(EntityManager entityManager) {
        return new DefaultSqlTemplate(entityManager);
    }
}
