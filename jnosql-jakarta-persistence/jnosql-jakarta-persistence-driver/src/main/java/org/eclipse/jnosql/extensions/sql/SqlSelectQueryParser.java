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

import jakarta.data.Direction;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.data.SelectProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.Conditions;
import org.eclipse.jnosql.communication.semistructured.DefaultSelectQuery;
import org.eclipse.jnosql.communication.semistructured.QueryParams;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class SqlSelectQueryParser  {

    SqlPreparedStatement prepare(String query, String entity, SqlTemplate template) {
        Params params = new Params();
        var selectQuery = query(query, entity, params);
        return new SqlPreparedStatement(new QueryParams(selectQuery, params), template, query);
    }



    private org.eclipse.jnosql.communication.semistructured.SelectQuery query(String query, String entity, Params params) {

        var selectQuery = SelectProvider.INSTANCE.apply(query, entity);
        var entityName = CommunicationObserverParser.EMPTY.fireEntity(selectQuery.entity());
        var limit = selectQuery.limit();
        var skip = selectQuery.skip();
        var columns = selectQuery.fields().stream()
                .map(f -> CommunicationObserverParser.EMPTY.fireSelectField(entityName, f)).toList();
        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, entityName))
                .collect(toList());

        var condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, CommunicationObserverParser.EMPTY, entityName)).orElse(null);

        if (params.isNotEmpty()) {
            throw new QueryException("To run a query with a parameter use a PrepareStatement instead.");
        }
        boolean count = selectQuery.isCount();
        return new DefaultSelectQuery(limit, skip, entityName, columns, sorts, condition, count);
    }

    private Sort<?> toSort(Sort<?> sort, String entity) {
        return Sort.of(CommunicationObserverParser.EMPTY.fireSortProperty(entity, sort.property()),
                sort.isAscending()? Direction.ASC: Direction.DESC, false);
    }

}
