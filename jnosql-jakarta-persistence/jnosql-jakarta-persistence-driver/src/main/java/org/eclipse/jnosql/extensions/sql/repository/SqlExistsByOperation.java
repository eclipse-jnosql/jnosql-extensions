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
import org.eclipse.jnosql.mapping.metadata.repository.spi.ExistsByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

@ApplicationScoped
class SqlExistsByOperation implements ExistsByOperation {

    private final SqlQueryBuilder sqlQueryBuilder;

    @Inject
    SqlExistsByOperation(SqlQueryBuilder sqlQueryBuilder) {
        this.sqlQueryBuilder = sqlQueryBuilder;
    }

    SqlExistsByOperation() {
        this.sqlQueryBuilder = null;
    }

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var selectQuery = this.sqlQueryBuilder.selectQuery(context);
        var template = (SemiStructuredTemplate) context.template();
        return (T) (Boolean) template.exists(selectQuery);
    }
}
