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
package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.mapping.core.Converters;

import java.util.Objects;

/**
 * Step builder for creating {@link SemistructuredQueryBuilder} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * SemistructuredQueryBuilder builder = SemistructuredQueryBuilderBuilder.builder()
 *         .withConverters(converters)
 *         .build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withConverters()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public sealed interface SemistructuredQueryBuilderBuilder
        permits SemistructuredQueryBuilderBuilder.Step1 {

    /**
     * Returns a new builder starting from {@link Step1}.
     *
     * @return the first step of the builder
     */
    static Step1 builder() {
        return new Step1(null);
    }

    record Step1(Converters converters) implements SemistructuredQueryBuilderBuilder {

        public Step1 withConverters(Converters converters) {
            Objects.requireNonNull(converters, "converters is required");
            return new Step1(converters);
        }

        public SemistructuredQueryBuilder build() {
            Objects.requireNonNull(converters, "converters is required");
            return new SemistructuredQueryBuilder(converters);
        }
    }
}