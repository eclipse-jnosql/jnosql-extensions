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
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import jakarta.data.page.impl.PageRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.util.ArrayList;
import java.util.List;

final class SelectQueryConverter extends QueryConverterSupport{

    private static final List<String> RESERVED_PROPERTIES = List.of("_AND", "_OR", "_NOT");

    SelectQueryConverter(EntityManager manager) {
        super(manager);
    }

    @SuppressWarnings("unchecked")
    <T> TypedQuery<T> convert(SelectQuery query) {
        Class<T> type = resolveEntity(query.name());

        CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);

        Root<T> root = criteriaQuery.from(type);

        applyColumns(query.columns(), root, criteriaQuery);
        applyCondition(query.condition().orElse(null), criteriaBuilder, root, criteriaQuery);
        applySort(query.sorts(), criteriaBuilder, root, criteriaQuery);
        long limit = query.limit();
        TypedQuery<T> typedQuery = manager.createQuery(criteriaQuery);
        applySkip(query.skip(), typedQuery);
        applyLimit(limit, typedQuery);
        return typedQuery;
    }

    private static <T> void applyLimit(long limit, TypedQuery<T> typedQuery) {
        if (limit > 0) {
            typedQuery.setMaxResults((int) limit);
        }
    }

    private static <T> void applySkip(long skip, TypedQuery<T> typedQuery) {
        if (skip > 0) {
            typedQuery.setFirstResult((int) skip);
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

        Selection<?>[] selections = columns.stream()
                .map(column -> resolvePath(root, column))
                .toArray(Selection[]::new);

        criteriaQuery.multiselect(selections);
    }

    <T> PageRecord<T> executePagination(SelectQuery query, PageRequest pageRequest, DefaultSqlTemplate template) {
        var typedQuery = this.<T>convert(query);
        int size = pageRequest.size();
        long page = pageRequest.page();
        int offset = Math.toIntExact((page - 1) * size);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(size + 1);
        List<T> results = typedQuery.getResultList();
        boolean hasNext = results.size() > size;
        List<T> content = hasNext
                ? results.subList(0, size)
                : results;
        long totalElements = -1;
        if (pageRequest.requestTotal()) {
            totalElements = template.count(query);
        }
        return new PageRecord<>(pageRequest, content, totalElements, hasNext);
    }

    <T> CursoredPageRecord<T> executeQueryWithPagination(SelectQuery query, PageRequest pageRequest) {
        if (query.sorts().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cursor pagination requires at least one sort field");
        }

        int size = pageRequest.size();

        CriteriaCondition cursorCondition = null;

        if (pageRequest.mode() != PageRequest.Mode.OFFSET) {
            var cursor = pageRequest.cursor().orElseThrow();
            cursorCondition =
                    SelectQueryConverter.buildCursorCondition(query, cursor, pageRequest.mode());
        }

        SelectQuery effectiveQuery =
                SelectQueryConverter.updateQuery(size + 1, query, cursorCondition);

        var typedQuery = this.<T>convert(effectiveQuery);

        // Important: apply the same limit on the JPA query
        typedQuery.setMaxResults(size + 1);

        List<T> results = typedQuery.getResultList();

        if (results.isEmpty()) {
            return new CursoredPageRecord<>(results, List.of(), -1, pageRequest, null, null);
        }

        boolean hasNext = results.size() > size;

        List<T> content = hasNext
                ? results.subList(0, size)
                : results;

        T last = content.getLast();
        PageRequest.Cursor nextCursor = SelectQueryConverter.buildCursor(query.sorts(), last);
        PageRequest next = null;
        PageRequest previous = null;
        if (!content.isEmpty()) {
            next = PageRequest.ofSize(size).afterCursor(nextCursor);
            if (pageRequest.mode() != PageRequest.Mode.OFFSET) {
                previous = PageRequest.ofSize(size).beforeCursor(nextCursor);
            }
        }
        return new CursoredPageRecord<>(
                content,
                List.of(nextCursor),
                -1,
                pageRequest,
                next,
                previous
        );
    }

    static CriteriaCondition buildCursorCondition(
            SelectQuery query,
            PageRequest.Cursor cursor,
            PageRequest.Mode mode) {

        List<Sort<?>> sorts = query.sorts();

        if (cursor.size() != sorts.size()) {
            throw new IllegalArgumentException(
                    "The cursor size is different from the sort size. Cursor: "
                            + cursor.size() + " Sort: " + sorts.size());
        }

        CriteriaCondition condition = null;
        CriteriaCondition previousCondition = null;

        for (int index = 0; index < sorts.size(); index++) {

            Sort<?> sort = sorts.get(index);
            Object key = cursor.get(index);

            boolean ascending = sort.isAscending();
            boolean nextPage = mode == PageRequest.Mode.CURSOR_NEXT;

            boolean useGreater =
                    (ascending && nextPage) ||
                            (!ascending && !nextPage);

            CriteriaCondition comparison =
                    useGreater
                            ? CriteriaCondition.gt(sort.property(), key)
                            : CriteriaCondition.lt(sort.property(), key);

            if (condition == null) {

                condition = comparison;
                previousCondition = CriteriaCondition.eq(sort.property(), key);

            } else {

                condition = condition.or(
                        previousCondition.and(comparison)
                );

                previousCondition = previousCondition.and(
                        CriteriaCondition.eq(sort.property(), key)
                );
            }
        }

        return condition;
    }

    static SelectQuery updateQuery(
            int limit,
            SelectQuery query,
            CriteriaCondition condition) {

        SelectQuery.QueryBuilder builder = query.columns().isEmpty()
                ? SelectQuery.builder()
                : SelectQuery.builder(query.columns().toArray(String[]::new));

        builder.from(query.name());

        query.sorts().forEach(builder::sort);

        if (condition != null) {
            CriteriaCondition merged = query.condition()
                    .map(existing -> CriteriaCondition.and(existing, condition))
                    .orElse(condition);
            builder.where(merged);
        } else {
            query.condition().ifPresent(builder::where);
        }

        builder.limit(limit);

        return builder.build();
    }

    static PageRequest.Cursor buildCursor(List<Sort<?>> sorts, Object entity) {
        List<Object> keys = new ArrayList<>(sorts.size());
        for (Sort<?> sort : sorts) {
            keys.add(readProperty(entity, sort.property()));
        }
        return PageRequest.Cursor.forKey(keys.toArray());
    }

}
