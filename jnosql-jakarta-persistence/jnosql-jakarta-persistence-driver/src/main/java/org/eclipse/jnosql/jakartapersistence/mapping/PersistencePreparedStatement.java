/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.eclipse.jnosql.mapping.PreparedStatement;

/**
 *
 * @author Ondro Mihalyi
 */
public class PersistencePreparedStatement implements PreparedStatement {

    private final String queryString;
    private final SelectQueryParser selectParser;
    private Map<String, Object> parameters = new HashMap<>();

    public PersistencePreparedStatement(String queryString, final SelectQueryParser selectParser) {
        this.selectParser = selectParser;
        this.queryString = queryString;
    }

    private void applyParameters(Query query) {
        parameters.forEach((name, value) -> {
            if (name.startsWith("?")) {
                var position = Integer.parseInt(name, 1, name.length(), 10);
                query.setParameter(position, value);
            } else {
                query.setParameter(name, value);
            }
        });
    }

    @Override
    public PreparedStatement bind(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    @Override
    public <T> Stream<T> result() {
        Query query = createQuery();
        try {
            return query.getResultStream();
        } catch (IllegalStateException e) {
            return IntStream.rangeClosed(1, query.executeUpdate())
                    .mapToObj(i -> (T)Integer.valueOf(i));
        }
    }

    @Override
    public <T> Optional<T> singleResult() {
        Query query = createQuery();
        return Optional.ofNullable((T) query.getSingleResultOrNull());
    }

    private Query createQuery() {
        Query query = selectParser.buildQuery(queryString);
        applyParameters(query);
        return query;
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCount() {
        return false;
    }

}
