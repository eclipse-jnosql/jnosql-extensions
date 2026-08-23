/*
 *  Copyright (c) 2026 Otávio Santana and others
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.nosql.AttributeConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AutoApplyConvertersTest {

    @Nested
    @DisplayName("When resolving a converter type")
    class WhenTheConverterTypeIsResolved {

        @Test
        @DisplayName("Should return the explicit converter type")
        void shouldReturnTheExplicitConverterType() {
            Class<? extends AttributeConverter<?, ?>> explicitConverter = ExplicitConverter.class;

            var result = AutoApplyConverters.INSTANCE.typeConverter(explicitConverter, String.class);

            assertThat(result)
                    .as("explicit converter type")
                    .isSameAs(explicitConverter);
        }

        @Test
        @DisplayName("Should return the auto-apply converter type")
        void shouldReturnTheAutoApplyConverterType() {
            var result = AutoApplyConverters.INSTANCE.typeConverter(null, String.class);

            assertThat(result)
                    .as("auto-apply converter type")
                    .isSameAs(TestAutoApplyConverterMetadata.TestAttributeConverter.class);
        }

        @Test
        @DisplayName("Should return null when no converter is registered")
        void shouldReturnNullWhenNoConverterIsRegistered() {
            var result = AutoApplyConverters.INSTANCE.typeConverter(null, Integer.class);

            assertThat(result)
                    .as("converter type for an unregistered attribute")
                    .isNull();
        }
    }

    @Nested
    @DisplayName("When resolving a converter instance")
    class WhenTheConverterInstanceIsResolved {

        @Test
        @DisplayName("Should return the explicit converter instance")
        void shouldReturnTheExplicitConverterInstance() {
            var explicitConverter = new ExplicitConverter();

            var result = AutoApplyConverters.INSTANCE.instanceConverter(explicitConverter, String.class);

            assertThat(result)
                    .as("explicit converter instance")
                    .isSameAs(explicitConverter);
        }

        @Test
        @DisplayName("Should return the auto-apply converter instance")
        void shouldReturnTheAutoApplyConverterInstance() {
            var result = AutoApplyConverters.INSTANCE.instanceConverter(null, String.class);

            assertThat(result)
                    .as("auto-apply converter instance")
                    .isSameAs(TestAutoApplyConverterMetadata.CONVERTER);
        }

        @Test
        @DisplayName("Should return null when no converter is registered")
        void shouldReturnNullWhenNoConverterIsRegistered() {
            var result = AutoApplyConverters.INSTANCE.instanceConverter(null, Integer.class);

            assertThat(result)
                    .as("converter instance for an unregistered attribute")
                    .isNull();
        }
    }

    private static final class ExplicitConverter implements AttributeConverter<String, String> {

        @Override
        public String convertToDatabaseColumn(String attribute) {
            return attribute;
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            return dbData;
        }
    }
}
