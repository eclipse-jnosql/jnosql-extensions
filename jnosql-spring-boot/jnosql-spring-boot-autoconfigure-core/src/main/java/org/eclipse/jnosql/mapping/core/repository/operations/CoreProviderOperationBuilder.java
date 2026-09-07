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

import org.eclipse.jnosql.mapping.core.repository.ProviderQueryHandlerResolver;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;

import java.util.Objects;

/**
 * Step builder for creating {@link ProviderOperation} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * ProviderOperation operation = CoreProviderOperationBuilder.builder()
 *         .withResolver(resolver)
 *         .build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withResolver()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public sealed interface CoreProviderOperationBuilder
        permits CoreProviderOperationBuilder.Step1 {

    /**
     * Returns a new builder starting from {@link Step1}.
     *
     * @return the first step of the builder
     */
    static Step1 builder() {
        return new Step1(null);
    }

    record Step1(ProviderQueryHandlerResolver resolver) implements CoreProviderOperationBuilder {

        public Step1 withResolver(ProviderQueryHandlerResolver resolver) {
            Objects.requireNonNull(resolver, "resolver is required");
            return new Step1(resolver);
        }

        public ProviderOperation build() {
            Objects.requireNonNull(resolver, "resolver is required");
            return new CoreProviderOperation(resolver);
        }
    }
}