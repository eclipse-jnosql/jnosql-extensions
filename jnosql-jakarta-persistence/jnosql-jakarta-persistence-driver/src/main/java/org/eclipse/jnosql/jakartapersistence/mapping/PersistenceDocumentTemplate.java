/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;

import jakarta.annotation.Priority;
import jakarta.data.exceptions.EntityExistsException;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import jakarta.nosql.QueryMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;

import static org.eclipse.jnosql.jakartapersistence.mapping.QLUtil.isDeleteQuery;
import static org.eclipse.jnosql.jakartapersistence.mapping.QLUtil.isUpdateQuery;

@Alternative
@Priority(Interceptor.Priority.APPLICATION)
@Default
@ApplicationScoped
@Database(DatabaseType.DOCUMENT)
public class PersistenceDocumentTemplate implements DocumentTemplate {

    private final PersistenceDatabaseManager manager;
    private final SelectQueryParser selectParser;
    private final DeleteQueryParser deleteParser;
    private final UpdateQueryParser updateParser;

    @Inject
    PersistenceDocumentTemplate(PersistenceDatabaseManager manager) {
        this.manager = manager;
        this.selectParser = new SelectQueryParser(manager);
        this.deleteParser = new DeleteQueryParser(manager);
        this.updateParser = new UpdateQueryParser(manager);
    }

    PersistenceDocumentTemplate() {
        manager = null;
        selectParser = null;
        deleteParser = null;
        updateParser = null;
    }

    private EntityManager entityManager() {
        return manager.getEntityManager();
    }

    @Override
    public long count(String entity) {
        return selectParser.count(entity);
    }

    @Override
    public <T> long count(Class<T> type) {
        return selectParser.count(type);
    }

    @Override
    public <T> Stream<T> findAll(Class<T> type) {
        return selectParser.findAll(type);
    }

    @Override
    public <T> Stream<T> query(String query) {
        final BaseQueryParser queryParser = getParserForQuery(query);
        return queryParser.query(query);
    }

    @Override
    public <T> Stream<T> query(String query, String entity) {
        final BaseQueryParser queryParser = getParserForQuery(query);
        return queryParser.query(query, entity);
    }

    @Override
    public <T> Optional<T> singleResult(String query) {
        return selectParser.singleResult(query);
    }

    @Override
    public <T> Optional<T> singleResult(String query, String entity) {
        return selectParser.singleResult(query, entity);
    }

    @Override
    public <T, K> Optional<T> find(Class<T> type, K k) {
        return selectParser.find(type, k);
    }

    public <T, K> boolean existsById(Class<T> type, K k) {
        return selectParser.existsById(type, k);
    }

    @Override
    public <T> T insert(T entity) {
        final Object identifier = getPersistenceUnitUtil().getIdentifier(entity);
        final Object entityWithSameId = entityManager().find(entity.getClass(), identifier);
        if (entityWithSameId != null) {
            throw new EntityExistsException("Entity of type " + entity.getClass() + " with id=" + identifier + " already exists");
        }
        entityManager().persist(entity);
        return entity;
    }

    @Override
    public <T> T update(T entity) {
        return entityManager().merge(entity);
    }

    @Override
    public PersistencePreparedStatement prepare(String queryString, String entity) {
        BaseQueryParser queryParser = getParserForQuery(queryString);
        return new PersistencePreparedStatement(queryString, queryParser, entity);
    }

    @Override
    public PreparedStatement prepare(String queryString) {
        BaseQueryParser queryParser = getParserForQuery(queryString);
        return new PersistencePreparedStatement(queryString, queryParser);
    }

    private BaseQueryParser getParserForQuery(String entity) {
        BaseQueryParser queryParser;
        if (isUpdateQuery(entity)) {
            queryParser = updateParser;
        } else if (isDeleteQuery(entity)) {
            queryParser = deleteParser;
        } else {
            queryParser = selectParser;
        }
        return queryParser;
    }

    @Override
    public void delete(DeleteQuery query) {
        deleteWithCount(query);
    }

    public long deleteWithCount(DeleteQuery query) {
        return deleteParser.delete(query);
    }

    @Override
    public <T> Stream<T> select(SelectQuery selectQuery) {
        return selectParser.select(selectQuery);
    }

    @Override
    public <T> Optional<T> singleResult(SelectQuery selectQuery) {
        return selectParser.singleResult(selectQuery);
    }

    @Override
    public long count(SelectQuery selectQuery) {
        return selectParser.count(selectQuery);
    }

    @Override
    public boolean exists(SelectQuery query) {
        return selectParser.exists(query);
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        deleteParser.deleteAll(type);
    }

    @Override
    public <T> CursoredPage<T> selectCursor(SelectQuery query, PageRequest pageRequest) {
        throw new UnsupportedOperationException("'selectCursor' not supported yet.");
    }

    @Override
    public <T> T insert(T t, Duration drtn) {
        throw new UnsupportedOperationException("'insert(T t, Duration drtn)' not supported yet.");
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> itrbl) {
        throw new UnsupportedOperationException("'insert(Iterable<T> itrbl)' not supported yet.");
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> itrbl, Duration drtn) {
        throw new UnsupportedOperationException("'insert(Iterable<T> itrbl, Duration drtn)' not supported yet.");
    }

    @Override
    public <T> Iterable<T> update(Iterable<T> itrbl) {
        throw new UnsupportedOperationException("'update' not supported yet.");
    }

    @Override
    public <T, K> void delete(Class<T> type, K key) {
        deleteParser.delete(type, key);
    }

    @Override
    public <T> QueryMapper.MapperFrom select(Class<T> type) {
        throw new UnsupportedOperationException("'select(Class<T> type)' not supported yet.");
    }

    @Override
    public <T> QueryMapper.MapperDeleteFrom delete(Class<T> type) {
        throw new UnsupportedOperationException("'delete(Class<T> type)' not supported yet.");
    }

    @Override
    public <T> Page<T> selectOffSet(SelectQuery sq, PageRequest pr) {
        return selectParser.selectOffset(sq, pr);
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return entityManager().getEntityManagerFactory().getPersistenceUnitUtil();
    }

}
