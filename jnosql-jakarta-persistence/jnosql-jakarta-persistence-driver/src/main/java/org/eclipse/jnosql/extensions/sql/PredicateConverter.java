/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
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
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.StandardFunction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;
import org.eclipse.jnosql.communication.semistructured.Element;

import java.util.List;

@SuppressWarnings("unchecked")
final class PredicateConverter {

    private final PathResolver pathResolver;

    private final NativeExpressionConverter expressionConverter;

    PredicateConverter(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        this.expressionConverter = new NativeExpressionConverter(pathResolver);
    }

    void applyCondition(CriteriaCondition criteriaCondition,
                        CriteriaBuilder criteriaBuilder,
                        Root<?> root,
                        CriteriaQuery<?> criteriaQuery,
                        EntityManager entityManager) {
        if (criteriaCondition == null) {
            return;
        }
        Predicate predicate = toPredicate(criteriaCondition, criteriaBuilder, root, entityManager);
        if (predicate != null) {
            criteriaQuery.where(predicate);
        }
    }

    Predicate toPredicate(CriteriaCondition condition,
                          CriteriaBuilder criteriaBuilder,
                          Root<?> root, EntityManager entityManager) {
        return toPredicate(condition, criteriaBuilder, root, false, entityManager);
    }

    private Predicate toPredicate(CriteriaCondition condition,
                                  CriteriaBuilder criteriaBuilder,
                                  Root<?> root,
                                  boolean ignoreCase,
                                  EntityManager entityManager) {

        if (condition.isNative()) {
            return nativePredicate(condition, criteriaBuilder, root, ignoreCase, entityManager);
        }

        Element element = condition.element();
        String property = element.name();
        Object rawValue = element.get();

        return switch (condition.condition()) {

            case EQUALS -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield equalsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case LIKE -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield likePredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case CONTAINS -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield containsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case STARTS_WITH -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield startsWithPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case ENDS_WITH -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield endsWithPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case GREATER_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield greaterThanPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case GREATER_EQUALS_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield greaterEqualsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case LESSER_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield lessThanPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case LESSER_EQUALS_THAN -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield lessEqualsPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case IN -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield inPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case BETWEEN -> {
                Path<?> path = pathResolver.resolvePath(root, property, entityManager);
                yield betweenPredicate(criteriaBuilder, path, rawValue, ignoreCase);
            }

            case AND -> andPredicate(criteriaBuilder, root, element, ignoreCase, entityManager);

            case OR -> orPredicate(criteriaBuilder, root, element, ignoreCase, entityManager);

            case NOT -> notPredicate(criteriaBuilder, root, element, ignoreCase, entityManager);

            case IGNORE_CASE -> {
                CriteriaCondition inner = element.get(CriteriaCondition.class);
                yield toPredicate(inner, criteriaBuilder, root, true, entityManager);
            }
        };
    }

    private Predicate nativePredicate(CriteriaCondition condition, CriteriaBuilder cb, Root<?> root,
                                      boolean ignoreCase, EntityManager entityManager) {
        CriteriaOperand leftOperand = condition.leftOperand();
        CriteriaOperand rightOperand = condition.rightOperand();
        Expression<?> left = expressionConverter.operand(leftOperand, cb, root, entityManager);
        boolean caseInsensitive = ignoreCase && isStringExpression(left);
        if (caseInsensitive) {
            left = upper(cb, left, leftOperand);
        }
        Condition type = condition.condition();

        if (rightOperand instanceof CriteriaOperand.Constant constant) {
            Object value = value(constant.value());
            return switch (type) {
                case EQUALS -> value == null ? cb.isNull(left)
                        : cb.equal(left, caseInsensitive ? upper(value) : value);
                case GREATER_THAN -> cb.greaterThan((Expression<Comparable>) left,
                        comparable(type, caseInsensitive ? upper(value) : value));
                case GREATER_EQUALS_THAN -> cb.greaterThanOrEqualTo((Expression<Comparable>) left,
                        comparable(type, caseInsensitive ? upper(value) : value));
                case LESSER_THAN -> cb.lessThan((Expression<Comparable>) left,
                        comparable(type, caseInsensitive ? upper(value) : value));
                case LESSER_EQUALS_THAN -> cb.lessThanOrEqualTo((Expression<Comparable>) left,
                        comparable(type, caseInsensitive ? upper(value) : value));
                case LIKE -> {
                    requireString(type, left);
                    yield cb.like((Expression<String>) left, String.valueOf(caseInsensitive ? upper(value) : value));
                }
                case IN -> nativeIn(cb, left, value, caseInsensitive);
                case BETWEEN -> {
                    List<?> bounds = (List<?>) value;
                    yield cb.between((Expression<Comparable>) left,
                            comparable(type, caseInsensitive ? upper(value(bounds.get(0))) : value(bounds.get(0))),
                            comparable(type, caseInsensitive ? upper(value(bounds.get(1))) : value(bounds.get(1))));
                }
                default -> throw unsupported(condition);
            };
        }

        Expression<?> right = expressionConverter.operand(rightOperand, cb, root, entityManager);
        if (caseInsensitive && isStringExpression(right)) {
            right = upper(cb, right, rightOperand);
        }
        return switch (type) {
            case EQUALS -> cb.equal(left, right);
            case GREATER_THAN -> cb.greaterThan((Expression<Comparable>) left, (Expression<Comparable>) right);
            case GREATER_EQUALS_THAN ->
                    cb.greaterThanOrEqualTo((Expression<Comparable>) left, (Expression<Comparable>) right);
            case LESSER_THAN -> cb.lessThan((Expression<Comparable>) left, (Expression<Comparable>) right);
            case LESSER_EQUALS_THAN ->
                    cb.lessThanOrEqualTo((Expression<Comparable>) left, (Expression<Comparable>) right);
            default -> throw unsupported(condition);
        };
    }

    private Predicate nativeIn(CriteriaBuilder cb, Expression<?> left, Object value, boolean caseInsensitive) {
        CriteriaBuilder.In<Object> in = cb.in(left);
        for (Object item : (Iterable<?>) value) {
            Object resolved = value(item);
            if (resolved instanceof Iterable<?> iterable) {
                iterable.forEach(inner -> in.value(nativeInValue(inner, caseInsensitive)));
            } else {
                in.value(nativeInValue(resolved, caseInsensitive));
            }
        }
        return in;
    }

    private Object nativeInValue(Object item, boolean caseInsensitive) {
        Object resolved = value(item);
        if (resolved == null) {
            throw new UnsupportedOperationException("A null value is not supported in an IN predicate "
                    + "with a native function");
        }
        return caseInsensitive ? upper(resolved) : resolved;
    }

    // IGNORE_CASE on an UPPER function would apply the same conversion twice
    private Expression<String> upper(CriteriaBuilder cb, Expression<?> expression, CriteriaOperand operand) {
        if (operand instanceof CriteriaOperand.Function function
                && StandardFunction.UPPER.name().equalsIgnoreCase(function.function().name())) {
            return (Expression<String>) expression;
        }
        return cb.upper((Expression<String>) expression);
    }

    private static Object upper(Object value) {
        return value instanceof String text ? text.toUpperCase() : value;
    }

    @SuppressWarnings("rawtypes")
    private static Comparable comparable(Condition condition, Object value) {
        if (value instanceof Comparable comparable) {
            return comparable;
        }
        throw new UnsupportedOperationException("The condition " + condition
                + " is not supported with a native function and the value " + value);
    }

    private static void requireString(Condition condition, Expression<?> expression) {
        if (!isStringExpression(expression)) {
            throw new UnsupportedOperationException("The condition " + condition
                    + " requires a native function that returns a string");
        }
    }

    private static boolean isStringExpression(Expression<?> expression) {
        return String.class.equals(expression.getJavaType());
    }

    private static UnsupportedOperationException unsupported(CriteriaCondition condition) {
        return new UnsupportedOperationException("The condition " + condition.condition()
                + " is not supported with a native function operand: " + condition);
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

    private Predicate andPredicate(CriteriaBuilder cb, Root<?> root, Element element, boolean ignoreCase, EntityManager entityManager) {
        List<CriteriaCondition> conditions =
                element.value().get(new TypeReference<>() {});

        return cb.and(
                conditions.stream()
                        .map(c -> toPredicate(c, cb, root, ignoreCase, entityManager))
                        .toArray(Predicate[]::new)
        );
    }

    private Predicate orPredicate(CriteriaBuilder cb, Root<?> root, Element element, boolean ignoreCase, EntityManager entityManager) {
        List<CriteriaCondition> conditions =
                element.value().get(new TypeReference<>() {});

        return cb.or(
                conditions.stream()
                        .map(c -> toPredicate(c, cb, root, ignoreCase, entityManager))
                        .toArray(Predicate[]::new)
        );
    }

    private Predicate notPredicate(CriteriaBuilder cb, Root<?> root, Element element, boolean ignoreCase, EntityManager entityManager) {
        CriteriaCondition inner = element.get(CriteriaCondition.class);
        return cb.not(toPredicate(inner, cb, root, ignoreCase, entityManager));
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
        Path<?> resolvePath(Path<?> root, String property, EntityManager entityManager);
    }
}