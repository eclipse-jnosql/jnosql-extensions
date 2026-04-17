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

import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;

import java.util.Objects;

/**
 * Step builder for creating {@link FindByOperation} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * FindByOperation operation = SemistructuredFindByOperationBuilder.builder()
 *         .withQueryBuilder(queryBuilder)
 *         .withReturnType(returnType)
 *         .build();
 * }</pre>
 * </p>
 *
 * <p>
 * Each step is immutable. Calling {@code withXxx()} returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 */
public sealed interface SemistructuredFindByOperationBuilder
        permits SemistructuredFindByOperationBuilder.Step1,
        SemistructuredFindByOperationBuilder.Step2 {

    /**
     * Returns a new builder starting from {@link Step1}.
     *
     * @return the first step of the builder
     */
    static Step1 builder() {
        return new Step1(null, null);
    }

    record Step1(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType) implements SemistructuredFindByOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType);
        }
    }

    record Step2(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType) implements SemistructuredFindByOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType);
        }

        public FindByOperation build() {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            Objects.requireNonNull(returnType, "returnType is required");
            return new SemistructuredFindByOperation(queryBuilder, returnType);
        }
    }
}