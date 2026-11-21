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

import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryResolver;

import java.util.Objects;

/**
 * Step builder for creating {@link CustomRepositoryMethodOperator} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * CustomRepositoryMethodOperator operator = DefaultCustomRepositoryMethodOperatorBuilder.builder()
 *         .withResolver(resolver)
 *         .build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withResolver()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public sealed interface DefaultCustomRepositoryMethodOperatorBuilder
        permits DefaultCustomRepositoryMethodOperatorBuilder.Step1 {

    /**
     * Returns a new builder starting from {@link Step1}.
     *
     * @return the first step of the builder
     */
    static Step1 builder() {
        return new Step1(null);
    }

    record Step1(CustomRepositoryResolver resolver) implements DefaultCustomRepositoryMethodOperatorBuilder {

        public Step1 withResolver(CustomRepositoryResolver resolver) {
            Objects.requireNonNull(resolver, "resolver is required");
            return new Step1(resolver);
        }

        public CustomRepositoryMethodOperator build() {
            Objects.requireNonNull(resolver, "resolver is required");
            return new DefaultCustomRepositoryMethodOperator(resolver);
        }
    }
}