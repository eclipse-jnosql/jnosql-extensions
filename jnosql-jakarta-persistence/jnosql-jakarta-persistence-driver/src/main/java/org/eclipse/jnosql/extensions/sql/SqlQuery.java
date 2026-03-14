/*
 *   Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
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
 *   Ondro Mihalyi
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.nosql.Query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class SqlQuery implements Query {

    private final jakarta.persistence.Query query;

    private final DefaultSqlTemplate template;

     SqlQuery(DefaultSqlTemplate template, jakarta.persistence.Query query) {
        this.template = template;
        this.query = query;
    }

    @Override
    public void executeUpdate() {

    }

    @Override
    public <T> List<T> result() {
        return List.of();
    }

    @Override
    public <T> Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public <T> Optional<T> singleResult() {
        return Optional.empty();
    }

    @Override
    public Query bind(String name, Object value) {
        return null;
    }

    @Override
    public Query bind(int position, Object value) {
        return null;
    }
}
