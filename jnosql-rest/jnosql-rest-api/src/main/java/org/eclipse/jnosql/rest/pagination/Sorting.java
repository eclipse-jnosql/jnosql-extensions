/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest.pagination;

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.ws.rs.BadRequestException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds the Jakarta Data {@link Sort} and {@link Order} of generated resources from validated HTTP input.
 *
 * <p>The syntax is a comma-separated list of {@code attribute:direction} terms, such as
 * {@code name:asc,price:desc}; the direction is {@code asc} or {@code desc} and defaults to {@code asc}. Only the
 * attributes the generator computed at compile time are accepted, so clients cannot sort by anything the REST
 * representation does not expose. Sorting itself is always delegated to the repository.
 */
public final class Sorting {

    private Sorting() {
    }

    /**
     * Builds an order of any number of criteria; absent input yields an empty order.
     *
     * @param value   the {@code sort} query parameter
     * @param allowed the sortable attributes
     * @param <T>     the entity type
     * @return the order
     * @throws BadRequestException if the input is invalid
     */
    public static <T> Order<T> order(String value, Set<String> allowed) {
        return order(parse(value), allowed, null);
    }

    /**
     * Builds an order that must include a unique attribute, as cursor pagination needs a total ordering.
     *
     * @param value   the {@code sort} query parameter
     * @param allowed the sortable attributes
     * @param unique  the attribute the order must contain
     * @param <T>     the entity type
     * @return the order
     * @throws BadRequestException if the input is invalid or lacks the unique attribute
     */
    public static <T> Order<T> order(String value, Set<String> allowed, String unique) {
        return order(parse(value), allowed, unique);
    }

    /**
     * Builds an order from the {@code sort} member of a search body.
     *
     * @param criteria the criteria, possibly {@code null}
     * @param allowed  the sortable attributes
     * @param unique   the attribute the order must contain, or {@code null}
     * @param <T>      the entity type
     * @return the order
     * @throws BadRequestException if the input is invalid
     */
    public static <T> Order<T> order(List<SortCriterion> criteria, Set<String> allowed, String unique) {
        List<Sort<T>> sorts = new ArrayList<>();
        Set<String> attributes = new LinkedHashSet<>();
        for (SortCriterion criterion : criteria == null ? List.<SortCriterion>of() : criteria) {
            sorts.add(sort(criterion, allowed, attributes));
        }
        if (unique != null && !attributes.contains(unique)) {
            throw new BadRequestException("Cursor pagination needs a total ordering: the sort must include '"
                    + unique + "'");
        }
        return Order.by(sorts);
    }

    /**
     * Builds one sort criterion; exactly one is required.
     *
     * @param value   the {@code sort} query parameter
     * @param allowed the sortable attributes
     * @param <T>     the entity type
     * @return the sort
     * @throws BadRequestException if the input is invalid or holds more or fewer than one criterion
     */
    public static <T> Sort<T> sort(String value, Set<String> allowed) {
        return single(parse(value), allowed);
    }

    /**
     * Builds one sort criterion from the {@code sort} member of a search body.
     *
     * @param criteria the criteria, possibly {@code null}
     * @param allowed  the sortable attributes
     * @param <T>      the entity type
     * @return the sort
     * @throws BadRequestException if the input is invalid or holds more or fewer than one criterion
     */
    public static <T> Sort<T> sort(List<SortCriterion> criteria, Set<String> allowed) {
        return single(criteria == null ? List.of() : criteria, allowed);
    }

    private static <T> Sort<T> single(List<SortCriterion> criteria, Set<String> allowed) {
        if (criteria.size() != 1) {
            throw new BadRequestException("Exactly one sort criterion is required");
        }
        return sort(criteria.get(0), allowed, new LinkedHashSet<>());
    }

    private static <T> Sort<T> sort(SortCriterion criterion, Set<String> allowed, Set<String> seen) {
        if (criterion == null || criterion.attribute() == null || !allowed.contains(criterion.attribute())) {
            throw new BadRequestException("Cannot sort by '" + (criterion == null ? null : criterion.attribute())
                    + "'; sortable attributes are " + allowed);
        }
        if (!seen.add(criterion.attribute())) {
            throw new BadRequestException("'" + criterion.attribute() + "' is sorted more than once");
        }
        String direction = criterion.direction() == null ? "asc" : criterion.direction().toLowerCase(Locale.ROOT);
        return switch (direction) {
            case "asc" -> Sort.asc(criterion.attribute());
            case "desc" -> Sort.desc(criterion.attribute());
            default -> throw new BadRequestException("The sort direction '" + criterion.direction()
                    + "' is invalid; use asc or desc");
        };
    }

    private static List<SortCriterion> parse(String value) {
        List<SortCriterion> criteria = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return criteria;
        }
        for (String term : value.split(",", -1)) {
            String[] parts = term.trim().split(":", -1);
            if (parts.length > 2 || parts[0].isBlank() || parts.length == 2 && parts[1].isBlank()) {
                throw new BadRequestException("The sort term '" + term + "' is invalid; use attribute:direction");
            }
            criteria.add(new SortCriterion(parts[0].trim(), parts.length == 2 ? parts[1].trim() : null));
        }
        return criteria;
    }
}
