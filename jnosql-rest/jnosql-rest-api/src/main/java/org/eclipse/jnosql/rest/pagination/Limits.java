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

import jakarta.data.Limit;
import jakarta.ws.rs.BadRequestException;

/**
 * Builds the Jakarta Data {@link Limit} of generated resources from validated HTTP input. The limit is always
 * bounded: it defaults to {@value #DEFAULT_LIMIT} results and never exceeds {@value #MAX_LIMIT}.
 */
public final class Limits {

    /** The limit used when the client sends none. */
    public static final int DEFAULT_LIMIT = Pagination.DEFAULT_SIZE;

    /** The largest limit accepted. */
    public static final int MAX_LIMIT = Pagination.MAX_SIZE;

    private Limits() {
    }

    /**
     * Builds a limit from query parameters.
     *
     * @param limit   the maximum number of results, {@value #DEFAULT_LIMIT} when absent
     * @param startAt the one-based position of the first result, {@code 1} when absent
     * @return the limit
     * @throws BadRequestException if a value is invalid
     */
    public static Limit of(String limit, String startAt) {
        int maxResults = limit == null ? DEFAULT_LIMIT : parse("limit", limit, Integer::parseInt).intValue();
        long start = startAt == null ? 1 : parse("startAt", startAt, Long::parseLong).longValue();
        if (maxResults < 1 || maxResults > MAX_LIMIT) {
            throw new BadRequestException("'limit' must be between 1 and " + MAX_LIMIT);
        }
        if (start < 1) {
            throw new BadRequestException("'startAt' must be one or greater");
        }
        return new Limit(maxResults, start);
    }

    private static <T extends Number> T parse(String name, String value,
                                              java.util.function.Function<String, T> parser) {
        try {
            return parser.apply(value);
        } catch (NumberFormatException exception) {
            throw new BadRequestException("'" + name + "' is not a valid number");
        }
    }
}
