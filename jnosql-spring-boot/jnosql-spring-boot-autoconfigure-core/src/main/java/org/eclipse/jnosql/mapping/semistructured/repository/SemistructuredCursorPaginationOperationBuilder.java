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
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;

import java.util.Objects;

/**
 * Step builder for creating {@link CursorPaginationOperation} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * CursorPaginationOperation operation = SemistructuredCursorPaginationOperationBuilder.builder()
 *         .withQueryBuilder(queryBuilder)
 *         .withReturnType(returnType)
 *         .withConverters(converters)
 *         .build();
 * }</pre>
 * </p>
 *
 * <p>
 * Each step is immutable. Calling {@code withXxx()} returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 */
public sealed interface SemistructuredCursorPaginationOperationBuilder
        permits SemistructuredCursorPaginationOperationBuilder.Step1,
        SemistructuredCursorPaginationOperationBuilder.Step2 {

    static Step1 builder() {
        return new Step1(null, null, null);
    }

    record Step1(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 Converters converters) implements SemistructuredCursorPaginationOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, converters);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, converters);
        }
    }

    record Step2(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 Converters converters) implements SemistructuredCursorPaginationOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, converters);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, converters);
        }

        public CursorPaginationOperation build() {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            Objects.requireNonNull(returnType, "returnType is required");
            return new SemistructuredCursorPaginationOperation(queryBuilder, returnType);
        }
    }
}