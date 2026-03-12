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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import jakarta.nosql.Template;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;

class PersistenceRepositoryInvocationHandler<T, K>  extends CoreRepositoryInvocationHandler<T, K> {

    private PersistenceRepositoryInvocationHandler(
            AbstractRepository<T, K> repository,
            EntityMetadata entityMetadata,
            RepositoryMetadata repositoryMetadata,
            InfrastructureOperatorProvider infrastructureOperatorProvider,
            RepositoryOperationProvider repositoryOperationProvider,
            PersistenceDocumentTemplate template
    ) {
        super(repository, entityMetadata, repositoryMetadata, infrastructureOperatorProvider, repositoryOperationProvider, template);
    }


    /**
     * Creates a new {@code PersistenceRepositoryInvocationHandler} with the components
     * required to resolve and execute Jakarta Data repository methods through the
     * Persistence engine.
     *
     * @param repository                       the repository implementation that provides CRUD semantics
     * @param entityMetadata                   metadata describing the entity type managed by the repository
     * @param repositoryMetadata               metadata describing the Jakarta Data repository interface
     * @param infrastructureOperatorProvider   provider for executing built-in or custom infrastructure methods
     * @param repositoryOperationProvider      provider for executing semantic repository operations
     * @param template                         the underlying NoSQL {@link Template} used for persistence
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T, K> PersistenceRepositoryInvocationHandler<T, K> of(
            AbstractRepository<T, K> repository,
            EntityMetadata entityMetadata,
            RepositoryMetadata repositoryMetadata,
            InfrastructureOperatorProvider infrastructureOperatorProvider,
            RepositoryOperationProvider repositoryOperationProvider,
            PersistenceDocumentTemplate template
    ) {
        return new PersistenceRepositoryInvocationHandler<>(
                repository,
                entityMetadata,
                repositoryMetadata,
                infrastructureOperatorProvider,
                repositoryOperationProvider,
                template
        );
    }
}
