/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.mapping.metadata.ProjectionBuilder;
import org.eclipse.jnosql.mapping.metadata.ProjectionBuilderSupplier;
import org.eclipse.jnosql.mapping.metadata.ProjectionConstructorMetadata;

import java.util.Objects;

/**
 * A supplier of constructor avoiding reflection.
 */
public class LiteProjectorConstructorBuilderSupplier implements ProjectionBuilderSupplier {

    @Override
    public ProjectionBuilder apply(ProjectionConstructorMetadata constructorMetadata) {
        Objects.requireNonNull(constructorMetadata, "constructorMetadata is required");
        if(constructorMetadata instanceof LiteProjectorConstructorMetadata) {
            return new LiteProjectorConstructorBuilder((LiteProjectorConstructorMetadata) constructorMetadata);
        } else {
            throw new UnsupportedOperationException("Eclipse JNoSQL Lite does not support reflection, including the use of constructors.");
        }
    }
}
