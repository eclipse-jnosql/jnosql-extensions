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
package org.eclipse.jnosql.rest.tests;

import jakarta.data.page.PageRequest;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.jnosql.rest.pagination.CursorCodec;
import org.eclipse.jnosql.rest.pagination.Pagination;
import org.eclipse.jnosql.rest.pagination.QueryValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cursor limits, query context and value parsing")
class CursorLimitsRuntimeTest {

    private static PageRequest after(Object... keys) {
        return PageRequest.afterCursor(PageRequest.Cursor.forKey(keys), 1, 20, false);
    }

    @Test
    @DisplayName("Should refuse to issue a token the decoder would reject")
    void shouldRefuseUnusableTokens() {
        assertThatThrownBy(() -> CursorCodec.encode(after("x".repeat(600), 1L), "c"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CursorCodec.encode(after(1, 2, 3, 4, 5, 6, 7, 8, 9), "c"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should round trip a token at the limits")
    void shouldRoundTripAtTheLimits() {
        String token = CursorCodec.encode(after(1, 2, 3, 4, 5, 6, 7, 8), "c");

        assertThat(token.length()).isLessThanOrEqualTo(CursorCodec.MAX_TOKEN_LENGTH);
        assertThat(CursorCodec.decode(token, "c").cursor().elements()).hasSize(8);
    }

    @Test
    @DisplayName("Should keep argument boundaries and types in the query context")
    void shouldDisambiguateContext() {
        assertThat(Pagination.context("m", "a|b", "c")).isNotEqualTo(Pagination.context("m", "a", "b|c"));
        assertThat(Pagination.context("m", 1L)).isNotEqualTo(Pagination.context("m", "1"));
        assertThat(Pagination.context("m", (Object) null)).isNotEqualTo(Pagination.context("m", "null"));
        assertThat(Pagination.context("m", "a", "b")).isEqualTo(Pagination.context("m", "a", "b"));

        String token = CursorCodec.encode(after("k"), Pagination.context("m", "a|b", "c"));
        assertThatThrownBy(() -> CursorCodec.decode(token, Pagination.context("m", "a", "b|c")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Should parse values and answer 400 for invalid ones")
    void shouldParseValues() {
        LocalDate parsed = QueryValues.parse("day", "2026-01-02", LocalDate::parse);
        LocalDate missing = QueryValues.parse("day", null, LocalDate::parse);
        assertThat(parsed).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(missing).isNull();
        assertThatThrownBy(() -> QueryValues.parse("day", "nope", LocalDate::parse))
                .isInstanceOf(BadRequestException.class);
    }
}
