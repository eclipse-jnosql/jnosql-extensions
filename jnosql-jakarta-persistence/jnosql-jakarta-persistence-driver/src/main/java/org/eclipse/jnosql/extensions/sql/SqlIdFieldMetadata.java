/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.lang.annotation.Annotation;
import java.util.Optional;

final class SqlIdFieldMetadata implements FieldMetadata {

    private final String name;

    SqlIdFieldMetadata(String name) {
        this.name = name;
    }

    @Override
    public Object read(Object bean) {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public void write(Object bean, Object value) {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public String fieldName() {
        return "";
    }

    @Override
    public Object value(Value value) {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public MappingType mappingType() {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public String name() {
       return name;
    }

    @Override
    public Class<?> type() {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public boolean isId() {
        return true;
    }

    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<Class<T>> converter() {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public <X, Y, T extends AttributeConverter<X, Y>> Optional<T> newConverter() {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public Optional<String> udt() {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }

    @Override
    public <T extends Annotation> Optional<String> value(Class<T> type) {
        throw new UnsupportedOperationException("SQL entities do not support field metadata");
    }
}
