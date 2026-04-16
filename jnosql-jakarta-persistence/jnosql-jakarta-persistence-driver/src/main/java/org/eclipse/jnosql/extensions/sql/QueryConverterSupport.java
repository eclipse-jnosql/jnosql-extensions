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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;

import java.util.List;

abstract class QueryConverterSupport {

    static final List<String> RESERVED_PROPERTIES = List.of("_AND", "_OR", "_NOT");
    private static final PredicateConverter PREDICATE_CONVERTER =  new PredicateConverter(QueryConverterSupport::resolvePath);
    protected final EntityManager manager;

    QueryConverterSupport(EntityManager manager) {
        this.manager = manager;
    }


    protected void applyCondition(CriteriaCondition criteriaCondition, CriteriaBuilder criteriaBuilder, Root<?> root, CriteriaQuery<?> criteriaQuery) {
        PREDICATE_CONVERTER.applyCondition(criteriaCondition, criteriaBuilder, root, criteriaQuery);
    }

    protected Predicate toPredicate(CriteriaCondition condition,
                                 CriteriaBuilder criteriaBuilder,
                                 Root<?> root) {
        return PREDICATE_CONVERTER.toPredicate(condition, criteriaBuilder, root);
    }


    static Path<?> resolvePath(Path<?> root, String property) {

        if(RESERVED_PROPERTIES.contains(property)) {
            return null;
        }
        if (!property.contains(".")) {
            return root.get(property);
        }

        Path<?> path = root;

        for (String part : property.split("\\.")) {
            path = path.get(part);
        }

        return path;
    }

    protected static Object readProperty(Object entity, String property) {
        try {
            var field = entity.getClass().getDeclaredField(property);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot read property '" + property + "' from entity", e);
        }
    }

    protected  <T> Class<T> resolveEntity(String name) {
        return manager.getMetamodel()
                .getEntities()
                .stream()
                .filter(entity ->
                        entity.getName().equals(name) ||
                                entity.getJavaType().getSimpleName().equals(name))
                .map(entity -> (Class<T>) entity.getJavaType())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Entity not found: " + name));
    }
}
