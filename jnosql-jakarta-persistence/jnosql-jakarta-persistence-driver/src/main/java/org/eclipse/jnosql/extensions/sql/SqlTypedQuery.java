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
        return List.of();
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public Optional<T> singleResult() {
        return Optional.empty();
    }

    @Override
    public void executeUpdate() {

    }

    @Override
    public TypedQuery<T> bind(String name, Object value) {
        return null;
    }

    @Override
    public TypedQuery<T> bind(int position, Object value) {
        return null;
    }
}
