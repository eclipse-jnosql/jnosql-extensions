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

import jakarta.data.Sort;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;


class BaseUpdateQueryParser extends BaseQueryParser {


    public BaseUpdateQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    @Override
    protected <T> Stream<T> query(String queryString, String entity, Collection<Sort<?>> sorts, Consumer<Query> queryModifier) {
        final Query query = buildQuery(queryString);
        if (queryModifier != null) {
            queryModifier.accept(query);
        }
        final int updated = query.executeUpdate();
        return IntStream.range(0, updated)
                .mapToObj(i -> null);
    }

}

