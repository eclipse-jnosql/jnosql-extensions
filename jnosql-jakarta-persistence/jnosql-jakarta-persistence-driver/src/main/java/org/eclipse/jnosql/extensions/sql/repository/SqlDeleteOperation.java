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

import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingDeleteQuery;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.RestrictionConverter;

import java.util.Optional;

@ApplicationScoped
@Typed(SqlDeleteOperation.class)
class SqlDeleteOperation extends CoreDeleteOperation implements DeleteOperation {


    @Override
    protected void deleteByRestriction(RepositoryInvocationContext context, Restriction<?> restriction) {

        var template = (SemiStructuredTemplate) context.template();
        EntityMetadata entityMetadata = context.entityMetadata();
        Optional<CriteriaCondition> condition = SqlRestrictionConverter.INSTANCE.parser(restriction, entityMetadata);
        var deleteQuery = new MappingDeleteQuery(entityMetadata.name(), condition.orElse(null));
        template.delete(deleteQuery);
    }
}
