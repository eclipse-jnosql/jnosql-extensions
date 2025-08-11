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

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.function.Function;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;


class DeleteQueryParser extends BaseQueryParser {


    public DeleteQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    public <T, K> void delete(Class<T> type, K key) {
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        CriteriaDelete<T> deleteCriteria = criteriaBuilder.createCriteriaDelete(type);
        Root<?> root = deleteCriteria.from(type);
        String entityIdName = getEntityIdName(type);
        deleteCriteria.where(criteriaBuilder.equal(root.get(entityIdName), key));
        entityManager().createQuery(deleteCriteria).executeUpdate();
    }

    private <T> String getEntityIdName(Class<T> type) {
        EntityType<T> entityType = entityManager().getMetamodel().entity(type);
        SingularAttribute<?, ?> idAttribute = entityType.getId(entityType.getIdType().getJavaType());
        String entityIdName = idAttribute.getName();
        return entityIdName;
    }

    <T> void deleteAll(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    void delete(DeleteQuery deleteQuery) {
                final String entityName = deleteQuery.name();
        final EntityType<?> entityType = findEntityType(entityName);
        if (deleteQuery.condition().isEmpty()) {
            deleteAll(entityType.getJavaType());
        } else {
            final CriteriaCondition criteria = deleteQuery.condition().get();
            Query query = buildQuery(entityType.getJavaType(), ctx -> {
                return ctx.query().where(parseCriteria(criteria, ctx.queryContext()));
            });
            query.executeUpdate();
        }
    }

    private <FROM> Query buildQuery(Class<FROM> fromType,
            Function<DeleteQueryContext<FROM>, CriteriaDelete<FROM>> queryModifier) {
        EntityManager em = entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaDelete<FROM> criteriaQuery = criteriaBuilder.createCriteriaDelete(fromType);
        Root<FROM> from = criteriaQuery.from(fromType);
        criteriaQuery = queryModifier.apply(
                new DeleteQueryContext(criteriaQuery,
                        new QueryContext(from, criteriaBuilder)));
        return em.createQuery(criteriaQuery);
    }

    record DeleteQueryContext<FROM>(CriteriaDelete<FROM> query, QueryContext<FROM> queryContext) {

        public Root<FROM> root() {
            return queryContext.root();
        }

        public CriteriaBuilder builder() {
            return queryContext.builder();
        }

    }

}

