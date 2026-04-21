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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
@Typed(SqlDeleteByOperation.class)
class SqlDeleteByOperation implements DeleteByOperation {

    private final SqlQueryBuilder sqlQueryBuilder;

    @Inject
    SqlDeleteByOperation(SqlQueryBuilder sqlQueryBuilder) {
        this.sqlQueryBuilder = sqlQueryBuilder;
    }

    SqlDeleteByOperation() {
        this.sqlQueryBuilder = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var returnType = method.returnType().orElse(void.class);
        var template = (SqlTemplate) context.template();
        var deleteQuery = sqlQueryBuilder.deleteQuery(context);
        if (returnType.equals(void.class) || returnType.equals(Void.class)) {
            template.delete(deleteQuery);
            return (T) Void.class;
        }
        long count = template.deleteWithCount(deleteQuery);
        return (T) Value.of(count).get(returnType);
    }
}
