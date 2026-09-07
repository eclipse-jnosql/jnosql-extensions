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

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;

import java.util.Objects;

/**
 * Step builder for creating {@link SemistructuredReturnType} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * SemistructuredReturnType returnType = SemistructuredReturnTypeBuilder.builder()
 *         .withEntities(entitiesMetadata)
 *         .withProjector(projectorConverter)
 *         .build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withXxx()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public sealed interface SemistructuredReturnTypeBuilder
        permits SemistructuredReturnTypeBuilder.Step1,
        SemistructuredReturnTypeBuilder.Step2 {

    /**
     * Returns a new builder starting from {@link Step1}.
     *
     * @return the first step of the builder
     */
    static Step1 builder() {
        return new Step1(null, null);
    }

    record Step1(EntitiesMetadata entities, ProjectorConverter projector) implements SemistructuredReturnTypeBuilder {

        public Step1 withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new Step1(entities, projector);
        }

        public Step2 withProjector(ProjectorConverter projector) {
            Objects.requireNonNull(projector, "projector is required");
            return new Step2(entities, projector);
        }
    }

    record Step2(EntitiesMetadata entities, ProjectorConverter projector) implements SemistructuredReturnTypeBuilder {

        public Step1 withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new Step1(entities, projector);
        }

        public Step2 withProjector(ProjectorConverter projector) {
            Objects.requireNonNull(projector, "projector is required");
            return new Step2(entities, projector);
        }

        public SemistructuredReturnType build() {
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(projector, "projector is required");
            return new SemistructuredReturnType(entities, projector);
        }
    }
}