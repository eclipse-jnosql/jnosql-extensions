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

/**
 * The {@code pagination} member of a generated search request body. {@code offset} and {@code includeTotal} apply
 * to {@code Page} results, {@code cursor} to {@code CursoredPage} results; {@code size} to both.
 *
 * @param offset       the zero-based page offset
 * @param size         the page size
 * @param includeTotal whether totals are requested (offset pagination only)
 * @param cursor       the opaque continuation token (cursor pagination only)
 */
public record PaginationRequest(Long offset, Integer size, Boolean includeTotal, String cursor) {
}
