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
                                    CriteriaBuilder cb,
                                    Root<?> root) {
        return toPredicate(condition, cb, root, false);
    }

    private Predicate toPredicate(CriteriaCondition condition,
                                  CriteriaBuilder cb,
                                  Root<?> root,
                                  boolean ignoreCase) {

        Element element = condition.element();
        String property = element.name();
        Object rawValue = element.get();
        Path<?> path = path(root, property, rawValue);

        return switch (condition.condition()) {

            case EQUALS -> equalsPredicate(cb, path, rawValue, ignoreCase);

            case LIKE -> likePredicate(cb, path, rawValue, ignoreCase);

            case CONTAINS -> containsPredicate(cb, path, rawValue, ignoreCase);

            case STARTS_WITH -> startsWithPredicate(cb, path, rawValue, ignoreCase);

            case ENDS_WITH -> endsWithPredicate(cb, path, rawValue, ignoreCase);

            case GREATER_THAN -> greaterThanPredicate(cb, path, rawValue, ignoreCase);

            case GREATER_EQUALS_THAN -> greaterEqualsPredicate(cb, path, rawValue, ignoreCase);

            case LESSER_THAN -> lessThanPredicate(cb, path, rawValue, ignoreCase);

            case LESSER_EQUALS_THAN -> lessEqualsPredicate(cb, path, rawValue, ignoreCase);

            case IN -> inPredicate(cb, path, rawValue, ignoreCase);

            case BETWEEN -> betweenPredicate(cb, path, rawValue, ignoreCase);

            case AND -> andPredicate(cb, root, element, ignoreCase);

            case OR -> orPredicate(cb, root, element, ignoreCase);

            case NOT -> notPredicate(cb, root, element, ignoreCase);

            case IGNORE_CASE -> {
                var inner = element.get(CriteriaCondition.class);
                yield toPredicate(inner, cb, root, true);
            }
        };
    }

    private Predicate equalsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.equal(field, cb.upper((Expression<String>) expr));
            }

            return cb.equal(field, v.toString().toUpperCase());
        }

        return cb.equal(path, v);
    }

    private Predicate likePredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.like(field, cb.upper((Expression<String>) expr));
            }

            return cb.like(field, v.toString().toUpperCase());
        }

        return cb.like(path.as(String.class), v.toString());
    }

    private Predicate containsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.like(field, cb.upper((Expression<String>) expr));
            }

            return cb.like(field, "%" + v.toString().toUpperCase() + "%");
        }

        return cb.like(path.as(String.class), "%" + v + "%");
    }

    private Predicate startsWithPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.like(field, cb.upper((Expression<String>) expr));
            }

            return cb.like(field, v.toString().toUpperCase() + "%");
        }

        return cb.like(path.as(String.class), v + "%");
    }

    private Predicate endsWithPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.like(field, cb.upper((Expression<String>) expr));
            }

            return cb.like(field, "%" + v.toString().toUpperCase());
        }

        return cb.like(path.as(String.class), "%" + v);
    }

    private Predicate greaterThanPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.greaterThan(field, cb.upper((Expression<String>) expr));
            }

            return cb.greaterThan(field, v.toString().toUpperCase());
        }

        return cb.greaterThan(path.as(Comparable.class), (Comparable) v);
    }

    private Predicate greaterEqualsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.greaterThanOrEqualTo(field, cb.upper((Expression<String>) expr));
            }

            return cb.greaterThanOrEqualTo(field, v.toString().toUpperCase());
        }

        return cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) v);
    }

    private Predicate lessThanPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.lessThan(field, cb.upper((Expression<String>) expr));
            }

            return cb.lessThan(field, v.toString().toUpperCase());
        }

        return cb.lessThan(path.as(Comparable.class), (Comparable) v);
    }

    private Predicate lessEqualsPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        Object v = value(rawValue, cb, path);

        if (ignoreCase && isStringPath(path)) {
            var field = cb.upper(path.as(String.class));

            if (v instanceof Expression<?> expr) {
                return cb.lessThanOrEqualTo(field, cb.upper((Expression<String>) expr));
            }

            return cb.lessThanOrEqualTo(field, v.toString().toUpperCase());
        }

        return cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) v);
    }

    private Predicate inPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        CriteriaBuilder.In<Object> in;

        if (ignoreCase && isStringPath(path)) {
            in = cb.in(cb.upper(path.as(String.class)));

            ((Iterable<?>) rawValue).forEach(v ->
                    in.value(v.toString().toUpperCase())
            );

            return in;
        }

        in = cb.in(path);
        ((Iterable<?>) rawValue).forEach(v ->
                in.value(value(v, cb, path))
        );
        return in;
    }

    private Predicate betweenPredicate(CriteriaBuilder cb, Path<?> path, Object rawValue, boolean ignoreCase) {
        var values = (List<?>) rawValue;

        if (ignoreCase && isStringPath(path)) {
            return cb.between(
                    cb.upper(path.as(String.class)),
                    values.get(0).toString().toUpperCase(),
                    values.get(1).toString().toUpperCase()
            );
        }

        return cb.between(
                path.as(Comparable.class),
                (Comparable) value(values.get(0), cb, path),
                (Comparable) value(values.get(1), cb, path)
        );
    }

    private Predicate andPredicate(CriteriaBuilder cb,
                                   Root<?> root,
                                   Element element,
                                   boolean ignoreCase) {
        List<CriteriaCondition> conditions =
                element.value().get(new TypeReference<>() {});

        return cb.and(
                conditions.stream()
                        .map(c -> toPredicate(c, cb, root, ignoreCase))
                        .toArray(Predicate[]::new)
        );
    }

    private Predicate orPredicate(CriteriaBuilder cb,
                                  Root<?> root,
                                  Element element,
                                  boolean ignoreCase) {
        List<CriteriaCondition> conditions =
                element.value().get(new TypeReference<>() {});

        return cb.or(
                conditions.stream()
                        .map(c -> toPredicate(c, cb, root, ignoreCase))
                        .toArray(Predicate[]::new)
        );
    }

    private Predicate notPredicate(CriteriaBuilder cb,
                                   Root<?> root,
                                   Element element,
                                   boolean ignoreCase) {
        var inner = element.get(CriteriaCondition.class);
        return cb.not(toPredicate(inner, cb, root, ignoreCase));
    }

    private boolean isStringPath(Path<?> path) {
        return path != null && String.class.isAssignableFrom(path.getJavaType());
    }

    private static Object value(Object value, CriteriaBuilder cb, Path<?> path) {
        if (value instanceof org.eclipse.jnosql.communication.ParamValue param) {
            return cb.parameter(path.getJavaType(), param.getName());
        }
        if (value instanceof Value wrapped) {
            return wrapped.get();
        }
        return value;
    }

    private Path<?> path(Root<?> root, String property, Object value) {
        if (value instanceof CriteriaCondition) {
            return null;
        }
        return pathResolver.resolvePath(root, property);
    }

    interface PathResolver {
        Path<?> resolvePath(Path<?> root, String property);
    }
}