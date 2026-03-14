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

import jakarta.data.Sort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.util.ArrayList;
import java.util.List;

class SqlSelectQueryConverter {

    private final EntityManager manager;

    SqlSelectQueryConverter(EntityManager manager) {
        this.manager = manager;
    }

    @SuppressWarnings("unchecked")
    <T> T getSelectTypedQuery(SelectQuery query) {
        Class<T> type = resolveEntity(query.name());

        CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);

        Root<T> root = criteriaQuery.from(type);

        applyColumns(query.columns(), root, criteriaQuery);
        applyCondition(query.condition().orElse(null), criteriaBuilder, root, criteriaQuery);
        applySort(query.sorts(), criteriaBuilder, root, criteriaQuery);
        TypedQuery<T> typed = manager.createQuery(criteriaQuery);
        return (T) typed;
    }

    private <T> void applyCondition(CriteriaCondition criteriaCondition, CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<T> criteriaQuery) {
        if (criteriaCondition == null) {
            return;
        }
        Predicate predicate = toPredicate(criteriaCondition, criteriaBuilder, root);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
    }

    private <T> void applySort(List<Sort<?>> sorts, CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<T> criteriaQuery) {
        if(sorts.isEmpty()) {
            return;
        }
        List<Order> orders = new ArrayList<>();
        for (Sort<?> sort : sorts) {
            Path<?> path = resolvePath(root, sort.property());
            if (sort.isAscending()) {
                orders.add(criteriaBuilder.asc(path));
            } else {
                orders.add(criteriaBuilder.desc(path));
            }
        }
        criteriaQuery.orderBy(orders);
    }

    private <T> void applyColumns(List<String> columns, Root<T> root, CriteriaQuery<T> criteriaQuery) {
        if (columns == null || columns.isEmpty()) {
            criteriaQuery.select(root);
            return;
        }

        List<Selection<?>> selections = new ArrayList<>();

        for (String column : columns) {
            selections.add(resolvePath(root, column));
        }

        criteriaQuery.multiselect(selections);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate toPredicate(CriteriaCondition condition,
                                  CriteriaBuilder cb,
                                  Root<?> root) {

        Element element = condition.element();

        String property = element.name();
        Object value = element.get();

        Path<?> path = resolvePath(root, property);

        switch (condition.condition()) {
            case EQUALS:
                return cb.equal(path, value);
            case GREATER_THAN:
                return cb.greaterThan(
                        path.as(Comparable.class),
                        (Comparable) value);

            case GREATER_EQUALS_THAN:
                return cb.greaterThanOrEqualTo(
                        path.as(Comparable.class),
                        (Comparable) value);

            case LESSER_THAN:
                return cb.lessThan(
                        path.as(Comparable.class),
                        (Comparable) value);

            case LESSER_EQUALS_THAN:
                return cb.lessThanOrEqualTo(
                        path.as(Comparable.class),
                        (Comparable) value);

            case LIKE:
                return cb.like(path.as(String.class), value.toString());

            case CONTAINS:
                return cb.like(path.as(String.class), "%" + value + "%");

            case STARTS_WITH:
                return cb.like(path.as(String.class), value + "%");

            case ENDS_WITH:
                return cb.like(path.as(String.class), "%" + value);

            case IN:
                CriteriaBuilder.In<Object> in = cb.in(path);
                ((Iterable<?>) value).forEach(in::value);
                return in;

            case BETWEEN:
                var values = (List<?>) value;
                return cb.between(
                        path.as(Comparable.class),
                        (Comparable) values.get(0),
                        (Comparable) values.get(1));

            case AND:
                List<CriteriaCondition> andConditions =
                        condition.element().value().get(new TypeReference<>() {});

                List<Predicate> andPredicates = andConditions.stream()
                        .map(c -> toPredicate(c, cb, root))
                        .toList();

                return cb.and(andPredicates.toArray(new Predicate[0]));

            case OR:
                List<CriteriaCondition> orConditions =
                        condition.element().value().get(new TypeReference<>() {});

                List<Predicate> orPredicates = orConditions.stream()
                        .map(c -> toPredicate(c, cb, root))
                        .toList();

                return cb.or(orPredicates.toArray(new Predicate[0]));

            case NOT:
                var criteriaCondition = element.get(CriteriaCondition.class);
                return cb.not(
                        toPredicate(criteriaCondition, cb, root)
                );
            case IGNORE_CASE:
                return cb.equal(
                        cb.lower(path.as(String.class)),
                        value.toString().toLowerCase()
                );

            default:
                throw new UnsupportedOperationException(
                        "Unsupported condition: " + condition.condition());
        }
    }

    private Path<?> resolvePath(Path<?> root, String property) {

        if (!property.contains(".")) {
            return root.get(property);
        }

        Path<?> path = root;

        for (String part : property.split("\\.")) {
            path = path.get(part);
        }

        return path;
    }

}
