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
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;

/**
 *
 * @author Ondro Mihalyi
 */
interface QueryModifier<FROM, RESULT> extends Function<SelectQueryParser.SelectQueryContext<FROM, RESULT>, CriteriaQuery<RESULT>> {

    static <FROM> QueryModifier<FROM, FROM> selectEntity() {
        return ctx -> ctx.query().select((Root<FROM>) ctx.root());
    }

    static <FROM> QueryModifier<FROM, Long> selectCount() {
        return ctx -> ctx.query().select(ctx.builder().count(ctx.root()));
    }

    static <FROM, RESULT> QueryModifier<FROM, RESULT> selectColumns(List<String> columns) {
        if (columns.isEmpty()) {
            return ctx -> ctx.query();
        }
        return ctx -> {
            Path<?> path = ctx.root();
            for (String column : columns) {
                path = path.get(column);
            }
            return ctx.query().select((Path<RESULT>)path);
        };
    }

    static <FROM, RESULT> QueryModifier<FROM, RESULT> where(Optional<CriteriaCondition> maybeCriteria) {
        return ctx -> {
            CriteriaQuery<RESULT> query = ctx.query();
            return maybeCriteria
                    .map(criteria -> query.where(BaseQueryParser.parseCriteria(criteria, ctx.queryContext())))
                    .orElse(query);
        };
    }

    static <FROM, RESULT> QueryModifier<FROM, RESULT> applySorts(List<Sort<?>> sorts) {
        return ctx -> {
            CriteriaQuery<RESULT> query = ctx.query();
            if (sorts != null && !sorts.isEmpty()) {
                List<Order> orders = new ArrayList<>();
                for (Sort sort : sorts) {
                    // Handle nested properties (e.g., "category.name")
                    Path<?> path = ctx.root();
                    String[] fields = sort.property().split("\\.");
                    for (String field : fields) {
                        path = path.get(BaseQueryParser.getFieldName(field));
                    }
                    // Create order based on direction
                    Order order = sort.isAscending() ? ctx.builder().asc(path) : ctx.builder().desc(path);
                    orders.add(order);
                }
                query.orderBy(orders);
            }
            // Apply sorting to query
            return query;
        };
    }

    static <FROM, RESULT> QueryModifier<FROM, RESULT> combine(QueryModifier<FROM, RESULT>... modifiers) {
        return ctx -> {
            CriteriaQuery<RESULT> query = ctx.query();
            for (QueryModifier<FROM, RESULT> modifier : modifiers) {
                query = modifier.apply(new SelectQueryParser.SelectQueryContext<>(query, ctx.queryContext()));
            }
            return query;
        };
    }

}
