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
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
@Typed(SqlFindByOperation.class)
class SqlFindByOperation implements FindByOperation {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        String name = method.name();

    }


    private SelectQuery getSelectQuery(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        var entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method.name(), entityMetadata.name());
        var observer = semistructuredQueryBuilder.observer(entityMetadata);
        var paramsBinder = semistructuredQueryBuilder.paramsBinder(entityMetadata);
        var queryParams = SELECT_PARSER.apply(selectQuery, observer);
        var query = queryParams.query();
        var params = queryParams.params();
        paramsBinder.bind(params, parameters, method.name());
        return semistructuredQueryBuilder.updateDynamicQuery(query, context);
    }
}
