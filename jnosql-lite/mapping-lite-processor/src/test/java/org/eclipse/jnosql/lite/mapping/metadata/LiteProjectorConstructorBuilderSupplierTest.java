/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 which accompanies this distribution.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.ProjectionConstructorMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LiteProjectorConstructorBuilderSupplierTest {

    @Nested
    @DisplayName("When creating a projection builder")
    class WhenTheBuilderIsCreated {

        @Test
        @DisplayName("Should create a reflection-free builder")
        void shouldCreateAReflectionFreeBuilder() {
            var metadata = mock(LiteProjectorConstructorMetadata.class);
            var supplier = new LiteProjectorConstructorBuilderSupplier();

            var result = supplier.apply(metadata);

            assertThat(result)
                    .as("projection constructor builder")
                    .isInstanceOf(LiteProjectorConstructorBuilder.class);
        }

        @Test
        @DisplayName("Should reject reflection-based metadata")
        void shouldRejectReflectionBasedMetadata() {
            var metadata = mock(ProjectionConstructorMetadata.class);
            var supplier = new LiteProjectorConstructorBuilderSupplier();

            assertThatThrownBy(() -> supplier.apply(metadata))
                    .as("reflection-based projection metadata")
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("does not support reflection");
        }

        @Test
        @DisplayName("Should reject null metadata")
        void shouldRejectNullMetadata() {
            var supplier = new LiteProjectorConstructorBuilderSupplier();

            assertThatNullPointerException()
                    .as("null projection constructor metadata")
                    .isThrownBy(() -> supplier.apply(null))
                    .withMessage("constructorMetadata is required");
        }
    }
}
