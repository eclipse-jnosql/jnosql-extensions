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

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;

import java.util.List;
import java.util.function.Function;

/**
 * HTTP representation of an offset-based Jakarta Data {@link Page}.
 *
 * @param content       the elements of the page
 * @param offset        the zero-based page offset (0 is the first page), not a row offset
 * @param size          the maximum number of elements per page
 * @param hasNext       whether another page follows
 * @param hasPrevious   whether a page precedes
 * @param totalElements the total number of elements, or {@code null} when totals were not requested
 * @param totalPages    the total number of pages, or {@code null} when totals were not requested
 * @param <T>           the element type
 */
public record PageResponse<T>(List<T> content,
                              long offset,
                              int size,
                              boolean hasNext,
                              boolean hasPrevious,
                              Long totalElements,
                              Long totalPages) {

    /**
     * Adapts a page whose elements are already the representation.
     *
     * @param page the page returned by the repository
     * @param <T>  the element type
     * @return the response
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return of(page, Function.identity());
    }

    /**
     * Adapts a page, converting its content, for instance into DTOs. The totals are read only when the page
     * reports them, so no count is triggered when they were not requested.
     *
     * @param page   the page returned by the repository
     * @param mapper converts the content of the page
     * @param <T>    the element type of the page
     * @param <R>    the element type of the response
     * @return the response
     */
    public static <T, R> PageResponse<R> of(Page<T> page, Function<List<T>, List<R>> mapper) {
        PageRequest request = page.pageRequest();
        boolean totals = page.hasTotals();
        return new PageResponse<>(mapper.apply(page.content()), request.page() - 1, request.size(),
                page.hasNext(), page.hasPrevious(),
                totals ? page.totalElements() : null, totals ? page.totalPages() : null);
    }
}
