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
        permits SemistructuredParameterBasedOperationBuilder.Step0,
        SemistructuredParameterBasedOperationBuilder.Step1,
        SemistructuredParameterBasedOperationBuilder.Step2,
        SemistructuredParameterBasedOperationBuilder.Step3,
        SemistructuredParameterBasedOperationBuilder.Step4 {

    static Step0 builder() {
        return new Step0();
    }

    record Step0() implements SemistructuredParameterBasedOperationBuilder {

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            return new Step1(queryBuilder);
        }
    }

    record Step1(SemistructuredQueryBuilder queryBuilder) implements SemistructuredParameterBasedOperationBuilder {

        public Step1 {
            queryBuilder = Objects.requireNonNull(queryBuilder, "queryBuilder is required");
        }

        public Step1 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            return new Step1(queryBuilder);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            return new Step2(queryBuilder, returnType);
        }

    }

    record Step2(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType) implements SemistructuredParameterBasedOperationBuilder {

        public Step2 {
            queryBuilder = Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            returnType = Objects.requireNonNull(returnType, "returnType is required");
        }

        public Step2 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            return new Step2(queryBuilder, returnType);
        }

        public Step2 withReturnType(SemistructuredReturnType returnType) {
            return new Step2(queryBuilder, returnType);
        }

        public Step3 withEntities(EntitiesMetadata entitiesMetadata) {
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }
    }

    record Step3(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 EntitiesMetadata entitiesMetadata) implements SemistructuredParameterBasedOperationBuilder {

        public Step3 {
            queryBuilder = Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            returnType = Objects.requireNonNull(returnType, "returnType is required");
            entitiesMetadata = Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
        }

        public Step3 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }

        public Step3 withReturnType(SemistructuredReturnType returnType) {
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }

        public Step3 withEntities(EntitiesMetadata entitiesMetadata) {
            return new Step3(queryBuilder, returnType, entitiesMetadata);
        }

        public Step4 withConverters(Converters converters) {
            return new Step4(queryBuilder, returnType, entitiesMetadata, converters);
        }
    }

    record Step4(SemistructuredQueryBuilder queryBuilder,
                 SemistructuredReturnType returnType,
                 EntitiesMetadata entitiesMetadata,
                 Converters converters) implements SemistructuredParameterBasedOperationBuilder {

        public Step4 {
            queryBuilder = Objects.requireNonNull(queryBuilder, "queryBuilder is required");
            returnType = Objects.requireNonNull(returnType, "returnType is required");
            entitiesMetadata = Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
            converters = Objects.requireNonNull(converters, "converters is required");
        }

        public Step4 withQueryBuilder(SemistructuredQueryBuilder queryBuilder) {
            return new Step4(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step4 withReturnType(SemistructuredReturnType returnType) {
            return new Step4(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step4 withEntities(EntitiesMetadata entitiesMetadata) {
            return new Step4(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public Step4 withConverters(Converters converters) {
            return new Step4(queryBuilder, returnType, entitiesMetadata, converters);
        }

        public ParameterBasedOperation build() {
            return new SemistructuredParameterBasedOperation(queryBuilder, returnType, entitiesMetadata, converters);
        }
    }
}