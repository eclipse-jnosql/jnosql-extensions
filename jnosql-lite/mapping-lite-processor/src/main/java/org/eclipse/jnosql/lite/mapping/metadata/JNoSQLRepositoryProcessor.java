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
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Internal processor responsible for executing repository operations generated
 * by the JNoSQL annotation processor.
 *
 * <p>This processor serves as the runtime execution engine for repository
 * implementations generated at compile time. Instead of relying on dynamic
 * proxies and {@link java.lang.reflect.InvocationHandler}, the generated
 * repository classes delegate method execution directly to this processor.</p>
 *
 * <p>The processor resolves a repository method using a {@link MethodSignatureKey}
 * and the available {@link RepositoryMetadata}. Once the corresponding
 * {@link RepositoryMethod} is located, the invocation is translated into a
 * {@link RepositoryInvocationContext} and delegated to the appropriate
 * operation provided by {@link RepositoryOperationProvider}.</p>
 *
 * <p>The {@link Template} instance acts as the central infrastructure component
 * responsible for communicating with the underlying NoSQL database.</p>
 *
 * <p>This class is part of the JNoSQL Lite internal infrastructure and is not
 * intended to be used directly by application code. Applications should
 * interact with repository interfaces instead.</p>
 */
public final class JNoSQLRepositoryProcessor {

    private static final Logger LOGGER = Logger.getLogger(JNoSQLRepositoryProcessor.class.getName());

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
     * Executes a repository method that produces a result.
     *
     * <p>The provided {@link MethodSignatureKey} uniquely identifies the repository
     * method previously analyzed during metadata generation. The parameters
     * correspond to the arguments supplied when invoking the generated
     * repository method.</p>
     *
     * <p>The processor resolves the associated {@link RepositoryMethod},
     * constructs a {@link RepositoryInvocationContext}, and delegates execution
     * to the appropriate operation supplied by {@link RepositoryOperationProvider}.</p>
     *
     * @param methodSignatureKey the identifier representing the repository method
     * @param params the arguments passed to the repository method invocation
     * @param <T> the expected return type of the repository method
     * @return the result produced by the repository operation
     * @throws NullPointerException if {@code methodSignatureKey} or {@code params} is {@code null}
     * @throws IllegalArgumentException if the repository method cannot be found
     *                                  or if the operation type is unsupported
     */
    public <T> T execute(MethodSignatureKey methodSignatureKey, Object[] params) {
        Objects.requireNonNull(methodSignatureKey, "methodSignatureKey is required");
        Objects.requireNonNull(params, "params is required");
        LOGGER.finest(() -> "Executing repository method: " + methodSignatureKey);
        var repositoryMethod = repositoryMetadata.find(methodSignatureKey).orElseThrow(() -> new IllegalArgumentException("Method not found:" +
                " " + methodSignatureKey));

        var context = repositoryInvocationContext(params, repositoryMethod);
        return switch (repositoryMethod.type()) {
            case INSERT -> repositoryOperationProvider.insertOperation().execute(context);
            case UPDATE -> repositoryOperationProvider.updateOperation().execute(context);
            case DELETE -> repositoryOperationProvider.deleteOperation().execute(context);
            case SAVE ->  repositoryOperationProvider.saveOperation().execute(context);
            case DELETE_BY -> repositoryOperationProvider.deleteByOperation().execute(context);
            case FIND_BY -> repositoryOperationProvider.findByOperation().execute(context);
            case COUNT_ALL -> repositoryOperationProvider.countAllOperation().execute(context);
            case COUNT_BY -> repositoryOperationProvider.countByOperation().execute(context);
            case CURSOR_PAGINATION -> repositoryOperationProvider.cursorPaginationOperation().execute(context);
            case PARAMETER_BASED -> repositoryOperationProvider.parameterBasedOperation().execute(context);
            case EXISTS_BY -> repositoryOperationProvider.existsByOperation().execute(context);
            case FIND_ALL -> repositoryOperationProvider.findAllOperation().execute(context);
            case QUERY -> repositoryOperationProvider.queryOperation().execute(context);
            case PROVIDER_OPERATION -> repositoryOperationProvider.providerOperation().execute(context);
            default -> throw new IllegalArgumentException("Unsupported repository operation: " + repositoryMethod.type()
                    + " for method: " + methodSignatureKey);
        };
    }

    /**
     * Executes a repository method that does not produce a return value.
     *
     * <p>This method delegates to {@link #execute(MethodSignatureKey, Object[])}
     * and ignores the returned value.</p>
     *
     * @param methodSignatureKey the identifier representing the repository method
     * @param params the arguments passed to the repository method invocation
     */
    public void executeVoid(MethodSignatureKey methodSignatureKey, Object[] params) {
        execute(methodSignatureKey, params);
    }

    private RepositoryInvocationContext repositoryInvocationContext(Object[] params, RepositoryMethod method) {
        return new RepositoryInvocationContext(method, repositoryMetadata, entityMetadata, template, params);
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
