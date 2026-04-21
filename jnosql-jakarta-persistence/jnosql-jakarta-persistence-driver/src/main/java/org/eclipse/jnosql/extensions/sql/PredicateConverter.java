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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;

import java.util.List;

@SuppressWarnings("unchecked")
final class PredicateConverter {

    private final PathResolver pathResolver;

    PredicateConverter(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    void applyCondition(CriteriaCondition criteriaCondition,
                        CriteriaBuilder criteriaBuilder,
                        Root<?> root,
                        CriteriaQuery<?> criteriaQuery) {
        if (criteriaCondition == null) {
            return;
        }
        Predicate predicate = toPredicate(criteriaCondition, criteriaBuilder, root);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
    }

    Predicate toPredicate(CriteriaCondition condition,
                          CriteriaBuilder criteriaBuilder,
                          Root<?> root) {
        return toPredicate(condition, criteriaBuilder, root, false);
    }

    private Predicate toPredicate(CriteriaCondition condition,
                                  CriteriaBuilder criteriaBuilder,
                                  Root<?> root,
                                  boolean ignoreCase) {

        Element element = condition.element();
        String property = element.name();
        Object rawValue = element.get();

        return switch (condition.condition()) {

            case EQUALS -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield equalsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case LIKE -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield likePredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case CONTAINS -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield containsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case STARTS_WITH -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield startsWithPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case ENDS_WITH -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield endsWithPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case GREATER_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield greaterThanPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case GREATER_EQUALS_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield greaterEqualsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case LESSER_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield lessThanPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case LESSER_EQUALS_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield lessEqualsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case IN -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield inPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case BETWEEN -> {
                Path<?> path = pathResolver.resolvePath(root, property);
                yield betweenPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case AND -> andPredicate(criteriaBuilder, root, element, ignoreCase);

            case OR -> orPredicate(criteriaBuilder, root, element, ignoreCase);

            case NOT -> notPredicate(criteriaBuilder, root, element, ignoreCase);

            case IGNORE_CASE -> {
                CriteriaCondition inner = element.get(CriteriaCondition.class);
                yield toPredicate(inner, criteriaBuilder, root, true);
            }
        };
    }

    private Predicate equalsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.equal(field, cb.upper((Expression<String>) expression));
            }

            return cb.equal(field, resolvedValue.toString().toUpperCase());
        }

        return cb.equal(path, resolvedValue);
    }

    private Predicate likePredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.like(field, cb.upper((Expression<String>) expression));
            }

            return cb.like(field, resolvedValue.toString().toUpperCase());
        }

        return cb.like(path.as(String.class), resolvedValue.toString());
    }

    private Predicate containsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.like(field, cb.upper((Expression<String>) expression));
            }

            return cb.like(field, "%" + resolvedValue.toString().toUpperCase() + "%");
        }

        return cb.like(path.as(String.class), "%" + resolvedValue + "%");
    }

    private Predicate startsWithPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.like(field, cb.upper((Expression<String>) expression));
            }

            return cb.like(field, resolvedValue.toString().toUpperCase() + "%");
        }

        return cb.like(path.as(String.class), resolvedValue + "%");
    }

    private Predicate endsWithPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.like(field, cb.upper((Expression<String>) expression));
            }

            return cb.like(field, "%" + resolvedValue.toString().toUpperCase());
        }

        return cb.like(path.as(String.class), "%" + resolvedValue);
    }

    private Predicate greaterThanPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.greaterThan(field, cb.upper((Expression<String>) expression));
            }

            return cb.greaterThan(field, resolvedValue.toString().toUpperCase());
        }

        return cb.greaterThan(path.as(Comparable.class), (Comparable) resolvedValue);
    }

    private Predicate greaterEqualsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.greaterThanOrEqualTo(field, cb.upper((Expression<String>) expression));
            }

            return cb.greaterThanOrEqualTo(field, resolvedValue.toString().toUpperCase());
        }

        return cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) resolvedValue);
    }

    private Predicate lessThanPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.lessThan(field, cb.upper((Expression<String>) expression));
            }

            return cb.lessThan(field, resolvedValue.toString().toUpperCase());
        }

        return cb.lessThan(path.as(Comparable.class), (Comparable) resolvedValue);
    }

    private Predicate lessEqualsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object resolvedValue = value(rawValue);

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            if (resolvedValue instanceof Expression<?> expression) {
                return cb.lessThanOrEqualTo(field, cb.upper((Expression<String>) expression));
            }

            return cb.lessThanOrEqualTo(field, resolvedValue.toString().toUpperCase());
        }

        return cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) resolvedValue);
    }

    private Predicate inPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        CriteriaBuilder.In<Object> in;

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));
            in = cb.in(field);

            ((Iterable<?>) rawValue).forEach(item -> {
                Object resolvedValue = value(item);

                if (resolvedValue instanceof Expression<?> expression) {
                    in.value(cb.upper((Expression<String>) expression));
                } else {
                    in.value(resolvedValue.toString().toUpperCase());
                }
            });

            return in;
        }

        in = cb.in(path);

        ((Iterable<?>) rawValue).forEach(item -> {
                    var rawItemValue = value(item);
                    if (rawItemValue instanceof Iterable<?> iterable) {
                        iterable.forEach(inner ->in.value(value(inner)));
                    } else {
                        in.value(value(item));
                    }
                }
        );

        return in;
    }

    private Predicate betweenPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {

        List<?> values = (List<?>) rawValue;

        Object lowerBound = value(values.get(0));
        Object upperBound = value(values.get(1));

        if (ignoreCase && isStringPath(path)) {
            Expression<String> field = cb.upper(path.as(String.class));

            Expression<String> lowerExpression = toStringExpression(cb, lowerBound);
            Expression<String> upperExpression = toStringExpression(cb, upperBound);

            return cb.between(
                    field,
                    cb.upper(lowerExpression),
                    cb.upper(upperExpression)
            );
        }

        return cb.between(
                path.as(Comparable.class),
                (Comparable) lowerBound,
                (Comparable) upperBound
        );
    }

    private Predicate andPredicate(CriteriaBuilder cb, Root<?> root, Element element, boolean ignoreCase) {
        List<CriteriaCondition> conditions =
                element.value().get(new TypeReference<>() {});

        return cb.and(
                conditions.stream()
                        .map(c -> toPredicate(c, cb, root, ignoreCase))
                        .toArray(Predicate[]::new)
        );
    }

    private Predicate orPredicate(CriteriaBuilder cb, Root<?> root, Element element, boolean ignoreCase) {
        List<CriteriaCondition> conditions =
                element.value().get(new TypeReference<>() {});

        return cb.or(
                conditions.stream()
                        .map(c -> toPredicate(c, cb, root, ignoreCase))
                        .toArray(Predicate[]::new)
        );
    }

    private Predicate notPredicate(CriteriaBuilder cb, Root<?> root, Element element, boolean ignoreCase) {
        CriteriaCondition inner = element.get(CriteriaCondition.class);
        return cb.not(toPredicate(inner, cb, root, ignoreCase));
    }

    private boolean isStringPath(Path<?> path) {
        return path != null && String.class.isAssignableFrom(path.getJavaType());
    }

    private Expression<String> toStringExpression(CriteriaBuilder cb, Object value) {
        if (value instanceof Expression<?> expression) {
            return (Expression<String>) expression;
        }
        return cb.literal(value.toString());
    }

    private static Object value(Object value) {
        if (value instanceof Value wrapped) {
            return wrapped.get();
        }
        return value;
    }


    interface PathResolver {
        Path<?> resolvePath(Path<?> root, String property);
    }
}