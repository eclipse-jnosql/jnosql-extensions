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
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;

import java.util.List;

abstract class QueryConverterSupport {

    static final List<String> RESERVED_PROPERTIES = List.of("_AND", "_OR", "_NOT");

    protected final EntityManager manager;

    QueryConverterSupport(EntityManager manager) {
        this.manager = manager;
    }


    protected<T> void applyCondition(CriteriaCondition criteriaCondition, CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<T> criteriaQuery) {
        if (criteriaCondition == null) {
            return;
        }
        Predicate predicate = toPredicate(criteriaCondition, criteriaBuilder, root);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Predicate toPredicate(CriteriaCondition condition,
                                 CriteriaBuilder criteriaBuilder,
                                 Root<?> root) {

        Element element = condition.element();

        String property = element.name();
        Object value = element.get();

        Path<?> path = resolvePath(root, property);

        return switch (condition.condition()) {

            case EQUALS ->
                    criteriaBuilder.equal(path, value);

            case GREATER_THAN ->
                    criteriaBuilder.greaterThan(
                            path.as(Comparable.class),
                            (Comparable) value);

            case GREATER_EQUALS_THAN ->
                    criteriaBuilder.greaterThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) value);

            case LESSER_THAN ->
                    criteriaBuilder.lessThan(
                            path.as(Comparable.class),
                            (Comparable) value);

            case LESSER_EQUALS_THAN ->
                    criteriaBuilder.lessThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) value);

            case LIKE ->
                    criteriaBuilder.like(path.as(String.class), value.toString());

            case CONTAINS ->
                    criteriaBuilder.like(path.as(String.class), "%" + value + "%");

            case STARTS_WITH ->
                    criteriaBuilder.like(path.as(String.class), value + "%");

            case ENDS_WITH ->
                    criteriaBuilder.like(path.as(String.class), "%" + value);

            case IN -> {
                CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
                ((Iterable<?>) value).forEach(in::value);
                yield in;
            }

            case BETWEEN -> {
                var values = (List<?>) value;
                yield criteriaBuilder.between(
                        path.as(Comparable.class),
                        (Comparable) values.get(0),
                        (Comparable) values.get(1));
            }

            case AND -> {
                List<CriteriaCondition> conditions =
                        condition.element().value().get(new TypeReference<>() {});

                List<Predicate> andPredicates = conditions.stream()
                        .map(c -> toPredicate(c, criteriaBuilder, root))
                        .toList();

                yield criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
            }

            case OR -> {
                List<CriteriaCondition> conditions =
                        condition.element().value().get(new TypeReference<>() {});

                List<Predicate> orPredicates = conditions.stream()
                        .map(c -> toPredicate(c, criteriaBuilder, root))
                        .toList();

                yield criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));
            }

            case NOT -> {
                var inner = element.get(CriteriaCondition.class);
                yield criteriaBuilder.not(
                        toPredicate(inner, criteriaBuilder, root)
                );
            }

            case IGNORE_CASE ->
                    criteriaBuilder.equal(
                            criteriaBuilder.lower(path.as(String.class)),
                            value.toString().toLowerCase()
                    );
        };
    }

    protected Path<?> resolvePath(Path<?> root, String property) {

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
}
