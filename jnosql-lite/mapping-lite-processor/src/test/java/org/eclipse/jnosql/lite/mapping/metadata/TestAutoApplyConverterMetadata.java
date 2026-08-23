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

public final class TestAutoApplyConverterMetadata implements AutoApplyConverterMetadata {

    static final TestAttributeConverter CONVERTER = new TestAttributeConverter();

    @Override
    public Class<?> type() {
        return String.class;
    }

    @Override
    public Class<? extends AttributeConverter<?, ?>> converterType() {
        return TestAttributeConverter.class;
    }

    @Override
    public AttributeConverter<?, ?> converter() {
        return CONVERTER;
    }

    public static final class TestAttributeConverter implements AttributeConverter<String, String> {

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
