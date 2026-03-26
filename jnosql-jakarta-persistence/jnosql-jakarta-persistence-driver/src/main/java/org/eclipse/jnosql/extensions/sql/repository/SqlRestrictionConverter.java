/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.data.constraint.AtLeast;
import jakarta.data.constraint.AtMost;
import jakarta.data.constraint.Between;
import jakarta.data.constraint.Constraint;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.GreaterThan;
import jakarta.data.constraint.In;
import jakarta.data.constraint.LessThan;
import jakarta.data.constraint.Like;
import jakarta.data.constraint.NotBetween;
import jakarta.data.constraint.NotEqualTo;
import jakarta.data.constraint.NotIn;
import jakarta.data.constraint.NotLike;
import jakarta.data.constraint.NotNull;
import jakarta.data.constraint.Null;
import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.restrict.BasicRestriction;
import jakarta.data.restrict.CompositeRestriction;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldParameterMetadata;
import org.eclipse.jnosql.mapping.semistructured.query.UnsatisfiableQueryException;
import org.eclipse.jnosql.mapping.semistructured.query.ValueConverter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.and;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.between;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.gt;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.gte;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.in;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.like;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.lt;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.lte;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.or;

enum SqlRestrictionConverter {
    INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(SqlRestrictionConverter.class.getName());

    public Optional<CriteriaCondition> parser(Restriction<?> restriction, EntityMetadata entityMetadata) {
        Objects.requireNonNull(restriction, "restriction is required");
        Objects.requireNonNull(entityMetadata, "entityMetadata is required");

        LOGGER.fine(() -> "Converter is invoked for restriction " + restriction);

        CriteriaCondition criteriaCondition;
        switch (restriction) {
            case BasicRestriction<?, ?> basicRestriction -> {
                if (basicRestriction.expression() instanceof BasicAttribute<?, ?> basicAttribute) {
                    Constraint<?> constraint = basicRestriction.constraint();
                    criteriaCondition = condition(basicAttribute, constraint, entityMetadata);
                } else {
                    throw new UnsupportedOperationException("The expression " + basicRestriction.expression() + " is not supported");
                }
            }
            case CompositeRestriction<?> compositeRestriction -> {

                if (isUnmatchable(restriction)) {
                    throw new UnsatisfiableQueryException(
                            "The query restrictions evaluate to an always-false predicate and cannot be "
                                    + "satisfied by NoSQL databases. The query execution was short-circuited: "
                                    + restriction
                    );
                } else if (isUnrestricted(restriction)) {
                    return Optional.empty();
                }
                var negated = compositeRestriction.isNegated();
                var conditions = compositeRestriction.restrictions()
                        .stream()
                        .map(r -> {
                            if(r instanceof CompositeRestriction<?>) {
                                return parser(r, entityMetadata, converters)
                                        .orElseThrow(() -> new UnsupportedOperationException(
                                                "Cannot parse nested composite restriction: " + r));
                            }
                            return r;
                        })
                        .map(r ->{
                            if(r instanceof BasicRestriction<?, ?> basicRestriction) {
                                return parser(negated? basicRestriction.negate(): basicRestriction, entityMetadata,
                                        converters);
                            }
                            return Optional.ofNullable((CriteriaCondition)r);
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toArray(CriteriaCondition[]::new);
                criteriaCondition = switch (compositeRestriction.type()) {
                    case ALL -> negated ? or(conditions) : and(conditions);
                    case ANY -> negated ? and(conditions) : or(conditions);
                };
            }
            default ->
                    throw new UnsupportedOperationException("Unsupported restriction type: " + restriction.getClass().getName());
        }
        return Optional.ofNullable(criteriaCondition);
    }

    private CriteriaCondition condition(BasicAttribute<?, ?> basicAttribute, Constraint<?> constraint) {
        var name = basicAttribute.name();


        switch (constraint) {

            case EqualTo<?> equalTo -> {
                var value = ValueConverter.of(equalTo::expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return eq(nativeColumn, value);
            }

            case NotEqualTo<?> notEqualTo -> {
                var value = ValueConverter.of(notEqualTo::expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return eq(nativeColumn, value).negate();
            }

            case LessThan<?> lessThan -> {
                var value = ValueConverter.of(lessThan::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return lt(nativeColumn, value);
            }

            case GreaterThan<?> greaterThan -> {
                var value = ValueConverter.of(greaterThan::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return gt(nativeColumn, value);
            }

            case AtLeast<?> greaterThanOrEqual -> {
                var value = ValueConverter.of(greaterThanOrEqual::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return gte(nativeColumn, value);
            }

            case AtMost<?> lesserThanOrEqual -> {
                var value = ValueConverter.of(lesserThanOrEqual::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return lte(nativeColumn, value);
            }

            case Between<?> between -> {
                var lowerBound = ValueConverter.of(between::lowerBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                var upperBound = ValueConverter.of(between::upperBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return between(nativeColumn, List.of(lowerBound, upperBound));
            }

            case NotBetween<?> between -> {
                var lowerBound = ValueConverter.of(between::lowerBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                var upperBound = ValueConverter.of(between::upperBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return between(nativeColumn, List.of(lowerBound, upperBound)).negate();
            }

            case Like like -> {
                var value = ValueConverter.of(like::pattern, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return like(nativeColumn, value);
            }

            case NotLike like -> {
                var value = ValueConverter.of(like::pattern, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return like(nativeColumn, value).negate();
            }

            case Null<?> isNull -> {
                return eq(nativeColumn, Value.ofNull());
            }

            case NotNull<?> isNull -> {
                return eq(nativeColumn, Value.ofNull()).negate();
            }

            case In<?> in -> {
                var values = in.expressions().stream().map(expression -> ValueConverter.of(() -> expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null))).toList();
                return in(nativeColumn, values);
            }

            case NotIn<?> in -> {
                var values = in.expressions().stream().map(expression -> ValueConverter.of(() -> expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null))).toList();
                return in(nativeColumn, values).negate();
            }

            default -> throw new UnsupportedOperationException("Unexpected value: " + constraint);
        }

}
