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

import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.data.DeleteProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.Conditions;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DefaultDeleteQuery;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;

import java.util.List;

final class SqlDeleteQueryParser {

    SqlPreparedStatement prepare(String query, SqlTemplate template) {
        Params params = Params.newParams();
        DeleteQuery deleteQuery = getQuery(query, params);
        return SqlPreparedStatement.delete(deleteQuery, params, query, template);
    }

    private DeleteQuery getQuery(String query, Params params) {
        org.eclipse.jnosql.communication.query.DeleteQuery deleteQuery = DeleteProvider.INSTANCE.apply(query);

        return getQuery(params, deleteQuery);
    }

    private DeleteQuery getQuery(Params params, org.eclipse.jnosql.communication.query.DeleteQuery deleteQuery) {
        String columnFamily = CommunicationObserverParser.EMPTY.fireEntity(deleteQuery.entity());
        List<String> columns = deleteQuery.fields().stream()
                .map(f -> CommunicationObserverParser.EMPTY.fireSelectField(columnFamily, f))   .toList();
        CriteriaCondition condition = deleteQuery.where().map(c -> Conditions.getCondition(c, params, CommunicationObserverParser.EMPTY, columnFamily))
                .orElse(null);

        return new DefaultDeleteQuery(columnFamily, condition, columns);
    }

}
