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
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;

import java.util.Objects;

/**
 * Step builder for creating {@link ParameterBasedOperation} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * ParameterBasedOperation operation = SemistructuredParameterBasedOperationBuilder.builder()
 *         .withQueryBuilder(queryBuilder)
 *         .withReturnType(returnType)
 *         .withEntities(entitiesMetadata)
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
public sealed interface SemistructuredParameterBasedOperationBuilder
        permits SemistructuredParameterBasedOperationBuilder.Step1,
        SemistructuredParameterBasedOperationBuilder.Step2,
        SemistructuredParameterBasedOperationBuilder.Step3 {

    static Step1 builder() {
        return new Step1(null, null, null, null);
    }

    record Step1(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 EntitiesMetadata entitiesMetadata,
                 Converters converters) implements SemistructuredParameterBasedOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step3 withEntities(EntitiesMetadata entitiesMetadata) {
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata, converters);
        }

    }

    record Step2(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 EntitiesMetadata entitiesMetadata,
                 Converters converters) implements SemistructuredParameterBasedOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step3 withEntities(EntitiesMetadata entitiesMetadata) {
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata, converters);
        }
    }

    record Step3(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 EntitiesMetadata entitiesMetadata,
                 Converters converters) implements SemistructuredParameterBasedOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step1(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            Objects.requireNonNull(returnType, "returnType is required");
            return new Step2(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step3 withEntities(EntitiesMetadata entitiesMetadata) {
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public ParameterBasedOperation build() {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            Objects.requireNonNull(returnType, "returnType is required");
            Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            return new SemistructuredParameterBasedOperation(queryBuilder, returnType, entitiesMetadata);
        }
    }
}