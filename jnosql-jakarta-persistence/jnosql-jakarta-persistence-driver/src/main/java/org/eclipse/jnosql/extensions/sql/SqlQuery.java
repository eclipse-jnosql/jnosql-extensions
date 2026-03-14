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
import java.util.Objects;
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
        template.executeInTransaction(query::executeUpdate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> result() {
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Stream<T> stream() {
        return (Stream<T>) query.getResultStream();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> singleResult() {

        var results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        if (results.size() > 1) {
            throw new IllegalStateException(
                    "Query returned more than one result: " + results.size());
        }

        return Optional.of((T) results.get(0));
    }

    @Override
    public Query bind(String name, Object value) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(value, "value is required");

        query.setParameter(name, value);
        return this;
    }

    @Override
    public Query bind(int position, Object value) {

        if (position <= 0) {
            throw new IllegalArgumentException("position must be greater than 0");
        }

        Objects.requireNonNull(value, "value is required");

        query.setParameter(position, value);
        return this;
    }
}
