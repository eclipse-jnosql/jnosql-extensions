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

/**
 * Represents a reflection-free constructor invoker.
 */
public interface LiteConstructorInvoker {

    /**
     * Builds a new instance using the constructor represented by this metadata.
     * <p>
     * The {@code parameters} array must match the constructor signature in
     * declaration order.
     *
     * @param parameters the constructor arguments
     * @param <T> the resulting instance type
     * @return a new instance
     * @throws IllegalArgumentException if the parameters are invalid
     */
    <T> T build(Object[] parameters);
}