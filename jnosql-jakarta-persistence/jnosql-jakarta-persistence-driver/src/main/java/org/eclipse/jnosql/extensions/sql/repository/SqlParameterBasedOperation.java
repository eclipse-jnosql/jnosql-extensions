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
import jakarta.inject.Inject;
import org.eclipse.jnosql.extensions.sql.SqlEntityMetadata;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.core.repository.ParamValue;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Map;

@ApplicationScoped
class SqlParameterBasedOperation implements ParameterBasedOperation {

    private final SqlReturnType sqlReturnType;

    @Inject
    SqlParameterBasedOperation(SqlReturnType sqlReturnType) {
        this.sqlReturnType = sqlReturnType;
    }

    SqlParameterBasedOperation() {
        this.sqlReturnType = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var template = (SqlTemplate) context.template();
        var entityMetadata = method.find().filter(r -> !void.class.equals(r))
                .map(r -> (EntityMetadata) SqlEntityMetadata.of(r, template.entityManager()))
                .orElse(context.entityMetadata());
        var parameters = context.parameters();
        Map<String, ParamValue> paramValueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, parameters);
        var query = SqlParameterBasedQuery.INSTANCE.toQuery(paramValueMap, entityMetadata);
        var selectQuery = SqlQueryBuilder.updateQuery(context, method, query);
        return (T) sqlReturnType.executeFindByQuery(context, selectQuery);
    }
}
