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

import jakarta.data.page.CursoredPage;

import java.util.List;
import java.util.function.Function;

/**
 * HTTP representation of a cursor-based Jakarta Data {@link CursoredPage}. It deliberately carries no totals and no
 * numeric page position: navigation happens only through the opaque continuation tokens.
 *
 * @param content        the elements of the page
 * @param size           the maximum number of elements per page
 * @param hasNext        whether another page follows
 * @param hasPrevious    whether a page precedes
 * @param nextCursor     the token of the next page, or {@code null}
 * @param previousCursor the token of the previous page, or {@code null}
 * @param <T>            the element type
 */
public record CursorPageResponse<T>(List<T> content,
                                    int size,
                                    boolean hasNext,
                                    boolean hasPrevious,
                                    String nextCursor,
                                    String previousCursor) {

    /**
     * Adapts a cursored page whose elements are already the representation.
     *
     * @param page    the page returned by the repository
     * @param context the query context the tokens are bound to
     * @param <T>     the element type
     * @return the response
     */
    public static <T> CursorPageResponse<T> of(CursoredPage<T> page, String context) {
        return of(page, context, Function.identity());
    }

    /**
     * Adapts a cursored page, converting its content. A navigation request is only asked from the page when the
     * corresponding {@code hasNext} or {@code hasPrevious} flag is set.
     *
     * @param page    the page returned by the repository
     * @param context the query context the tokens are bound to
     * @param mapper  converts the content of the page
     * @param <T>     the element type of the page
     * @param <R>     the element type of the response
     * @return the response
     */
    public static <T, R> CursorPageResponse<R> of(CursoredPage<T> page, String context,
                                                  Function<List<T>, List<R>> mapper) {
        boolean next = page.hasNext();
        boolean previous = page.hasPrevious();
        return new CursorPageResponse<>(mapper.apply(page.content()), page.pageRequest().size(), next, previous,
                next ? CursorCodec.encode(page.nextPageRequest(), context) : null,
                previous ? CursorCodec.encode(page.previousPageRequest(), context) : null);
    }
}
