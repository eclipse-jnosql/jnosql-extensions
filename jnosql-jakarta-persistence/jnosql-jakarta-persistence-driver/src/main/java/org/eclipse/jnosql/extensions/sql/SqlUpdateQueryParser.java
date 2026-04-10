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
import org.eclipse.jnosql.communication.query.UpdateItem;
import org.eclipse.jnosql.communication.query.data.UpdateProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.Conditions;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DefaultUpdateQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.communication.semistructured.Values;

import java.util.ArrayList;
import java.util.List;

final class SqlUpdateQueryParser {


    SqlPreparedStatement prepare(String query, SqlTemplate template) {

        Params params = Params.newParams();
        var updateQuery = getQuery(query, params);
        return SqlPreparedStatement.update(updateQuery, params, query, template);
    }

    private UpdateQuery getQuery(String query, Params params) {
        var updateQuery = UpdateProvider.INSTANCE.apply(query);
        return getQuery(params, updateQuery);
    }

    private UpdateQuery getQuery(Params params, org.eclipse.jnosql.communication.query.UpdateQuery updateQuery) {
        var entity = CommunicationObserverParser.EMPTY.fireEntity(updateQuery.entity());

        List<Element> set = new ArrayList<>();
        for (UpdateItem updateItem : updateQuery.set()) {
            var field = CommunicationObserverParser.EMPTY.fireSelectField(entity, updateItem.name());
            var value = Values.get(updateItem.value(), params);
            set.add(Element.of(field, value));
        }
        CriteriaCondition condition = updateQuery.where().map(c -> Conditions.getCondition(c, params, CommunicationObserverParser.EMPTY, entity))
                .orElse(null);

        return new DefaultUpdateQuery(entity, set, condition);
    }

}
