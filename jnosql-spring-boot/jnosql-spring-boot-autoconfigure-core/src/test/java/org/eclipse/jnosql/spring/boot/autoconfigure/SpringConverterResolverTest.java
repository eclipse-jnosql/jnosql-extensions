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
package org.eclipse.jnosql.spring.boot.autoconfigure;

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.metadata.FieldParameterMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SpringConverterResolver}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>When a Spring-managed converter bean exists it is returned.</li>
 *   <li>When no Spring bean exists, the resolver falls back to reflective instantiation.</li>
 *   <li>When there is no converter on the metadata, {@link Optional#empty()} is returned.</li>
 *   <li>A {@code null} metadata argument throws {@link NullPointerException}.</li>
 * </ul>
 */
class SpringConverterResolverTest {

    private ApplicationContext applicationContext;
    private SpringConverterResolver resolver;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        resolver = new SpringConverterResolver(applicationContext);
    }

    // ------------------------------------------------------------------
    // Null / empty-converter cases
    // ------------------------------------------------------------------

    @Test
    void shouldThrowNullPointerExceptionWhenMetadataIsNull() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnEmptyWhenMetadataHasNoConverter() {
        FieldParameterMetadata metadata = mock(FieldParameterMetadata.class);
        when(metadata.converter()).thenReturn(Optional.empty());

        Optional<AttributeConverter<?, ?>> result = resolver.resolve(metadata);

        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------------
    // Spring-managed converter
    // ------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldReturnSpringManagedConverterWhenBeanExists() {
        FieldParameterMetadata metadata = mock(FieldParameterMetadata.class);
        SampleConverter expectedConverter = new SampleConverter();

        // Explicit raw cast needed to match the generic wildcard return type of converter()
        when(metadata.converter()).thenReturn((Optional) Optional.of(SampleConverter.class));
        when(applicationContext.getBean(SampleConverter.class)).thenReturn(expectedConverter);

        Optional<AttributeConverter<?, ?>> result = resolver.resolve(metadata);

        assertThat(result).isPresent().contains(expectedConverter);
    }

    // ------------------------------------------------------------------
    // Reflective fallback
    // ------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldFallBackToReflectiveInstantiationWhenNoBeanExists() {
        FieldParameterMetadata metadata = mock(FieldParameterMetadata.class);

        when(metadata.converter()).thenReturn((Optional) Optional.of(SampleConverter.class));
        when(applicationContext.getBean(SampleConverter.class))
                .thenThrow(new NoSuchBeanDefinitionException(SampleConverter.class));

        Optional<AttributeConverter<?, ?>> result = resolver.resolve(metadata);

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(SampleConverter.class);
    }

    // ------------------------------------------------------------------
    // Test fixture
    // ------------------------------------------------------------------

    /** A simple no-arg converter used as a test fixture. */
    public static class SampleConverter implements AttributeConverter<String, String> {
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
