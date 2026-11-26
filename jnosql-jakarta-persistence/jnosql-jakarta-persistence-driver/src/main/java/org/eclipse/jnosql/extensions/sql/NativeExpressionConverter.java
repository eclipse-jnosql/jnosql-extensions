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
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.function.ArgumentType;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.eclipse.jnosql.communication.function.StandardFunction;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;

import java.math.BigInteger;
import java.util.List;

/**
 * Translates the structured operands of a native function predicate into Jakarta Persistence Criteria
 * {@link Expression expressions}.
 *
 * <p>Persistent attribute references are resolved by the {@link PredicateConverter.PathResolver}, evaluable
 * arguments are consumed as the values already produced by the JNoSQL core, and native functions are mapped to
 * portable {@link CriteriaBuilder} operations. A function that cannot be translated is rejected with an
 * {@link UnsupportedOperationException}; it is never ignored, flattened into an attribute name or evaluated in Java.</p>
 */
@SuppressWarnings("unchecked")
final class NativeExpressionConverter {

    private final PredicateConverter.PathResolver pathResolver;

    NativeExpressionConverter(PredicateConverter.PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    Expression<?> operand(CriteriaOperand operand, CriteriaBuilder cb, Root<?> root, EntityManager em) {
        return switch (operand) {
            case CriteriaOperand.Attribute attribute -> attribute(attribute.name(), root, em);
            case CriteriaOperand.Function function -> function(function.function(), cb, root, em);
            case CriteriaOperand.Constant constant -> cb.literal(constant.value().get());
        };
    }

    Expression<?> function(NativeFunction function, CriteriaBuilder cb, Root<?> root, EntityManager em) {
        StandardFunction standard = StandardFunction.find(function.name()).orElseThrow(() ->
                new UnsupportedOperationException("The native function " + function.name()
                        + " is not supported by the Jakarta Persistence extension"));
        List<FunctionArgument> arguments = function.arguments();
        return switch (standard) {
            case ABS -> cb.abs((Expression<Number>) argument(standard, arguments, 0, cb, root, em));
            case LENGTH -> cb.length(text(standard, arguments, 0, cb, root, em));
            case LOWER -> cb.lower(text(standard, arguments, 0, cb, root, em));
            case UPPER -> cb.upper(text(standard, arguments, 0, cb, root, em));
            case LEFT -> left(standard, arguments, cb, root, em);
            case RIGHT -> right(standard, arguments, cb, root, em);
        };
    }

    private Expression<String> left(StandardFunction function, List<FunctionArgument> arguments,
                                    CriteriaBuilder cb, Root<?> root, EntityManager em) {
        Expression<String> text = text(function, arguments, 0, cb, root, em);
        Expression<Integer> length = length(function, arguments, 1, cb, root, em);
        if (!isEvaluable(arguments.get(1))) {
            // Some databases (Derby) reject a SUBSTR length computed from data that exceeds the text length
            length = cb.<Integer>selectCase()
                    .when(cb.gt(length, cb.length(text)), cb.length(text))
                    .otherwise(length);
        }
        return cb.substring(text, cb.literal(1), length);
    }

    /*
     * RIGHT(text, n) is substring(text, max(1, length(text) - n + 1)). The start is never below the one-based
     * first position, so a length beyond the text returns the whole text and a zero length starts after the end.
     */
    private Expression<String> right(StandardFunction function, List<FunctionArgument> arguments,
                                     CriteriaBuilder cb, Root<?> root, EntityManager em) {
        Expression<String> text = text(function, arguments, 0, cb, root, em);
        Expression<Integer> length = length(function, arguments, 1, cb, root, em);
        Expression<Integer> start = cb.sum(cb.diff(cb.length(text), length), 1);
        Expression<Integer> bounded = cb.<Integer>selectCase()
                .when(cb.lt(start, 1), 1)
                .otherwise(start);
        return cb.substring(text, bounded);
    }

    private Expression<String> text(StandardFunction function, List<FunctionArgument> arguments, int index,
                                    CriteriaBuilder cb, Root<?> root, EntityManager em) {
        return (Expression<String>) argument(function, arguments, index, cb, root, em);
    }

    private Expression<Integer> length(StandardFunction function, List<FunctionArgument> arguments, int index,
                                       CriteriaBuilder cb, Root<?> root, EntityManager em) {
        FunctionArgument argument = arguments.get(index);
        if (isEvaluable(argument)) {
            Object value = evaluated(argument, function, index);
            if (value == null) {
                return cb.nullLiteral(Integer.class);
            }
            return cb.literal(length(function, index, value));
        }
        Expression<?> expression = argument(function, arguments, index, cb, root, em);
        if (Integer.class.equals(boxed(expression.getJavaType()))) {
            return (Expression<Integer>) expression;
        }
        return expression.cast(Integer.class);
    }

    private Expression<?> argument(StandardFunction function, List<FunctionArgument> arguments, int index,
                                   CriteriaBuilder cb, Root<?> root, EntityManager em) {
        ArgumentType type = function.parameters().get(index);
        FunctionArgument argument = arguments.get(index);
        return switch (argument) {
            case FunctionArgument.AttributePath path -> {
                Expression<?> attribute = attribute(path.path(), root, em);
                requireType(function, index, type, attribute);
                yield attribute;
            }
            case FunctionArgument.NativeArgument nested -> function(nested.function(), cb, root, em);
            default -> literal(function, index, type, evaluated(argument, function, index), cb);
        };
    }

    private Expression<?> attribute(String name, Root<?> root, EntityManager em) {
        Path<?> path = pathResolver.resolvePath(root, name, em);
        if (path == null) {
            throw new UnsupportedOperationException("The attribute " + name
                    + " cannot be used as a native function argument");
        }
        return path;
    }

    private Expression<?> literal(StandardFunction function, int index, ArgumentType type, Object value,
                                  CriteriaBuilder cb) {
        if (value == null) {
            Class<?> nullType = switch (type) {
                case STRING -> String.class;
                case INTEGER -> Integer.class;
                case NUMERIC -> Number.class;
            };
            return cb.nullLiteral(nullType);
        }
        if (!type.accepts(value)) {
            throw new IllegalArgumentException("Invalid argument type for " + function.name() + " at position "
                    + (index + 1) + ": expected " + type + " but found " + value.getClass().getName());
        }
        return cb.literal(value);
    }

    private static boolean isEvaluable(FunctionArgument argument) {
        return !(argument instanceof FunctionArgument.AttributePath)
                && !(argument instanceof FunctionArgument.NativeArgument);
    }

    private static Object evaluated(FunctionArgument argument, StandardFunction function, int index) {
        return switch (argument) {
            case FunctionArgument.Literal literal -> literal.value();
            case FunctionArgument.ValueArgument value -> value.value().get();
            case FunctionArgument.EvaluableArgument nested -> nested.function().get();
            case FunctionArgument.NamedParameter parameter -> throw new IllegalStateException(
                    "Unbound parameter :" + parameter.name() + " in " + function.name() + " at position "
                            + (index + 1));
            case FunctionArgument.PositionalParameter parameter -> throw new IllegalStateException(
                    "Unbound parameter ?" + parameter.position() + " in " + function.name() + " at position "
                            + (index + 1));
            default -> throw new UnsupportedOperationException("The argument " + argument + " of "
                    + function.name() + " cannot be evaluated independently of the database");
        };
    }

    // mirrors the core contract: negative lengths are invalid and lengths beyond the int range are clamped
    private static int length(StandardFunction function, int index, Object value) {
        if (!ArgumentType.INTEGER.accepts(value)) {
            throw new IllegalArgumentException("Invalid argument type for " + function.name() + " at position "
                    + (index + 1) + ": expected " + ArgumentType.INTEGER + " but found "
                    + value.getClass().getName());
        }
        if (value instanceof BigInteger big) {
            if (big.signum() < 0) {
                throw negative(function, index, value);
            }
            return big.bitLength() > 31 ? Integer.MAX_VALUE : big.intValue();
        }
        long number = ((Number) value).longValue();
        if (number < 0) {
            throw negative(function, index, value);
        }
        return number > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) number;
    }

    private static IllegalArgumentException negative(StandardFunction function, int index, Object value) {
        return new IllegalArgumentException("Invalid argument value for " + function.name() + " at position "
                + (index + 1) + ": the length must not be negative but was " + value);
    }

    private static void requireType(StandardFunction function, int index, ArgumentType type, Expression<?> attribute) {
        Class<?> javaType = boxed(attribute.getJavaType());
        boolean compatible = javaType != null && switch (type) {
            case STRING -> String.class.equals(javaType);
            case INTEGER -> javaType == Byte.class || javaType == Short.class || javaType == Integer.class
                    || javaType == Long.class || javaType == BigInteger.class;
            case NUMERIC -> Number.class.isAssignableFrom(javaType);
        };
        if (!compatible) {
            throw new UnsupportedOperationException("The attribute type " + attribute.getJavaType()
                    + " is not supported by " + function.name() + " at position " + (index + 1)
                    + ": expected " + type);
        }
    }

    static Class<?> boxed(Class<?> type) {
        if (type == null || !type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "boolean" -> Boolean.class;
            case "char" -> Character.class;
            default -> type;
        };
    }
}
