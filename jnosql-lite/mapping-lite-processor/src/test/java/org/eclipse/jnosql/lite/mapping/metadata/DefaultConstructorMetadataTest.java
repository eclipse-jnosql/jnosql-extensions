/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;

class DefaultConstructorMetadataTest {

    @Nested
    @DisplayName("When creating constructor metadata")
    class WhenTheMetadataIsCreated {

        @Test
        @DisplayName("Should expose the constructor definition")
        void shouldExposeTheConstructorDefinition() {
            var parameter = mock(ParameterMetaData.class);

            var metadata = DefaultConstructorMetadata.of(false, List.of(parameter));

            assertSoftly(softly -> {
                softly.assertThat(metadata.isDefault())
                        .as("default constructor flag")
                        .isFalse();
                softly.assertThat(metadata.parameters())
                        .as("constructor parameters")
                        .containsExactly(parameter);
            });
        }

        @Test
        @DisplayName("Should expose empty default metadata")
        void shouldExposeEmptyDefaultMetadata() {
            var metadata = DefaultConstructorMetadata.EMPTY;

            assertSoftly(softly -> {
                softly.assertThat(metadata.isDefault())
                        .as("empty metadata default constructor flag")
                        .isTrue();
                softly.assertThat(metadata.parameters())
                        .as("empty metadata parameters")
                        .isEmpty();
            });
        }

        @Test
        @DisplayName("Should reject null parameters")
        void shouldRejectNullParameters() {
            assertThatNullPointerException()
                    .as("null constructor parameters")
                    .isThrownBy(() -> DefaultConstructorMetadata.of(true, null))
                    .withMessage("parameters is required");
        }

        @Test
        @DisplayName("Should prevent changes through the parameters view")
        void shouldPreventChangesThroughTheParametersView() {
            var metadata = DefaultConstructorMetadata.of(true, List.of());

            assertThatThrownBy(() -> metadata.parameters().add(mock(ParameterMetaData.class)))
                    .as("immutable constructor parameters")
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
