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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.nosql.TypedQuery;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class SqlTypedQuery<T> implements TypedQuery<T> {

    private final jakarta.persistence.Query query;

    private final DefaultSqlTemplate template;

     SqlTypedQuery(DefaultSqlTemplate template, jakarta.persistence.TypedQuery<T> query) {
        this.template = template;
        this.query = query;
    }


    @Override
    public List<T> result() {
        return query.getResultList();
    }

    @Override
    public Stream<T> stream() {
        return query.getResultStream();
    }

    @Override
    public Optional<T> singleResult() {

        List<T> results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        }

        if (results.size() > 1) {
            throw new IllegalStateException(
                    "Query returned more than one result: " + results.size());
        }

        return Optional.of(results.get(0));
    }

    @Override
    public void executeUpdate() {
        template.executeInTransaction(query::executeUpdate);
    }

    @Override
    public TypedQuery<T> bind(String name, Object value) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(value, "value is required");

        query.setParameter(name, value);
        return this;
    }

    @Override
    public TypedQuery<T> bind(int position, Object value) {

        if (position <= 0) {
            throw new IllegalArgumentException("position must be greater than 0");
        }

        Objects.requireNonNull(value, "value is required");

        query.setParameter(position, value);
        return this;
    }
}
