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
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

@ApplicationScoped
@Typed(SqlFindAllOperation.class)
class SqlFindAllOperation implements FindAllOperation {

    private static final String[] EMPTY = new String[0];

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        RepositoryMethod method = context.method();
        var query = SelectQuery.select(method.select().toArray(EMPTY)).from(entityMetadata.name()).build();

        throw new UnsupportedOperationException("FindAllOperation is not supported by SQL extension");
    }
}
