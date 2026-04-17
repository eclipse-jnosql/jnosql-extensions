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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.core.repository.operations;

import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.eclipse.jnosql.mapping.core.repository.ProviderQueryHandlerResolver;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryAnnotation;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.List;

/**
 * Provider operation implementation using a {@link ProviderQueryHandlerResolver}
 * instead of CDI {@code Instance<ProviderQueryHandler>}.
 *
 * <p>
 * This class is a copy of the CDI-based {@code CoreProviderOperation} from
 * {@code jnosql-mapping-core}, adapted to work outside a CDI container.
 * </p>
 *
 * @see ProviderQueryHandlerResolver
 */
class CoreProviderOperation implements ProviderOperation {

    private final ProviderQueryHandlerResolver resolver;

    CoreProviderOperation(ProviderQueryHandlerResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        RepositoryMethod method = context.method();
        List<RepositoryAnnotation> annotations = method.annotations();
        var providerAnnotation = annotations.stream()
                .filter(RepositoryAnnotation::isProviderAnnotation)
                .findFirst()
                .orElseThrow(() -> new DynamicQueryException("No provider annotation found on method: " + method.name()));

        String provider = providerAnnotation.provider().orElseThrow(() -> new DynamicQueryException("Provider annotation missing identifier on method: " + method.name()));

        if (resolver == null) {
            throw new DynamicQueryException(
                    "Cannot resolve ProviderQueryHandler for provider '" + provider + "' " +
                            "required by repository method '" + method.name() + "'. " +
                            "ProviderQueryHandlerResolver is not available."
            );
        }

        ProviderQueryHandler handler = resolver.resolve();
        if (handler == null) {
            throw new DynamicQueryException(
                    "Cannot resolve ProviderQueryHandler for provider '" + provider + "' " +
                            "required by repository method '" + method.name() + "'. " +
                            "Resolver returned null."
            );
        }
        return handler.execute(context);
    }
}