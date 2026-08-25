/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and the Apache License v2.0 which accompanies this distribution.
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

import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LiteConstructorBuilderTest {

    @Nested
    @DisplayName("When reading constructor parameters")
    class WhenTheParametersAreRead {

        @Test
        @DisplayName("Should return the generated metadata parameters")
        void shouldReturnTheGeneratedMetadataParameters() {
            var metadata = mock(LiteConstructorMetadata.class);
            var parameter = mock(ParameterMetaData.class);
            when(metadata.parameters()).thenReturn(List.of(parameter));
            var builder = new LiteConstructorBuilder(metadata);

            var result = builder.parameters();

            assertThat(result)
                    .as("generated constructor parameters")
                    .containsExactly(parameter);
        }
    }

    @Nested
    @DisplayName("When building an entity")
    class WhenTheEntityIsBuilt {

        @Test
        @DisplayName("Should invoke the generated constructor with collected values")
        void shouldInvokeTheGeneratedConstructorWithCollectedValues() {
            var metadata = mock(LiteConstructorMetadata.class);
            var expected = new Object();
            when(metadata.build(any(Object[].class))).thenReturn(expected);
            var builder = new LiteConstructorBuilder(metadata);
            builder.add("Ada");
            builder.addEmptyParameter();

            var result = builder.build();

            var values = ArgumentCaptor.forClass(Object[].class);
            verify(metadata).build(values.capture());
            assertSoftly(softly -> {
                softly.assertThat(result)
                        .as("constructed entity")
                        .isSameAs(expected);
                softly.assertThat(values.getValue())
                        .as("generated constructor arguments")
                        .containsExactly("Ada", null);
            });
        }
    }
}
