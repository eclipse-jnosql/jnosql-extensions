/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping;

import org.eclipse.jnosql.lite.mapping.metadata.LiteConstructorBuilderSupplier;
import org.eclipse.jnosql.lite.mapping.metadata.LiteConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LiteConstructorBuilderSupplierTest {

    @Nested
    @DisplayName("When creating a constructor builder")
    class WhenTheBuilderIsCreated {

        @Test
        @DisplayName("Should create a reflection-free builder")
        void shouldCreateAReflectionFreeBuilder() {
            var metadata = mock(LiteConstructorMetadata.class);
            var supplier = new LiteConstructorBuilderSupplier();

            var result = supplier.apply(metadata);

            assertThat(result)
                    .as("constructor builder")
                    .isInstanceOf(org.eclipse.jnosql.lite.mapping.metadata.LiteConstructorBuilder.class);
        }

        @Test
        @DisplayName("Should reject reflection-based metadata")
        void shouldRejectReflectionBasedMetadata() {
            var metadata = mock(ConstructorMetadata.class);
            var supplier = new LiteConstructorBuilderSupplier();

            assertThatThrownBy(() -> supplier.apply(metadata))
                    .as("reflection-based constructor metadata")
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("does not support reflection");
        }

        @Test
        @DisplayName("Should reject null metadata")
        void shouldRejectNullMetadata() {
            var supplier = new LiteConstructorBuilderSupplier();

            assertThatNullPointerException()
                    .as("null constructor metadata")
                    .isThrownBy(() -> supplier.apply(null))
                    .withMessage("constructorMetadata is required");
        }
    }
}
