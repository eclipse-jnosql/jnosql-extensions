/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and the Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 which accompanies this distribution.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.ProjectionParameterMetadata;
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

class LiteProjectorConstructorBuilderTest {

    @Nested
    @DisplayName("When reading projection parameters")
    class WhenTheParametersAreRead {

        @Test
        @DisplayName("Should return the generated metadata parameters")
        void shouldReturnTheGeneratedMetadataParameters() {
            var metadata = mock(LiteProjectorConstructorMetadata.class);
            var parameter = mock(ProjectionParameterMetadata.class);
            when(metadata.parameters()).thenReturn(List.of(parameter));
            var builder = new LiteProjectorConstructorBuilder(metadata);

            var result = builder.parameters();

            assertThat(result)
                    .as("generated projection parameters")
                    .containsExactly(parameter);
        }
    }

    @Nested
    @DisplayName("When building a projection")
    class WhenTheProjectionIsBuilt {

        @Test
        @DisplayName("Should invoke the generated constructor with collected values")
        void shouldInvokeTheGeneratedConstructorWithCollectedValues() {
            var metadata = mock(LiteProjectorConstructorMetadata.class);
            var expected = new Object();
            when(metadata.build(any(Object[].class))).thenReturn(expected);
            var builder = new LiteProjectorConstructorBuilder(metadata);
            builder.add("Ada");
            builder.addEmptyParameter();

            var result = builder.build();

            var values = ArgumentCaptor.forClass(Object[].class);
            verify(metadata).build(values.capture());
            assertSoftly(softly -> {
                softly.assertThat(result)
                        .as("constructed projection")
                        .isSameAs(expected);
                softly.assertThat(values.getValue())
                        .as("generated projection constructor arguments")
                        .containsExactly("Ada", null);
            });
        }
    }
}
