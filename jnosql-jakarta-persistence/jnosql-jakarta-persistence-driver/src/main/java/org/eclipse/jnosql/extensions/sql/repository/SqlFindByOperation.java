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

import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Optional;
import java.util.function.Function;

@ApplicationScoped
@Typed(SqlFindByOperation.class)
class SqlFindByOperation implements FindByOperation {

    private final SqlQueryBuilder sqlQueryBuilder;
    private final SqlReturnType sqlReturnType;

    @Inject
    SqlFindByOperation(SqlQueryBuilder sqlQueryBuilder, SqlReturnType sqlReturnType) {
        this.sqlQueryBuilder = sqlQueryBuilder;
        this.sqlReturnType = sqlReturnType;
    }

    SqlFindByOperation() {
        this.sqlReturnType = null;
        this.sqlQueryBuilder = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var specialParameters = SpecialParameters.of(context.parameters(), Function.identity());
        Optional<Restriction<?>> restriction = specialParameters.restriction();
        var selectQuery = sqlQueryBuilder.selectQuery(context);
        return (T) sqlReturnType.executeFindByQuery(context, selectQuery);
    }

}
