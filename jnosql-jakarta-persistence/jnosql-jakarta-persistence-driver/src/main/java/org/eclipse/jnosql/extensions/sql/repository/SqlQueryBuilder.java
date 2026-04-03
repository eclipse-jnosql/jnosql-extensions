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

import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.method.DeleteMethodProvider;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.DeleteQueryParser;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.DynamicQueryException;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@ApplicationScoped
class SqlQueryBuilder {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();
    private static final DeleteQueryParser DELETE_PARSER = new DeleteQueryParser();

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
        return updateQuery(context, method, query);
    }

    static SelectQuery updateQuery(RepositoryInvocationContext context, RepositoryMethod method, SelectQuery query) {
        List<Sort<?>> sorts = new ArrayList<>(method.sorts());
        var specialParameters = SpecialParameters.of(context.parameters(), Function.identity());
        sorts.addAll(specialParameters.sorts());
        List<String> attributes = new ArrayList<>(query.columns());
        attributes.addAll(method.select());

        var skip = specialParameters.limit().map(Limit::startAt).orElse(query.skip());
        var limit = specialParameters.limit().map(Limit::maxResults).orElse((int) query.limit());
        return new MappingQuery(sorts, limit, skip, query.condition().orElse(null),
                query.name()
                , attributes);
    }

    DeleteQuery deleteQuery(RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        var provider = DeleteMethodProvider.INSTANCE;
        var method = context.method();
        var deleteQuery = provider.apply(method.name(), entityMetadata.name());
        var queryParams = DELETE_PARSER.apply(deleteQuery, CommunicationObserverParser.EMPTY);
        var params = queryParams.params();
        var parameters = context.parameters();
        var query = queryParams.query();
        bind(params, parameters, method.name());
        return query;
    }


    private void bind(Params params, Object[] args, String methodName) {
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
