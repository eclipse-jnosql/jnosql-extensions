/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;


final class DeleteQueryConverter extends QueryConverterSupport {

    DeleteQueryConverter(EntityManager manager) {
        super(manager);
    }

    <T> Query convert(DeleteQuery query) {

        Class<T> type = resolveEntity(query.name());

        CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
        CriteriaDelete<T> criteriaDelete = criteriaBuilder.createCriteriaDelete(type);

        Root<T> root = criteriaDelete.from(type);

        applyCondition(query.condition().orElse(null), criteriaBuilder, root, criteriaDelete);

        return manager.createQuery(criteriaDelete);
    }

    private  <T> void applyCondition(
            CriteriaCondition criteriaCondition,
            CriteriaBuilder criteriaBuilder,
            Root<T> root,
            CriteriaDelete<T> criteriaDelete) {

        if (criteriaCondition == null) {
            return;
        }

        var predicate = PREDICATE_CONVERTER.toPredicate(criteriaCondition, criteriaBuilder, root);
        if (predicate != null) {
            criteriaDelete.where(predicate);
        }
    }
}