/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.data.Sort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.ParamValue;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;

import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.BETWEEN;
import static org.eclipse.jnosql.communication.Condition.CONTAINS;
import static org.eclipse.jnosql.communication.Condition.ENDS_WITH;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.GREATER_THAN;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.LIKE;
import static org.eclipse.jnosql.communication.Condition.NOT;

abstract class BaseQueryParser {

    protected final PersistenceDatabaseManager manager;

    protected BaseQueryParser(PersistenceDatabaseManager manager) {
        this.manager = manager;
    }

    protected abstract <T> Stream<T> query(String queryString, String entity, Collection<Sort<?>> sorts, Consumer<Query> queryModifier);

    public <T> Stream<T> query(String queryString) {
        return query(queryString, null, null, null);
    }

    public <T> Stream<T> query(String queryString, String entity) {
        return query(queryString, entity, null, null);
    }

    public Query buildQuery(String queryString) {
        return buildQuery(queryString, null, null);
    }

    public Query buildQuery(String queryString, String entity) {
        return buildQuery(queryString, entity, null);
    }

    protected <T> Class<T> entityClassFromEntityName(String entityName) {
        final EntityType<T> entityType = manager.findEntityType(entityName);
        return entityType.getJavaType();
    }

    protected <T> EntityType<T> entityTypeFromEntityName(String entityName) {
        return manager.findEntityType(entityName);
    }

    protected Class<?> entityAttributeClass(EntityType<?> entityType, String attributeName) {
        return entityType.getAttribute(attributeName).getJavaType();
    }

    protected EntityManager entityManager() {
        return manager.getEntityManager();
    }

    protected PersistenceUnitUtil getPersistenceUnitUtil() {
        return entityManager().getEntityManagerFactory().getPersistenceUnitUtil();
    }

    protected static String getName(Element element) {
        String name = element.name();
        return getFieldName(name);
    }

    protected static String getFieldName(String fieldName) {
        // NoSQL DBs translate id field into "_id" but we don't want it
        return fieldName.equalsIgnoreCase("_id") ? "id" : fieldName;
    }

    protected static Collection<?> elementCollection(CriteriaCondition criteria) {
        Element element = criteria.element();
        return (Collection<?>) element.value().get();
    }

    protected Query buildQuery(String queryString, String entity, Collection<Sort<?>> sorts) {
        EntityManager em = entityManager();
        return em.createQuery(queryString);
    }

    protected <T> TypedQuery<T> buildQuery(String queryString, String entity, Class<T> entityClass, Collection<Sort<?>> sorts) {
        EntityManager em = entityManager();
        return em.createQuery(queryString, entityClass);
    }

    protected static <FROM> Predicate parseCriteria(Object value, QueryContext<FROM> ctx) {
        return parseCriteria(value, ctx, false);
    }

    private static <FROM> Predicate parseCriteria(Object value, QueryContext<FROM> ctx, boolean ignoreCase) {
        if (value instanceof CriteriaCondition criteria) {
            return switch (criteria.condition()) {
                case NOT ->
                    ctx.builder().not(parseCriteria(criteria.element(), ctx));
                case EQUALS ->
                    parseEquals(criteria, ctx, ignoreCase);
                case AND ->
                    parseAnd(criteria, ctx);
                case OR ->
                    parseOr(criteria, ctx);
                case LESSER_THAN ->
                    parseLesserThan(criteria, ctx, ignoreCase);
                case LESSER_EQUALS_THAN ->
                    parseLesserThanEquals(criteria, ctx, ignoreCase);
                case GREATER_THAN ->
                    parseGreaterThan(criteria, ctx, ignoreCase);
                case GREATER_EQUALS_THAN ->
                    parseGreaterEqualsThan(criteria, ctx, ignoreCase);
                case BETWEEN ->
                    parseBetween(criteria, ctx, ignoreCase);
                case IN ->
                    parseIn(criteria, ctx, ignoreCase);
                case LIKE ->
                    parseLike(criteria, ctx, ignoreCase);
                case CONTAINS ->
                    parseContains(criteria, ctx, ignoreCase);
                case STARTS_WITH ->
                    parseStartsWith(criteria, ctx, ignoreCase);
                case ENDS_WITH ->
                    parseEndsWith(criteria, ctx, ignoreCase);
                case IGNORE_CASE ->
                    parseCriteria(criteria.element().value().get(), ctx, true);
                /* StartsWith ? */
                default ->
                    throw new UnsupportedOperationException("JNoSQL criteria condition "
                            + criteria.condition() + " is not supported yet.");
            };
        } else if (value instanceof Element element) {
            return parseCriteria(element.value().get(), ctx);
        }
        throw new UnsupportedOperationException("Parsing value " + value + " is not supported yet.");
    }

    private static <FROM> Predicate parseAnd(CriteriaCondition criteria, QueryContext<FROM> ctx) {
        return elementCollection(criteria).stream()
                .map(elem -> parseCriteria(elem, ctx))
                .reduce(ctx.builder()::and).get();
    }

    private static <FROM> Predicate parseOr(CriteriaCondition criteria, QueryContext<FROM> ctx) {
        return elementCollection(criteria).stream()
                .map(elem -> parseCriteria(elem, ctx))
                .reduce(ctx.builder()::or).get();
    }

    private static <FROM> Predicate parseEquals(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        Element element = (Element) criteria.element();
        if (element.value().isNull()) {
            return ctx.builder().isNull(ctx.root().get(getName(element)));
        } else {
            ComparableContext comparableContext = ComparableContext.from(ctx, criteria, ignoreCase);
            return ctx.builder().equal(comparableContext.field(), comparableContext.expression());
        }
    }

    private static <FROM> Predicate parseLesserThan(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        ComparableContext comparableContext = ComparableContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().lessThan(comparableContext.field(), comparableContext.expression());
    }

    private static <FROM> Predicate parseLesserThanEquals(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        ComparableContext comparableContext = ComparableContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().lessThanOrEqualTo(comparableContext.field(), comparableContext.expression());
    }

    private static <FROM> Predicate parseGreaterThan(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        ComparableContext comparableContext = ComparableContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().greaterThan(comparableContext.field(), comparableContext.expression());
    }

    private static <FROM> Predicate parseGreaterEqualsThan(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        ComparableContext comparableContext = ComparableContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().greaterThanOrEqualTo(comparableContext.field(), comparableContext.expression());
    }

    private static <FROM> Predicate parseBetween(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        BiComparableContext comparableContext = BiComparableContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().between(comparableContext.field(), comparableContext.expression1(), comparableContext.expression2());
    }

    private static <FROM> Predicate parseIn(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        MultiValueContext valueContext = MultiValueContext.from(ctx, criteria, ignoreCase);
        CriteriaBuilder.In<Object> inExpr = ctx.builder().in(valueContext.field());
        valueContext.fieldValues().forEach(v -> inExpr.value(v));
        return inExpr;
    }

    private static <FROM> Predicate parseLike(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        StringContext stringContext = StringContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().like(stringContext.field(), stringContext.fieldValue());
    }

    private static <FROM> Predicate parseContains(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        StringContext stringContext = StringContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().like(stringContext.field(), "%" + stringContext.fieldValue() + "%");
    }

    private static <FROM> Predicate parseStartsWith(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        StringContext stringContext = StringContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().like(stringContext.field(), stringContext.fieldValue() + "%");
    }

    private static <FROM> Predicate parseEndsWith(CriteriaCondition criteria, QueryContext<FROM> ctx, boolean ignoreCase) {
        StringContext stringContext = StringContext.from(ctx, criteria, ignoreCase);
        return ctx.builder().like(stringContext.field(), "%" + stringContext.fieldValue());
    }

    static Expression<? extends Comparable> getComparableExpression(CriteriaBuilder cb, Object value) {
        if (value instanceof Comparable result) {
            return cb.literal(result);
        } else if (value instanceof ParamValue param && param.isEmpty()) {
            // We only create parameter if we have no value
            // If we have the value, we use the value instead
            return cb.parameter(Comparable.class, param.getName());
        } else {
            return cb.literal(((Value) value).get(Comparable.class));
        }
    }

    static void requireComparisonSupportsIgnoreCase(CriteriaCondition criteria, Expression<? extends Comparable>... operands) {
            if (!Stream.of(operands).allMatch(BaseQueryParser::isStringExpression)) {
                throw new UnsupportedOperationException("IgnoreCase supported only for String values. Criteria: " + criteria);
            }
    }

    static <T> String toValueInUpperCase(CriteriaCondition criteria, T value) {
            if (value instanceof String stringValue) {
                return stringValue.toUpperCase();
            } else {
                throw new UnsupportedOperationException("IgnoreCase supported only for String values. Criteria: " + criteria);
            }
    }

    private static boolean isStringExpression(Expression<? extends Comparable> expression) {
        return expression.getJavaType().isAssignableFrom(String.class);
    }

    static record QueryContext<FROM>(Root<FROM> root, CriteriaBuilder builder) {
    }

    static record ComparableContext(Expression<? extends Comparable> field, Expression<? extends Comparable> expression) {

        public static <FROM> ComparableContext from(QueryContext ctx, CriteriaCondition criteria, boolean ignoreCase) {
            Element element = (Element) criteria.element();
            Expression<? extends Comparable> field = ctx.root().get(getName(element));
            Expression<? extends Comparable> expression = getComparableExpression(ctx.builder(), element.value());
            if (ignoreCase) {
                BaseQueryParser.requireComparisonSupportsIgnoreCase(criteria, field, expression);
                field = ctx.builder().upper((Expression<String>) field);
                expression = ctx.builder().upper((Expression<String>) expression);
            }
            return new ComparableContext(field, expression);
        }
    }

    static record StringContext(Expression<String> field, String fieldValue) {

        public static <FROM> StringContext from(QueryContext ctx, CriteriaCondition criteria, boolean ignoreCase) {
            Element element = (Element) criteria.element();
            Expression<String> field = ctx.root().get(getName(element));
            String fieldValue = element.value().get(String.class);
            if (ignoreCase) {
                BaseQueryParser.requireComparisonSupportsIgnoreCase(criteria, field);
                fieldValue = toValueInUpperCase(criteria, fieldValue);
                field = ctx.builder().upper((Expression<String>) field);
            }
            return new StringContext(field, fieldValue);
        }
    }

    static record BiComparableContext(Expression<? extends Comparable> field, Expression<? extends Comparable> expression1, Expression<? extends Comparable> expression2) {

        public static <FROM> BiComparableContext from(QueryContext ctx, CriteriaCondition criteria, boolean ignoreCase) {
            Element element = criteria.element();
            Expression<? extends Comparable> field = ctx.root().get(getName(element));
            Iterator<?> iterator = elementCollection(criteria).iterator();
            Expression<? extends Comparable> expression1 = getComparableExpression(ctx.builder(), iterator.next());
            Expression<? extends Comparable> expression2 = getComparableExpression(ctx.builder(), iterator.next());
            if (ignoreCase) {
                BaseQueryParser.requireComparisonSupportsIgnoreCase(criteria, field, expression1, expression2);
                field = ctx.builder().upper((Expression<String>) field);
                expression1 = ctx.builder().upper((Expression<String>) expression1);
                expression2 = ctx.builder().upper((Expression<String>) expression2);
            }
            return new BiComparableContext(field, expression1, expression2);
        }

    }

    static record MultiValueContext(Expression<? extends Comparable> field, Collection<?> fieldValues) {

        public static <FROM> MultiValueContext from(QueryContext ctx, CriteriaCondition criteria, boolean ignoreCase) {
            Element element = (Element) criteria.element();
            Expression<? extends Comparable> field = ctx.root().get(getName(element));
            if (ignoreCase) {
                BaseQueryParser.requireComparisonSupportsIgnoreCase(criteria, field);
                field = ctx.builder().upper((Expression<String>) field);
            }

            final var expressions = elementCollection(criteria)
                    .stream()
                    .map((Object elem) -> {
                        if (elem instanceof Value value) {
                            elem = value.get();
                        }
                        if (ignoreCase) {
                            return toValueInUpperCase(criteria, elem);
                        } else {
                            return elem;
                        }
                    })
                    .toList();
            return new MultiValueContext(field, expressions);
        }
    }
}