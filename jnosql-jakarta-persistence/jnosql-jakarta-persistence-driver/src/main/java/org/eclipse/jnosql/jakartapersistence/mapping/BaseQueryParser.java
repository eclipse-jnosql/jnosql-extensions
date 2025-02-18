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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
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
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.GREATER_THAN;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.NOT;

abstract class BaseQueryParser {

    protected final PersistenceDatabaseManager manager;

    protected BaseQueryParser(PersistenceDatabaseManager manager) {
        this.manager = manager;
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

    public <T> Stream<T> query(String queryString) {
        return query(queryString, null, null);
    }

    public <T> Stream<T> query(String queryString, String entity) {
        return query(queryString, entity, null);
    }

    public abstract <T> Stream<T> query(String queryString, String entity, Consumer<Query> queryModifier);

    public Query buildQuery(String queryString) {
        return buildQuery(queryString, null);
    }

    public Query buildQuery(String queryString, String entity) {
        EntityManager em = entityManager();
        return em.createQuery(queryString);
    }

    protected static <FROM> Predicate parseCriteria(Object value, QueryContext<FROM> ctx) {
        if (value instanceof CriteriaCondition criteria) {
            return switch (criteria.condition()) {
                case NOT ->
                    ctx.builder().not(parseCriteria(criteria.element(), ctx));
                case EQUALS -> {
                    Element element = (Element) criteria.element();
                    if (element.value().isNull()) {
                        yield ctx.builder().isNull(ctx.root().get(getName(element)));
                    } else {
                        yield ctx.builder().equal(ctx.root().get(getName(element)), element.value().get());
                    }
                }
                case AND -> {
                    yield elementCollection(criteria).stream()
                    .map(elem -> parseCriteria(elem, ctx))
                    .reduce(ctx.builder()::and).get();
                }
                case LESSER_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx, criteria);
                    yield ctx.builder().lessThan(comparableContext.field(), comparableContext.expression());
                }
                case LESSER_EQUALS_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx, criteria);
                    yield ctx.builder().lessThanOrEqualTo(comparableContext.field(), comparableContext.expression());
                }
                case GREATER_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx, criteria);
                    yield ctx.builder().greaterThan(comparableContext.field(), comparableContext.expression());
                }
                case GREATER_EQUALS_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx, criteria);
                    yield ctx.builder().greaterThanOrEqualTo(comparableContext.field(), comparableContext.expression());
                }
                case BETWEEN -> {
                    BiComparableContext comparableContext = BiComparableContext.from(ctx, criteria);
                    yield ctx.builder().between(comparableContext.field(), comparableContext.expression1(), comparableContext.expression2());
                }
                case IN -> {
                    MultiValueContext valueContext = MultiValueContext.from(ctx, criteria);
                    CriteriaBuilder.In<Object> inExpr = ctx.builder().in(valueContext.field());
                    valueContext.fieldValues().forEach(v -> inExpr.value(v));
                    yield inExpr;
                }
                case LIKE -> {
                    StringContext stringContext = StringContext.from(ctx, criteria);
                    yield ctx.builder().like(stringContext.field(), stringContext.fieldValue());
                }

                default ->
                    throw new UnsupportedOperationException("JNoSQL criteria condition "
                            + criteria.condition() + " is not supported yet.");
            };
        } else if (value instanceof Element element) {
            return parseCriteria(element.value().get(), ctx);
        }
        throw new UnsupportedOperationException("Parsing value " + value + " is not supported yet.");
    }

    protected static String getName(Element element) {
        String name = element.name();
        return getFieldName(name);
    }

    protected static String getFieldName(String fieldName) {
        // NoSQL DBs translate id field into "_id" but we don't want it
        return fieldName.equals("_id") ? "id" : fieldName;
    }

    protected static Collection<?> elementCollection(CriteriaCondition criteria) {
        Element element = criteria.element();
        return (Collection<?>) element.value().get();
    }

    static Expression<? extends Comparable> getComparableValue(CriteriaBuilder cb, Object value) {
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

    static record QueryContext<FROM>(Root<FROM> root, CriteriaBuilder builder) {
    }

    static record ComparableContext(Path<Comparable> field, Expression<? extends Comparable> expression) {

        public static <FROM> ComparableContext from(QueryContext ctx, CriteriaCondition criteria) {
            Element element = (Element) criteria.element();
            Path<Comparable> field = ctx.root().get(getName(element));
            return new ComparableContext(field, getComparableValue(ctx.builder(), element.value()));
        }
    }

    static record StringContext(Path<String> field, String fieldValue) {

        public static <FROM> StringContext from(QueryContext ctx, CriteriaCondition criteria) {
            Element element = (Element) criteria.element();
            Path<String> field = ctx.root().get(getName(element));
            String fieldValue = element.value().get(String.class);
            return new StringContext(field, fieldValue);
        }
    }

    static record BiComparableContext(Path<Comparable> field, Expression<? extends Comparable> expression1, Expression<? extends Comparable> expression2) {

        public static <FROM> BiComparableContext from(QueryContext ctx, CriteriaCondition criteria) {
            Element element = criteria.element();
            final Path<Comparable> field = ctx.root().get(getName(element));
            Iterator<?> iterator = elementCollection(criteria).iterator();
            final Expression<? extends Comparable> expression1 = getComparableValue(ctx.builder(), iterator.next());
            final Expression<? extends Comparable> expression2 = getComparableValue(ctx.builder(), iterator.next());
            return new BiComparableContext(field, expression1, expression2);
        }

    }

    static record MultiValueContext(Path<?> field, Collection<?> fieldValues) {

        public static <FROM> MultiValueContext from(QueryContext ctx, CriteriaCondition criteria) {
            Element element = (Element) criteria.element();
            Path<Comparable> field = ctx.root().get(getName(element));
            final var expressions = elementCollection(criteria)
                    .stream()
                    .map(elem -> {
                        if (elem instanceof Value value) {
                            return value.get();
                        } else {
                            return elem;
                        }
                    })
                    .toList();
            return new MultiValueContext(field, expressions);
        }
    }
}