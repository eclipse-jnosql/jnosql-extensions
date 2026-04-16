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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Predicate toPredicate(CriteriaCondition condition,
                                 CriteriaBuilder criteriaBuilder,
                                 Root<?> root) {

        Element element = condition.element();

        String property = element.name();
        Object rawValue = element.get();
        Path<?> path = path(root, property, rawValue);
        return switch (condition.condition()) {

            case EQUALS ->
                    criteriaBuilder.equal(path, rawValue);

            case GREATER_THAN ->
                    criteriaBuilder.greaterThan(
                            path.as(Comparable.class),
                            (Comparable) rawValue);

            case GREATER_EQUALS_THAN ->
                    criteriaBuilder.greaterThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) rawValue);

            case LESSER_THAN ->
                    criteriaBuilder.lessThan(
                            path.as(Comparable.class),
                            (Comparable) rawValue);

            case LESSER_EQUALS_THAN ->
                    criteriaBuilder.lessThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) rawValue);

            case LIKE ->
                    criteriaBuilder.like(path.as(String.class), rawValue.toString());

            case CONTAINS ->
                    criteriaBuilder.like(path.as(String.class), "%" + rawValue + "%");

            case STARTS_WITH ->
                    criteriaBuilder.like(path.as(String.class), rawValue + "%");

            case ENDS_WITH ->
                    criteriaBuilder.like(path.as(String.class), "%" + rawValue);

            case IN -> {
                CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
                ((Iterable<?>) rawValue).forEach(v -> in.value(value(v)));
                yield in;
            }

            case BETWEEN -> {
                var values = (List<?>) rawValue;
                yield criteriaBuilder.between(
                        path.as(Comparable.class),
                        (Comparable) value(values.get(0)),
                        (Comparable) value(values.get(1)));
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

            case IGNORE_CASE -> {
                var inner = element.get(CriteriaCondition.class);
                var innerElement = inner.element();
                var innerProperty = innerElement.name();
                Object innerValue = innerElement.get();
                Path<?> innerPath = path(root, innerProperty, innerValue);
                yield criteriaBuilder.equal(
                        criteriaBuilder.lower(innerPath.as(String.class)),
                        innerValue.toString().toLowerCase()
                );
            }
        };
    }

    private static Object value(Object value) {
        if(value instanceof Value paramValue) {
            return paramValue.get();
        }
        return value;
    }

    private Path<?> path(Root<?> root, String property, Object value) {
        if(value instanceof CriteriaCondition) {
            return null;
        }
        return resolvePath(root, property);
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

    private boolean isStringPath(Path<?> path) {
        return path != null && String.class.isAssignableFrom(path.getJavaType());
    }
}
