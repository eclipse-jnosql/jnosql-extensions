/*
 * Copyright (c) 2024,2026 Contributors to the Eclipse Foundation
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
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;

import java.util.List;
import java.util.function.Function;

class UpdateQueryParser extends BaseUpdateQueryParser {

    public UpdateQueryParser(PersistenceDatabaseManager manager) {
        super(manager);
    }

    long update(UpdateQuery updateQuery) {
        final Class<?> type = entityClassFromEntityName(updateQuery.name());
        if (updateQuery.where().isEmpty()) {
            return updateAll(type, updateQuery.sets());
        } else {
            final CriteriaCondition criteria = updateQuery.where().get();
            return updateWithCriteria(type, updateQuery, criteria);
        }
    }

    private <T> long updateWithCriteria(Class<T> type, UpdateQuery updateQuery, CriteriaCondition criteria) {
        Query query = buildUpdateQuery(type, ctx -> {
            var criteriaUpdate = ctx.query().where(parseCriteria(criteria, ctx.queryContext()));
            criteriaUpdate = applySetsToUpdateCriteria(updateQuery.sets(), criteriaUpdate);
            return criteriaUpdate;
        });
        return query.executeUpdate();
    }

    private <T> CriteriaUpdate<T> applySetsToUpdateCriteria(List<Element> sets, CriteriaUpdate<T> criteriaUpdate) {
        for (Element setElement : sets) {
            criteriaUpdate = criteriaUpdate.set(setElement.name(), setElement.get());
        }
        return criteriaUpdate;
    }

    private  <T> long updateAll(Class<T> type, List<Element> sets) {
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        CriteriaUpdate<T> updateCriteria = criteriaBuilder.createCriteriaUpdate(type);
        updateCriteria = applySetsToUpdateCriteria(sets, updateCriteria);
        long entries = entityManager().createQuery(updateCriteria).executeUpdate();
        return entries;
    }

    private <FROM> Query buildUpdateQuery(Class<FROM> fromType,
            Function<UpdateQueryContext<FROM>, CriteriaUpdate<FROM>> queryModifier) {
        EntityManager em = entityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        // TODO - cache using PersistenceUnitCache as in SelectQueryParser
        CriteriaUpdate<FROM> criteriaQuery = criteriaBuilder.createCriteriaUpdate(fromType);
        Root<FROM> from = criteriaQuery.from(fromType);
        criteriaQuery = queryModifier.apply(
                new UpdateQueryContext(criteriaQuery,
                        new QueryContext(from, criteriaBuilder)));
        return em.createQuery(criteriaQuery);
    }

    record UpdateQueryContext<FROM>(CriteriaUpdate<FROM> query, QueryContext<FROM> queryContext) {

        public Root<FROM> root() {
            return queryContext.root();
        }

        public CriteriaBuilder builder() {
            return queryContext.builder();
        }

    }

}

