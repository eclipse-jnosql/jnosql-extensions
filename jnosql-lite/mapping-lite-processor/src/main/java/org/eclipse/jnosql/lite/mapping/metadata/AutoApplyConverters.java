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

import jakarta.nosql.AttributeConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public enum AutoApplyConverters {

    INSTANCE;

    private final Map<Class<?>, Class<? extends AttributeConverter<?, ?>>> converters;
    private final Map<Class<?>, AttributeConverter<?, ?>> convertersInstance;

    AutoApplyConverters() {
        this.converters = new HashMap<>();
        this.convertersInstance = new HashMap<>();
        loadConverters();
    }


    public Class<? extends AttributeConverter<?, ?>> typeConverter(Class<? extends AttributeConverter<?, ?>> convert,
                                                                   Class<?> type) {
        if (convert != null) {
            return convert;
        }
        return converters.get(type);
    }

    public AttributeConverter<?, ?> instanceConverter(AttributeConverter<?, ?> convert, Class<?> type) {
        if (convert != null) {
            return convert;
        }
        return convertersInstance.get(type);
    }

    private void loadConverters() {
        ServiceLoader.load(AutoApplyConverterMetadata.class)
                .forEach(metadata -> {
                    converters.put(
                            metadata.type(),
                            metadata.converterType()
                    );

                    convertersInstance.put(
                            metadata.type(),
                            metadata.converter()
                    );
                });
    }

}
