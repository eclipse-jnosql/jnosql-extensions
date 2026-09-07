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

import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;

import java.util.Objects;

/**
 * Step builder for creating {@link QueryOperation} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * QueryOperation operation = SemistructuredQueryOperationBuilder.builder()
 *         .withQueryBuilder(queryBuilder)
 *         .withReturnType(returnType)
 *         .withEntities(entitiesMetadata)
 *         .build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withXxx()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public sealed interface SemistructuredQueryOperationBuilder
        permits SemistructuredQueryOperationBuilder.Step1,
        SemistructuredQueryOperationBuilder.Step2,
        SemistructuredQueryOperationBuilder.Step3 {

    /**
     * Returns a new builder starting from {@link Step1}.
     *
     * @return the first step of the builder
     */
    static Step1 builder() {
        return new Step1(null, null, null);
    }

    record Step1(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 org.eclipse.jnosql.mapping.metadata.EntitiesMetadata entitiesMetadata) implements SemistructuredQueryOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, entitiesMetadata);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, entitiesMetadata);
        }

        public Step3 withEntities(org.eclipse.jnosql.mapping.metadata.EntitiesMetadata entitiesMetadata) {
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }
    }

    record Step2(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 org.eclipse.jnosql.mapping.metadata.EntitiesMetadata entitiesMetadata) implements SemistructuredQueryOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, entitiesMetadata);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, entitiesMetadata);
        }

        public Step3 withEntities(org.eclipse.jnosql.mapping.metadata.EntitiesMetadata entitiesMetadata) {
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }
    }

    record Step3(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 org.eclipse.jnosql.mapping.metadata.EntitiesMetadata entitiesMetadata) implements SemistructuredQueryOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, entitiesMetadata);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, entitiesMetadata);
        }

        public Step3 withEntities(org.eclipse.jnosql.mapping.metadata.EntitiesMetadata entitiesMetadata) {
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }

        public QueryOperation build() {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            Objects.requireNonNull(returnType, "returnType is required");
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new SemistructuredQueryOperation(queryBuilder, returnType, entitiesMetadata);
        }
    }
}