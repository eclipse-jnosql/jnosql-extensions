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

/**
 * Metadata describing an auto-apply {@link AttributeConverter}.
 *
 * <p>This interface is intended for generated implementations that are
 * discovered through the {@link java.util.ServiceLoader} mechanism.
 * Each implementation associates a Java attribute type with a converter
 * instance that should be automatically applied whenever that type is
 * encountered during mapping.</p>
 *
 * <p>Implementations are typically generated at compile time from classes
 * annotated with {@link jakarta.nosql.Converter} where {@code autoApply=true}.</p>
 *
 * <p>For example, given the converter:</p>
 *
 * <pre>{@code
 * @Converter(autoApply = true)
 * public class UUIDConverter
 *         implements AttributeConverter<UUID, String> {
 *     ...
 * }
 * }</pre>
 *
 * <p>A generated implementation may return:</p>
 *
 * <pre>{@code
 * type()      -> UUID.class
 * converter() -> new UUIDConverter()
 * }</pre>
 *
 * <p>The mapping layer uses this metadata to automatically register
 * converters without requiring runtime classpath scanning or generic
 * type inspection.</p>
 *
 */
public interface AutoApplyConverterMetadata {

    /**
     * Returns the Java attribute type that should trigger
     * the auto-application of the converter.
     *
     * @return the attribute type
     */
    Class<?> type();

    /**
     * Returns the converter implementation type.
     *
     * @return the converter class
     */
    Class<? extends AttributeConverter<?, ?>> converterType();

    /**
     * Returns a converter instance.
     *
     * @return the converter instance
     */
    AttributeConverter<?, ?> converter();
}
