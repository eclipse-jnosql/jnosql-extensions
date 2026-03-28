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

import jakarta.data.Sort;
import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.nosql.Template;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ApplicationScoped
@Typed(SqlFindByOperation.class)
class SqlFindByOperation implements FindByOperation {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();
    private final SqlQueryBuilder sqlQueryBuilder;

    @Inject
    SqlFindByOperation(SqlQueryBuilder sqlQueryBuilder) {
        this.sqlQueryBuilder = sqlQueryBuilder;
    }

    SqlFindByOperation() {
        this.sqlQueryBuilder = null;
    }

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        var template = (SqlTemplate) context.template();
        var selectQuery = sqlQueryBuilder.selectQuery(context);
        var specialParameters = SpecialParameters.of(context.parameters(), Function.identity());
        Optional<Restriction<?>> restriction = specialParameters.restriction();
        return null;

    }
}
