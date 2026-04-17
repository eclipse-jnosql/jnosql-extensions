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
package org.eclipse.jnosql.mapping.core.repository.operations;

import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;

/**
 * Builder for creating {@link UpdateOperation} instances outside a CDI container.
 *
 * <p>
 * This interface provides a static factory method to construct
 * {@code UpdateOperation} implementations that have no external dependencies.
 * </p>
 *
 * @see UpdateOperation
 */
public interface CoreUpdateOperationBuilder {

    /**
     * Creates a new {@link UpdateOperation} instance.
     *
     * @return a new {@link UpdateOperation} instance
     */
    static UpdateOperation build() {
        return new CoreUpdateOperation();
    }
}