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
package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.mapping.metadata.repository.spi.CountAllOperation;

/**
 * Step builder for creating {@link CountAllOperation} instances outside a CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * CountAllOperation operation = SemistructuredCountAllOperationBuilder.build();
 * }</pre>

 *
 * <p>
 * Each step is immutable. Calling {@code withTemplate()} returns a new
 * record with the updated value, leaving the original step unchanged.

 */
public interface SemistructuredCountAllOperationBuilder {

    static CountAllOperation build() {
        return new SemistructuredCountAllOperation();
    }
}