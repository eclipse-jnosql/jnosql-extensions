/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.SqlPreparedStatement;
import org.eclipse.jnosql.extensions.sql.SqlSelectQuery;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.core.repository.DynamicQueryMethodReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Optional;
import java.util.logging.Logger;


@Typed(SqlQueryOperation.class)
@ApplicationScoped
class SqlQueryOperation implements QueryOperation {

    private static final Logger LOGGER = Logger.getLogger(SqlQueryOperation.class.getName());

    private final SqlReturnType sqlReturnType;

    private final EntitiesMetadata entitiesMetadata;

    @Inject
    SqlQueryOperation(SqlReturnType sqlReturnType, EntitiesMetadata entitiesMetadata) {
        this.sqlReturnType = sqlReturnType;
        this.entitiesMetadata = entitiesMetadata;
    }

     SqlQueryOperation() {
         this.entitiesMetadata = null;
         this.sqlReturnType = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        var method = context.method();
        var params = context.parameters();
        var template = (SqlTemplate) context.template();
        Class<?> type = entityMetadata.type();
        var pageRequest = DynamicReturn.findPageRequest(params);
        var queryValue = method.query().orElseThrow();
        var queryType = QueryType.parse(queryValue);
        var returnType = method.returnType().orElseThrow();
        Optional<Class<?>> returnElementType = method.elementType();
        LOGGER.finest("Query: " + queryValue + " with type: " + queryType + " and return type: " + returnType);
        queryType.checkValidReturn(returnType, queryValue);
        var entity = entityMetadata.name();
        var methodReturn = DynamicQueryMethodReturn.builder()
                .args(params)
                .methodName(method.name())
                .returnType(method.returnType().orElseThrow())
                .querySupplier(() -> queryValue)
                .paramsSupplier(() -> RepositoryMetadataUtils.INSTANCE.getParams(method, params))
                .typeClass(type)
                .pageRequest(pageRequest)
                .mapper(sqlReturnType.mapper(method, entityMetadata))
                .prepareConverter(textQuery -> {
                    SqlPreparedStatement prepare = (SqlPreparedStatement) template.prepare(textQuery, entity);
                    prepare.setSelectMapper(query -> {
                        SelectQuery selectQuery = SqlQueryBuilder.updateQuery(context, method, query);
                        if(returnElementType.isPresent() && entitiesMetadata.projection(returnElementType.orElseThrow()).isPresent()) {
                            return SqlSelectQuery.of(selectQuery, returnElementType.orElseThrow());
                        }
                        return selectQuery;
                    });
                    return prepare;
                }).build();
        return (T) methodReturn.execute();

    }
}
