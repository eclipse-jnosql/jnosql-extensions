/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.nosql.Template;
import org.eclipse.jnosql.extensions.sql.SqlEntityMetadata;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.core.repository.AbstractRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;

final class SqlInvocationHandler<T, K>  extends AbstractRepositoryInvocationHandler<T, K>  {

    private final PersistenceRepository<T, K> repository;
    private final SqlEntityMetadata entityMetadata;
    private final SqlTemplate sqlTemplate;
    private final RepositoryMetadata repositoryMetadata;
    private final InfrastructureOperatorProvider infrastructureOperatorProvider;
    private final RepositoryOperationProvider repositoryOperationProvider;

    SqlInvocationHandler(PersistenceRepository<T, K> repository,
                                SqlEntityMetadata entityMetadata,
                                SqlTemplate sqlTemplate, RepositoryMetadata repositoryMetadata,
                                InfrastructureOperatorProvider infrastructureOperatorProvider,
                                RepositoryOperationProvider repositoryOperationProvider) {
        this.repository = repository;
        this.entityMetadata = entityMetadata;
        this.sqlTemplate = sqlTemplate;
        this.repositoryMetadata = repositoryMetadata;
        this.infrastructureOperatorProvider = infrastructureOperatorProvider;
        this.repositoryOperationProvider = repositoryOperationProvider;
    }

    @Override
    protected AbstractRepository<T, K> repository() {
        return repository;
    }

    @Override
    protected SqlEntityMetadata entityMetadata() {
        return entityMetadata;
    }

    @Override
    protected RepositoryMetadata repositoryMetadata() {
        return repositoryMetadata;
    }

    @Override
    protected InfrastructureOperatorProvider infrastructureOperatorProvider() {
        return infrastructureOperatorProvider;
    }

    @Override
    protected RepositoryOperationProvider repositoryOperationProvider() {
        return repositoryOperationProvider;
    }

    @Override
    protected Template template() {
        return sqlTemplate;
    }

}
