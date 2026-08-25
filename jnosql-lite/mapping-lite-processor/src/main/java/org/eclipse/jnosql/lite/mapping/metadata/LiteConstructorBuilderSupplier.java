/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilderSupplier;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;

import java.util.Objects;

/**
 * A supplier of constructor avoiding reflection.
 */
public class LiteConstructorBuilderSupplier implements ConstructorBuilderSupplier {

    @Override
    public ConstructorBuilder apply(ConstructorMetadata constructorMetadata) {
        Objects.requireNonNull(constructorMetadata, "constructorMetadata is required");
        if(constructorMetadata instanceof LiteConstructorMetadata) {
            return new LiteConstructorBuilder((LiteConstructorMetadata) constructorMetadata);
        } else {
            throw new UnsupportedOperationException("Eclipse JNoSQL Lite does not support reflection, including the use of constructors.");
        }
    }
}
