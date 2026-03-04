/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.MethodSignatureKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;

import java.util.Objects;

/**
 * Internal processor responsible for executing repository operations generated
 * by the JNoSQL annotation processor.
 *
 * <p>This class acts as a runtime delegate used by compile-time generated
 * repository implementations. Instead of relying on dynamic proxies and
 * {@link java.lang.reflect.InvocationHandler}, generated repositories invoke
 * this processor directly to execute repository methods.</p>
 *
 * <p>The processor uses {@link RepositoryMetadata} and {@link EntityMetadata}
 * to resolve the repository method represented by a {@link MethodSignatureKey}.
 * Once resolved, the execution is delegated to the appropriate operation
 * provided by {@link RepositoryOperationProvider}.</p>
 *
 * <p>The {@link Template} instance is used as the central infrastructure
 * component responsible for interacting with the underlying data store.</p>
 *
 * <p>This class is part of the JNoSQL Lite infrastructure and is intended
 * for internal framework usage only. Application code should interact with
 * repositories rather than invoking this processor directly.</p>
 */
public final class JNoSQLRepositoryProcessor {

    private final Template template;

    private final EntityMetadata entityMetadata;

    private final RepositoryMetadata repositoryMetadata;

    private final RepositoryOperationProvider repositoryOperationProvider;

    private JNoSQLRepositoryProcessor(Template template, EntityMetadata entityMetadata,
                                RepositoryMetadata repositoryMetadata,
                                     RepositoryOperationProvider repositoryOperationProvider) {
        this.template = template;
        this.entityMetadata = entityMetadata;
        this.repositoryMetadata = repositoryMetadata;
        this.repositoryOperationProvider = repositoryOperationProvider;
    }


    /**
     * Executes a repository method that returns a result.
     *
     * <p>The {@link MethodSignatureKey} identifies the repository method
     * previously analyzed during metadata creation. The provided parameters
     * correspond to the arguments supplied to the repository method call.</p>
     *
     * @param methodSignatureKey the identifier representing the repository method
     * @param params the parameters passed to the repository method
     * @param <T> the return type of the repository method
     * @return the result produced by the corresponding repository operation
     */
    public <T> T execute(MethodSignatureKey methodSignatureKey, Object[] params) {
        return null;
    }

    /**
     * Executes a repository method that does not return a value.
     *
     * @param methodSignatureKey the identifier representing the repository method
     * @param params the parameters passed to the repository method
     */
    public void executeVoid(MethodSignatureKey methodSignatureKey, Object[] params) {
    }




    /**
     * Creates a new {@link JNoSQLRepositoryProcessor}.
     *
     * <p>This factory method ensures that all required infrastructure
     * components are provided before instantiating the processor.</p>
     *
     * @param template the template used to interact with the data store
     * @param entityMetadata metadata describing the managed entity
     * @param repositoryMetadata metadata describing the repository structure
     * @param repositoryOperationProvider provider responsible for executing repository operations
     * @return a new {@link JNoSQLRepositoryProcessor} instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static JNoSQLRepositoryProcessor of(Template template,
                                               EntityMetadata entityMetadata,
                                               RepositoryMetadata repositoryMetadata,
                                               RepositoryOperationProvider repositoryOperationProvider) {

        Objects.requireNonNull(template, "template is required");
        Objects.requireNonNull(entityMetadata, "entityMetadata is required");
        Objects.requireNonNull(repositoryMetadata, "repositoryMetadata is required");
        Objects.requireNonNull(repositoryOperationProvider, "repositoryOperationProvider is required");

        return new JNoSQLRepositoryProcessor(template, entityMetadata, repositoryMetadata, repositoryOperationProvider);
    }
}
