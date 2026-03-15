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
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;

import java.util.List;

class UpdateQueryConverter extends QueryConverterSupport {

    UpdateQueryConverter(EntityManager manager) {
        super(manager);
    }

    <T> Query getUpdateQuery(UpdateQuery query) {

        Class<T> type = resolveEntity(query.name());

        CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
        CriteriaUpdate<?> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(type);

        Root<?> root = criteriaUpdate.from(type);

        applySets(query.sets(), root, criteriaUpdate);

        query.where().ifPresent(condition ->
                applyCondition(condition, criteriaBuilder, root, criteriaUpdate)
        );

        return manager.createQuery(criteriaUpdate);
    }

    private void applySets(
            List<Element> sets,
            Root<?> root,
            CriteriaUpdate<?> criteriaUpdate) {

        for (Element element : sets) {

            String property = element.name();
            Object value = element.get();

            Path<?> path = resolvePath(root, property);

            criteriaUpdate.set(path, value);
        }
    }

    protected <T> void applyCondition(
            CriteriaCondition criteriaCondition,
            CriteriaBuilder criteriaBuilder,
            Root<T> root,
            CriteriaUpdate<T> criteriaUpdate) {

        if (criteriaCondition == null) {
            return;
        }

        Predicate predicate = toPredicate(criteriaCondition, criteriaBuilder, root);

        if (predicate != null) {
            criteriaUpdate.where(predicate);
        }
    }
}