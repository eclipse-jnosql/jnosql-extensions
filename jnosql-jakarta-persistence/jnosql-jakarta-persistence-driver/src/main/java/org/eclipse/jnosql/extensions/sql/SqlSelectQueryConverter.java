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
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

class SqlSelectQueryConverter {

    private final EntityManager manager;

    SqlSelectQueryConverter(EntityManager manager) {
        this.manager = manager;
    }

    <T> T getSelectTypedQuery(SelectQuery query) {
        Class<T> type = resolveEntity(query.name());

        CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);

        Root<T> root = criteriaQuery.from(type);

        applyColumns(query, root, criteriaQuery);
        applyCondition(query, criteriaBuilder, root, criteriaQuery);
        applySort(query, criteriaBuilder, root, criteriaQuery);
        TypedQuery<T> typed = manager.createQuery(criteriaQuery);
        applyPagination(query, typed);
        return typed;
    }

    private <T> void applyColumns(SelectQuery query, Root<T> root, CriteriaQuery<T> criteriaQuery) {

    }

    @SuppressWarnings("unchecked")
    private <FROM> Class<FROM> resolveEntity(String name) {
        return manager.getMetamodel()
                .getEntities()
                .stream()
                .filter(entity ->
                        entity.getName().equals(name) ||
                                entity.getJavaType().getSimpleName().equals(name))
                .map(entity -> (Class<FROM>) entity.getJavaType())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Entity not found: " + name));
    }
}
