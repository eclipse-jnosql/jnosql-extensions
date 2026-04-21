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

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@ApplicationScoped
class SqlReturnType {

    @SuppressWarnings("unchecked")
    protected Object executeFindByQuery(RepositoryInvocationContext context, SelectQuery query) {

        var method = context.method();
        var template = (SemiStructuredTemplate) context.template();
        var entityMetadata = context.entityMetadata();
        var typeClass = entityMetadata.type();
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodName(method.name())
                .returnType(method.returnType().orElseThrow())

                .result(() -> {
                    Stream<Object> select = template.select(query);
                    return select.map(mapper());
                })
                .singleResult(() -> {
                    Optional<Object> object = template.singleResult(query);
                    return object.map(mapper());
                })
                .pagination(DynamicReturn.findPageRequest(context.parameters()))
                .streamPagination(streamPagination(query, template))
                .singleResultPagination(getSingleResult(query, template))
                .page(getPage(query, template))
                .build();
        return dynamicReturn.execute();
    }

    protected <T> Function<PageRequest, Stream<T>> streamPagination(SelectQuery query,
                                                                    SemiStructuredTemplate template) {
        return p -> template.select(query).map(mapper());
    }

    protected <T> Function<PageRequest, Optional<T>> getSingleResult(SelectQuery query,
                                                                     SemiStructuredTemplate template) {
        return p -> template.singleResult(query).map(mapper());
    }

    protected <T>  Function<PageRequest, Page<T>> getPage(SelectQuery query,
                                                          SemiStructuredTemplate template) {
        return p -> {
            Stream<T> entities = template.select(query).map(mapper());
            return NoSQLPage.of(entities.toList(), p);
        };
    }


    @SuppressWarnings("unchecked")
    protected <E> Function<Object, E> mapper() {
        return value -> (E) value;
    }

}
