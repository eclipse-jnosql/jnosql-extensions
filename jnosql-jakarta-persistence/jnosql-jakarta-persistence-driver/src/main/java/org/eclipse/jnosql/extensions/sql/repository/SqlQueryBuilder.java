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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
class SqlQueryBuilder {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();



    SelectQuery selectQuery(RepositoryInvocationContext context) {
        var method = context.method();
        var entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method.name(), entityMetadata.name());
        var queryParams = SELECT_PARSER.apply(selectQuery, CommunicationObserverParser.EMPTY);
        var query = queryParams.query();
        var params = queryParams.params();
        bind(params, parameters, method.name());
        return query;
    }


    public void bind(Params params, Object[] args, String methodName) {
        Objects.requireNonNull(params, "params is required");
        Objects.requireNonNull(args, "args is required");
        Objects.requireNonNull(methodName, "methodName is required");

        List<String> names = params.getParametersNames();
        if (names.size() > args.length) {
            throw new DynamicQueryException("The number of parameters in a query is bigger than the number of " +
                    "parameters in the method: " + methodName);
        }
        for (int index = 0; index < names.size(); index++) {
            String name = names.get(index);
            params.bind(name, args[index]);
        }
    }

}
