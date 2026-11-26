/*
 * Copyright (c) 2024,2026 Contributors to the Eclipse Foundation
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
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.util.List;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.StandardFunction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;

/**
 * Builds Criteria predicates for conditions that have a native function operand, such as
 * {@code UPPER(name) = 'OTAVIO'}. Operands are converted to Criteria expressions by
 * {@link NativeExpressionConverter}. A condition that cannot be translated is rejected with an
 * {@link UnsupportedOperationException}; it is never ignored or weakened.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class NativePredicateParser {

    private NativePredicateParser() {
    }

    static <FROM> Predicate parse(CriteriaCondition condition, BaseQueryParser.QueryContext<FROM> ctx,
            boolean ignoreCase) {
        CriteriaBuilder cb = ctx.builder();
        NativeExpressionConverter converter = NativeExpressionConverter.INSTANCE;
        CriteriaOperand leftOperand = condition.leftOperand();
        CriteriaOperand rightOperand = condition.rightOperand();
        Expression<?> left = converter.operand(leftOperand, cb, ctx.root());
        boolean caseInsensitive = ignoreCase && isStringExpression(left);
        if (caseInsensitive) {
            left = upper(cb, left, leftOperand);
        }
        Condition type = condition.condition();

        if (rightOperand instanceof CriteriaOperand.Constant constant) {
            Object value = unwrap(constant.value());
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
                case IN -> in(cb, left, value, caseInsensitive);
                case BETWEEN -> {
                    List<?> bounds = (List<?>) value;
                    Object lower = unwrap(bounds.get(0));
                    Object upper = unwrap(bounds.get(1));
                    yield cb.between((Expression<Comparable>) left,
                            comparable(type, caseInsensitive ? upper(lower) : lower),
                            comparable(type, caseInsensitive ? upper(upper) : upper));
                }
                default -> throw unsupported(condition);
            };
        }

        Expression<?> right = converter.operand(rightOperand, cb, ctx.root());
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

    private static Predicate in(CriteriaBuilder cb, Expression<?> left, Object value, boolean caseInsensitive) {
        CriteriaBuilder.In<Object> in = cb.in(left);
        for (Object item : (Iterable<?>) value) {
            Object resolved = unwrap(item);
            if (resolved instanceof Iterable<?> iterable) {
                iterable.forEach(inner -> in.value(inValue(inner, caseInsensitive)));
            } else {
                in.value(inValue(resolved, caseInsensitive));
            }
        }
        return in;
    }

    private static Object inValue(Object item, boolean caseInsensitive) {
        Object resolved = unwrap(item);
        if (resolved == null) {
            throw new UnsupportedOperationException("A null value is not supported in an IN predicate "
                    + "with a native function");
        }
        return caseInsensitive ? upper(resolved) : resolved;
    }

    private static Object unwrap(Object value) {
        return value instanceof Value wrapped ? wrapped.get() : value;
    }

    // IGNORE_CASE on an UPPER function would apply the same conversion twice
    private static Expression<String> upper(CriteriaBuilder cb, Expression<?> expression, CriteriaOperand operand) {
        if (operand instanceof CriteriaOperand.Function function
                && StandardFunction.UPPER.name().equalsIgnoreCase(function.function().name())) {
            return (Expression<String>) expression;
        }
        return cb.upper((Expression<String>) expression);
    }

    private static Object upper(Object value) {
        return value instanceof String text ? text.toUpperCase() : value;
    }

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
}
