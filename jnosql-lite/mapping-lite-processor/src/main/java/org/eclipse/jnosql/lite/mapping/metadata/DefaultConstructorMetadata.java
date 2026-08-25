/*
 *  Copyright (c) 2023 Otávio Santana and others
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

import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * The lite implementation of {@link ConstructorMetadata}
 */
public class DefaultConstructorMetadata implements ConstructorMetadata {

    /**
     * The empty instance
     */
    public static final ConstructorMetadata EMPTY = new DefaultConstructorMetadata(true, emptyList());

    private final boolean defaultConstructor;
    private final List<ParameterMetaData> parameters;

    private DefaultConstructorMetadata(boolean defaultConstructor, List<ParameterMetaData> parameters) {
        this.defaultConstructor = defaultConstructor;
        this.parameters = parameters;
    }

    @Override
    public List<ParameterMetaData> parameters() {
        return Collections.unmodifiableList(parameters);
    }

    @Override
    public boolean isDefault() {
        return defaultConstructor;
    }

    /**
     * Creates a {@link DefaultConstructorMetadata} instance
     * @param defaultConstructor if the constructor is the default
     * @param parameters the parameters
     * @return a {@link DefaultConstructorMetadata} instance
     * @throws NullPointerException when there is null parameter
     */
    public static DefaultConstructorMetadata of(boolean defaultConstructor, List<ParameterMetaData> parameters) {
        Objects.requireNonNull(parameters, "parameters is required");
        return new DefaultConstructorMetadata(defaultConstructor, parameters);
    }
}
