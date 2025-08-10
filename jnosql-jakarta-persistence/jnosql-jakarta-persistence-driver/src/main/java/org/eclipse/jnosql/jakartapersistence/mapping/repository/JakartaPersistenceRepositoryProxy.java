/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import java.util.logging.Logger;

import org.eclipse.jnosql.communication.semistructured.QueryType;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistencePreparedStatement;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.DynamicQueryMethodReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredRepositoryProxy;

public class JakartaPersistenceRepositoryProxy<T, K> extends SemiStructuredRepositoryProxy<T, K> {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceRepositoryProxy.class.getName());

    public JakartaPersistenceRepositoryProxy(PersistenceDocumentTemplate template, EntitiesMetadata entities, Class<?> repositoryType, Converters converters) {
        super(template, entities, repositoryType, converters);
    }

    public JakartaPersistenceRepositoryProxy(PersistenceDocumentTemplate template, EntityMetadata entity, Class<?> repositoryType, Converters converters) {
        super(template, entity, repositoryType, converters);
    }

    @Override
    protected Object executeQuery(Object instance, Method method, Object[] params) {
        LOGGER.finest("Executing query on method: " + method);
        Class<?> type = entityMetadata().type();
        var entity = entityMetadata().name();
        var pageRequest = DynamicReturn.findPageRequest(params);
        var queryValue = method.getAnnotation(Query.class).value();
        var queryType = QueryType.parse(queryValue);
        var returnType = method.getReturnType();
        LOGGER.finest("Query: " + queryValue + " with type: " + queryType + " and return type: " + returnType);
        queryType.checkValidReturn(returnType, queryValue);

        var methodReturn = DynamicQueryMethodReturn.builder()
                .args(params)
                .method(method)
                .typeClass(type)
                .pageRequest(pageRequest)
                .prepareConverter(textQuery -> {
                    var prepare = (PersistencePreparedStatement) template().prepare(textQuery, entity);
//                    prepare.setSelectMapper(query -> updateQueryDynamically(params, query));
                    return prepare;
                }).build();
        return methodReturn.execute();
    }

    @Override
    protected Object executeCursorPagination(Object instance, Method method, Object[] params) {
        // We need to override this because SemiStructuredRepositoryProxy
        // expects the semistructured.PreparedStatement template
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
