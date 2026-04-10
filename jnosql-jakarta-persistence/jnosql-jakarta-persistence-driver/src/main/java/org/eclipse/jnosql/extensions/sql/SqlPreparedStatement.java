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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import org.eclipse.jnosql.mapping.PreparedStatement;

import java.util.Optional;
import java.util.stream.Stream;

public final class SqlPreparedStatement implements PreparedStatement {

    @Override
    public PreparedStatement bind(String name, Object value) {
        return null;
    }

    @Override
    public <T> Stream<T> result() {
        return Stream.empty();
    }

    @Override
    public <T> Optional<T> singleResult() {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean isCount() {
        return false;
    }
}
