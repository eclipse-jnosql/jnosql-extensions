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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.reflection.repository.ReflectionRepositorySupplier;

import java.lang.reflect.Proxy;
import java.util.Objects;

@ApplicationScoped
class SqlRepositoryProducer {

    private final InfrastructureOperatorProvider infrastructureOperatorProvider;
    private final SqlRepositoryOperationProvider repositoryOperationProvider;

    @Inject
    SqlRepositoryProducer(InfrastructureOperatorProvider infrastructureOperatorProvider,
                          SqlRepositoryOperationProvider repositoryOperationProvider) {
        this.infrastructureOperatorProvider = infrastructureOperatorProvider;
        this.repositoryOperationProvider = repositoryOperationProvider;
    }


    /**
     * Returns a fully functional repository implementation for the given
     * repository interface.
     *
     * @param repositoryClass the repository interface to implement
     * @param template the semistructured template used by the repository
     * @param <R> the repository type
     * @return an instance implementing the given repository interface
     * @throws NullPointerException if any argument is {@code null}
     * @throws java.util.NoSuchElementException if required repository or entity
     *         metadata cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public <R> R get(Class<?> repositoryClass, SqlTemplate template) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template is required");
        RepositoryMetadata repositoryMetadata = ReflectionRepositorySupplier.INSTANCE.apply(repositoryClass);
        var entity = RepositoryEntityResolver.INSTANCE.resolveEntityType(repositoryClass);
        SqlRepositoryAdapter<?, ?> repositoryAdapter = new SqlRepositoryAdapter<>(entity, template);
        var entityMetadata = repositoryAdapter.metadata();

        SqlInvocationHandler<?, ?> repositoryHandler = new SqlInvocationHandler<>(repositoryAdapter,
                entityMetadata, template,
                repositoryMetadata,
                infrastructureOperatorProvider,
                repositoryOperationProvider);

        return (R) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class[]{repositoryClass},
                repositoryHandler);
    }
}
