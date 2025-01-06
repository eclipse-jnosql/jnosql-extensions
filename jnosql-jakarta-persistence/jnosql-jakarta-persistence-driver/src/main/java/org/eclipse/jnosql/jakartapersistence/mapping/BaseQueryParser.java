/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

import java.util.Collection;
import java.util.Iterator;

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
import static org.eclipse.jnosql.jakartapersistence.mapping.BaseQueryParser.elementCollection;
import static org.eclipse.jnosql.jakartapersistence.mapping.BaseQueryParser.getName;

class BaseQueryParser {

    protected final PersistenceDatabaseManager manager;

    protected BaseQueryParser(PersistenceDatabaseManager manager) {
        this.manager = manager;
    }

    protected <T> EntityType<T> findEntityType(String entityName) {
        return manager.findEntityType(entityName);
    }

    protected EntityManager entityManager() {
        return manager.getEntityManager();
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
                    Iterator<?> iterator = elementCollection(criteria).iterator();
                    yield ctx.builder().and(parseCriteria(iterator.next(), ctx), parseCriteria(iterator.next(), ctx));
                }
                case LESSER_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx.root(), criteria);
                    yield ctx.builder().lessThan(comparableContext.field(), comparableContext.fieldValue());
                }
                case LESSER_EQUALS_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx.root(), criteria);
                    yield ctx.builder().lessThanOrEqualTo(comparableContext.field(), comparableContext.fieldValue());
                }
                case GREATER_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx.root(), criteria);
                    yield ctx.builder().greaterThan(comparableContext.field(), comparableContext.fieldValue());
                }
                case GREATER_EQUALS_THAN -> {
                    ComparableContext comparableContext = ComparableContext.from(ctx.root(), criteria);
                    yield ctx.builder().greaterThanOrEqualTo(comparableContext.field(), comparableContext.fieldValue());
                }
                case BETWEEN -> {
                    BiComparableContext comparableContext = BiComparableContext.from(ctx.root(), criteria);
                    yield ctx.builder().between(comparableContext.field(), comparableContext.fieldValue1(), comparableContext.fieldValue2());
                }
                case IN -> {
                    MultiValueContext valueContext = MultiValueContext.from(ctx.root(), criteria);
                    CriteriaBuilder.In<Object> inExpr = ctx.builder().in(valueContext.field());
                    valueContext.fieldValues().forEach(v -> inExpr.value(v));
                    yield inExpr;
                }

                default ->
                    throw new UnsupportedOperationException("Not supported yet.");
            };
        } else if (value instanceof Element element) {
            return parseCriteria(element.value().get(), ctx);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected static String getName(Element element) {
        String name = element.name();
        // NoSQL DBs translate id field into "_id" but we don't want it
        return name.equals("_id") ? "id" : name;
    }

    protected static Collection<?> elementCollection(CriteriaCondition criteria) {
        Element element = (Element) criteria.element();
        return (Collection<?>) element.value().get();
    }

}

record QueryContext<FROM>(Root<FROM> root, CriteriaBuilder builder) {
}

record ComparableContext(Path<Comparable> field, Comparable fieldValue) {

    public static <FROM> ComparableContext from(Root<FROM> root, CriteriaCondition criteria) {
        Element element = (Element) criteria.element();
        Path<Comparable> field = root.get(getName(element));
        Comparable fieldValue = element.value().get(Comparable.class);
        return new ComparableContext(field, fieldValue);
    }
}

record BiComparableContext(Path<Comparable> field, Comparable fieldValue1, Comparable fieldValue2) {

    public static <FROM> BiComparableContext from(Root<FROM> root, CriteriaCondition criteria) {
        Element element = (Element) criteria.element();
        final Path<Comparable> field = root.get(getName(element));
        Iterator<?> iterator = elementCollection(criteria).iterator();
        final Comparable fieldValue1 = ((Value) iterator.next()).get(Comparable.class);
        final Comparable fieldValue2 = ((Value) iterator.next()).get(Comparable.class);
        return new BiComparableContext(field, fieldValue1, fieldValue2);
    }

}

record MultiValueContext(Path<?> field, Collection<?> fieldValues) {

    public static <FROM> MultiValueContext from(Root<FROM> root, CriteriaCondition criteria) {
        Element element = (Element) criteria.element();
        Path<Comparable> field = root.get(getName(element));
        return new MultiValueContext(field, elementCollection(criteria));
    }
}
