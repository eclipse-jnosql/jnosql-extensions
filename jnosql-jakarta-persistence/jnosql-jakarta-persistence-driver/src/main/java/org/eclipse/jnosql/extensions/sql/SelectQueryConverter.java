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
import java.util.Objects;


/**
 * Converts {@link SelectQuery} instances into Jakarta Persistence {@link TypedQuery}
 * representations using the Criteria API.
 *
 * <p>This converter acts as a bridge between the JNoSQL semi-structured query model
 * and relational persistence provided by Jakarta Persistence. It translates
 * {@link SelectQuery} definitions into executable queries while preserving
 * filtering, sorting, and pagination semantics.</p>
 *
 * <p>The converter supports three main query types:</p>
 * <ul>
 *     <li><b>Selection queries</b> via {@link #convert(SelectQuery)} returning entities or projections</li>
 *     <li><b>Count queries</b> via {@link #convertCount(SelectQuery)} returning aggregated results</li>
 *     <li><b>Existence queries</b> via {@link #convertExists(SelectQuery)} optimized for short-circuit evaluation</li>
 * </ul>
 *
 * <p>Each method builds a {@link TypedQuery} but does not execute it. Execution
 * is delegated to higher-level components such as {@code SqlTemplate}.</p>
 *
 * <p><b>Important:</b> Not all aspects of a {@link SelectQuery} are applicable to all query types.
 * For example, sorting and pagination are ignored in count and existence queries.</p>
 *
 */
public final class SelectQueryConverter extends QueryConverterSupport {

    public SelectQueryConverter(EntityManager manager) {
        super(manager);
    }

    /**
     * Converts {@link SelectQuery} instances into Jakarta Persistence {@link TypedQuery}
     * representations using the Criteria API.
     *
     * <p>This converter acts as a bridge between the JNoSQL semi-structured query model
     * and relational persistence provided by Jakarta Persistence. It translates
     * {@link SelectQuery} definitions into executable queries while preserving
     * filtering, sorting, and pagination semantics.</p>
     *
     * <p>The converter supports three main query types:</p>
     * <ul>
     *     <li><b>Selection queries</b> via {@link #convert(SelectQuery)} returning entities or projections</li>
     *     <li><b>Count queries</b> via {@link #convertCount(SelectQuery)} returning aggregated results</li>
     *     <li><b>Existence queries</b> via {@link #convertExists(SelectQuery)} optimized for short-circuit evaluation</li>
     * </ul>
     *
     * <p>Each method builds a {@link TypedQuery} but does not execute it. Execution
     * is delegated to higher-level components such as {@code SqlTemplate}.</p>
     *
     * <p><b>Important:</b> Not all aspects of a {@link SelectQuery} are applicable to all query types.
     * For example, sorting and pagination are ignored in count and existence queries.</p>
     *
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public <T> TypedQuery<T> convert(SelectQuery query) {
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

    @SuppressWarnings("unchecked")
    <T>TypedQuery<Long> convertCount(SelectQuery query) {
        Objects.requireNonNull(query, "query is null");

        Class<T> type = resolveEntity(query.name());

        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        Root<T> root = countQuery.from(type);

        countQuery.select(cb.count(root));

        applyCondition(
                query.condition().orElse(null),
                cb,
                root,
                countQuery
        );

        return manager.createQuery(countQuery);
    }

    TypedQuery<Integer> convertExists(SelectQuery query) {
        Objects.requireNonNull(query, "query is null");

        Class<?> type = resolveEntity(query.name());

        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Integer> existsQuery = cb.createQuery(Integer.class);

        Root<?> root = existsQuery.from(type);

        existsQuery.select(cb.literal(1));

        applyCondition(
                query.condition().orElse(null),
                cb,
                root,
                existsQuery
        );

        TypedQuery<Integer> typedQuery = manager.createQuery(existsQuery);
        typedQuery.setMaxResults(1);

        return typedQuery;
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

}
