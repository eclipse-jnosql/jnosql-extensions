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

import java.util.HashMap;
import java.util.Map;

public enum AutoApplyConverters {

    INSTANCE;

    private Map<Class<?>, Class<? extends AttributeConverter<?, ?>>> converters;
    private Map<Class<?>, AttributeConverter<?, ?>> convertersInstance;

     AutoApplyConverters() {
        this.converters = new HashMap<>();
        this.convertersInstance = new HashMap<>();
     }

    Class<? extends AttributeConverter<?, ?>> converter(Class<? extends AttributeConverter<?, ?>> convert, Class<?> type) {
        if (convert != null) {
            return convert;
        }
        return converters.get(type);
    }

    AttributeConverter<?, ?> converter(AttributeConverter<?, ?> convert, Class<?> type) {
        if (convert != null) {
            return convert;
        }
        return convertersInstance.get(type);
    }
}
