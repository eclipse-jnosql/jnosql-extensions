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

import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryMetadataUtils;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredParameterBasedQuery;

import java.util.Collections;
import java.util.function.Function;

@ApplicationScoped
class SqlCursorPaginationOperation implements CursorPaginationOperation {

    private final SqlQueryBuilder sqlQueryBuilder;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var method = context.method();
        var entityMetadata = context.entityMetadata();
        var template = (SemiStructuredTemplate) context.template();
        if (method.query().isPresent()) {
            throw new UnsupportedOperationException("The query annotation is not supported for cursor pagination operations");
        } else if (method.find().isPresent()) {
            return (T) executeFindAnnotation(context, method, entityMetadata, template);
        } else {
            return executeMethodByQuery(context, method, template));
        }
    }


    private CursoredPage<?> executeFindAnnotation(RepositoryInvocationContext context, RepositoryMethod method, EntityMetadata entityMetadata, SemiStructuredTemplate template) {
        var paramValueMap = RepositoryMetadataUtils.INSTANCE.getBy(method, context.parameters());
        var query = SqlParameterBasedQuery.INSTANCE.toQuery(paramValueMap, entityMetadata);
        var updateDynamicQuery = sqlQueryBuilder.updateQuery(context, method, query);
        var special = DynamicReturn.findSpecialParameters(context.parameters(), Function.identity());
        var pageRequest = pageRequest(method, special);
        return template.selectCursor(updateDynamicQuery, pageRequest);
    }

    private static PageRequest pageRequest(RepositoryMethod method, SpecialParameters special) {
        return special.pageRequest()
                .orElseThrow(() -> new IllegalArgumentException("Pageable is required in the method signature" +
                        " as parameter at " + method.name()));
    }
}
