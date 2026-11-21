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

import jakarta.data.page.PageRequest;
import jakarta.ws.rs.BadRequestException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Builds the Jakarta Data {@link PageRequest} for generated resources from validated HTTP input. It only translates
 * and validates; the repository provider decides everything about the page.
 * <ul>
 *     <li>The default page size is {@value #DEFAULT_SIZE} and the maximum is {@value #MAX_SIZE}.</li>
 *     <li>Offset pagination does not request totals unless {@code includeTotal=true}.</li>
 * </ul>
 */
public final class Pagination {

    /** The page size used when the client sends none. */
    public static final int DEFAULT_SIZE = 20;

    /** The largest page size accepted. */
    public static final int MAX_SIZE = 100;

    private Pagination() {
    }

    /**
     * Builds an offset request from query parameters.
     *
     * @param offset       the zero-based page offset, {@code 0} when absent
     * @param size         the page size, {@value #DEFAULT_SIZE} when absent
     * @param includeTotal {@code "true"} to request totals, absent or {@code "false"} otherwise
     * @return the request
     * @throws BadRequestException if a value is invalid
     */
    public static PageRequest offset(String offset, String size, String includeTotal) {
        Long page = number("offset", offset, Long::valueOf);
        Integer pageSize = number("size", size, Integer::valueOf);
        Boolean total = number("includeTotal", includeTotal, Pagination::flag);
        return offset(page, pageSize, total);
    }

    /**
     * Builds an offset request from typed values.
     *
     * @param offset       the zero-based page offset, {@code 0} when {@code null}
     * @param size         the page size, {@value #DEFAULT_SIZE} when {@code null}
     * @param includeTotal whether totals are requested, {@code false} when {@code null}
     * @return the request
     * @throws BadRequestException if a value is invalid
     */
    public static PageRequest offset(Long offset, Integer size, Boolean includeTotal) {
        long page = offset == null ? 0 : offset;
        if (page < 0 || page == Long.MAX_VALUE) {
            throw new BadRequestException("'offset' must be zero or positive");
        }
        return PageRequest.ofPage(page + 1, size(size), Boolean.TRUE.equals(includeTotal));
    }

    /**
     * Builds an offset request from the {@code pagination} member of a search body.
     *
     * @param pagination the member, possibly {@code null}
     * @return the request
     */
    public static PageRequest offset(PaginationRequest pagination) {
        return pagination == null ? offset((Long) null, null, null)
                : offset(pagination.offset(), pagination.size(), pagination.includeTotal());
    }

    /**
     * Builds a cursor request from query parameters.
     *
     * @param cursor  the continuation token, absent for the first page
     * @param size    the page size, {@value #DEFAULT_SIZE} when absent
     * @param context the query context the token must have been issued for
     * @return the request
     * @throws BadRequestException if a value or the token is invalid
     */
    public static PageRequest cursor(String cursor, String size, String context) {
        Integer pageSize = number("size", size, Integer::valueOf);
        return cursor(cursor, pageSize, context);
    }

    /**
     * Builds a cursor request from typed values.
     *
     * @param cursor  the continuation token, absent for the first page
     * @param size    the page size, {@value #DEFAULT_SIZE} when {@code null}
     * @param context the query context the token must have been issued for
     * @return the request
     * @throws BadRequestException if a value or the token is invalid
     */
    public static PageRequest cursor(String cursor, Integer size, String context) {
        int validated = size(size);
        if (cursor == null) {
            return PageRequest.ofSize(validated).withoutTotal();
        }
        if (cursor.isBlank()) {
            throw new BadRequestException("The cursor must not be blank");
        }
        CursorCodec.Token token = CursorCodec.decode(cursor, context);
        return token.previous() ? PageRequest.beforeCursor(token.cursor(), 1, validated, false)
                : PageRequest.afterCursor(token.cursor(), 1, validated, false);
    }

    /**
     * Builds a cursor request from the {@code pagination} member of a search body.
     *
     * @param pagination the member, possibly {@code null}
     * @param context    the query context the token must have been issued for
     * @return the request
     */
    public static PageRequest cursor(PaginationRequest pagination, String context) {
        return pagination == null ? cursor((String) null, (Integer) null, context)
                : cursor(pagination.cursor(), pagination.size(), context);
    }

    /**
     * Describes a query so continuation tokens cannot be replayed against a different one.
     *
     * @param operation the repository method name
     * @param values    the criteria values, in a deterministic order
     * @return the context
     */
    public static String context(String operation, Object... values) {
        return operation + "|" + Arrays.stream(values).map(String::valueOf).collect(Collectors.joining("|"));
    }

    private static int size(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BadRequestException("'size' must be between 1 and " + MAX_SIZE);
        }
        return size;
    }

    private static Boolean flag(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            throw new IllegalArgumentException(value);
        }
        return Boolean.valueOf(value);
    }

    private static <T> T number(String name, String value, java.util.function.Function<String, T> parser) {
        if (value == null) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("'" + name + "' is not a valid value");
        }
    }
}
