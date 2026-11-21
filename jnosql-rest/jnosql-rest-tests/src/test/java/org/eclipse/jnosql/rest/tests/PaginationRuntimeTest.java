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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pagination runtime support")
class PaginationRuntimeTest {

    private static final String CONTEXT = Pagination.context("findByName", "ana");

    private static String token(boolean previous, Object... keys) {
        PageRequest request = previous
                ? PageRequest.beforeCursor(PageRequest.Cursor.forKey(keys), 1, 10, false)
                : PageRequest.afterCursor(PageRequest.Cursor.forKey(keys), 1, 10, false);
        return CursorCodec.encode(request, CONTEXT);
    }

    private static String wrap(String text) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String unwrap(String token) {
        return new String(java.util.Base64.getUrlDecoder().decode(token), java.nio.charset.StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("Offset requests")
    class Offset {

        @Test
        @DisplayName("Should default to the first page of 20 without totals")
        void shouldDefault() {
            PageRequest request = Pagination.offset((String) null, null, null);

            assertThat(request.page()).isEqualTo(1);
            assertThat(request.size()).isEqualTo(Pagination.DEFAULT_SIZE);
            assertThat(request.requestTotal()).isFalse();
        }

        @Test
        @DisplayName("Should translate the zero-based page offset to a Jakarta Data page number")
        void shouldTranslateOffset() {
            PageRequest request = Pagination.offset("2", "50", "true");

            assertThat(request.page()).isEqualTo(3);
            assertThat(request.size()).isEqualTo(50);
            assertThat(request.requestTotal()).isTrue();
        }

        @Test
        @DisplayName("Should accept the maximum page size")
        void shouldAcceptMaximum() {
            assertThat(Pagination.offset("0", "100", null).size()).isEqualTo(Pagination.MAX_SIZE);
        }

        @ParameterizedTest(name = "offset={0}")
        @ValueSource(strings = {"-1", "abc", "1.5", "", "9223372036854775807", "99999999999999999999"})
        @DisplayName("Should reject invalid offsets")
        void shouldRejectOffsets(String offset) {
            assertThatThrownBy(() -> Pagination.offset(offset, null, null)).isInstanceOf(BadRequestException.class);
        }

        @ParameterizedTest(name = "size={0}")
        @ValueSource(strings = {"0", "-5", "101", "abc", "2147483648", ""})
        @DisplayName("Should reject invalid sizes")
        void shouldRejectSizes(String size) {
            assertThatThrownBy(() -> Pagination.offset(null, size, null)).isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should reject a non-boolean includeTotal")
        void shouldRejectTotal() {
            assertThatThrownBy(() -> Pagination.offset(null, null, "yes")).isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("Cursor requests")
    class Cursor {

        @Test
        @DisplayName("Should start without a cursor and without totals")
        void shouldStart() {
            PageRequest request = Pagination.cursor((String) null, (String) null, CONTEXT);

            assertThat(request.cursor()).isEmpty();
            assertThat(request.size()).isEqualTo(Pagination.DEFAULT_SIZE);
            assertThat(request.requestTotal()).isFalse();
        }

        @Test
        @DisplayName("Should continue forward after a next token")
        void shouldContinueForward() {
            PageRequest request = Pagination.cursor(token(false, "a", 1L), "5", CONTEXT);

            assertThat(request.mode()).isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            assertThat((java.util.List<Object>) request.cursor().orElseThrow().elements()).containsExactly("a", 1L);
            assertThat(request.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should continue backward after a previous token")
        void shouldContinueBackward() {
            PageRequest request = Pagination.cursor(token(true, "a", 1L), (String) null, CONTEXT);

            assertThat(request.mode()).isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
        }

        @Test
        @DisplayName("Should reject a blank cursor and an invalid size")
        void shouldRejectInput() {
            assertThatThrownBy(() -> Pagination.cursor(" ", (String) null, CONTEXT))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> Pagination.cursor((String) null, "0", CONTEXT))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> Pagination.cursor((String) null, "101", CONTEXT))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should keep the same repository method and criteria in the context")
        void shouldBuildContext() {
            assertThat(Pagination.context("a", "x")).isEqualTo(Pagination.context("a", "x"))
                    .isNotEqualTo(Pagination.context("a", "y")).isNotEqualTo(Pagination.context("b", "x"));
        }
    }

    @Nested
    @DisplayName("Cursor codec")
    class Codec {

        @Test
        @DisplayName("Should round-trip every supported key type")
        void shouldRoundTrip() {
            Object[][] groups = {{"text", 1, 2L, (short) 3, (byte) 4, true, 1.5d, 2.5f},
                    {new BigDecimal("10.50"), BigInteger.TEN, UUID.randomUUID(), LocalDate.of(2026, 1, 2),
                            LocalDateTime.of(2026, 1, 2, 3, 4), LocalTime.of(5, 6),
                            Instant.parse("2026-01-02T03:04:05Z")}};

            for (Object[] keys : groups) {
                CursorCodec.Token decoded = CursorCodec.decode(token(false, keys), CONTEXT);

                assertThat((java.util.List<Object>) decoded.cursor().elements()).containsExactly(keys);
                assertThat(decoded.previous()).isFalse();
            }
        }

        @Test
        @DisplayName("Should preserve the navigation direction")
        void shouldPreserveDirection() {
            assertThat(CursorCodec.decode(token(true, "a"), CONTEXT).previous()).isTrue();
            assertThat(CursorCodec.decode(token(false, "a"), CONTEXT).previous()).isFalse();
        }

        @Test
        @DisplayName("Should be URL safe and deterministic")
        void shouldBeUrlSafe() {
            String token = token(false, "a/b+c=?", 1L);

            assertThat(token).matches("[A-Za-z0-9_\\-]+").isEqualTo(token(false, "a/b+c=?", 1L));
            assertThat(unwrap(token)).startsWith("v1;N;");
        }

        @Test
        @DisplayName("Should preserve text with separators and non-ASCII characters")
        void shouldPreserveSpecialText() {
            String value = "a;b~c é 日本";

            assertThat(CursorCodec.decode(token(false, value), CONTEXT).cursor().get(0)).isEqualTo(value);
        }

        @Test
        @DisplayName("Should reject a token issued for another query")
        void shouldRejectOtherContext() {
            assertThatThrownBy(() -> CursorCodec.decode(token(false, "a"), Pagination.context("findByName", "bob")))
                    .isInstanceOf(BadRequestException.class).hasMessageContaining("different query");
        }

        @Test
        @DisplayName("Should reject an unsupported version")
        void shouldRejectVersion() {
            String token = wrap(unwrap(token(false, "a")).replaceFirst("^v1", "v2"));

            assertThatThrownBy(() -> CursorCodec.decode(token, CONTEXT)).isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("version");
        }

        @ParameterizedTest(name = "[{index}] malformed")
        @ValueSource(strings = {"garbage", "v1", "v1;N", "v1;X;00000000;S~YQ", "v1;N;zz;S~YQ",
                "v1;N;00000000;", "v1;N;00000000;Q~YQ", "v1;N;00000000;S~***", "v1;N;00000000;I~YWJj",
                "v1;N;00000000;S~YQ;S~YQ;S~YQ;S~YQ;S~YQ;S~YQ;S~YQ;S~YQ;S~YQ"})
        @DisplayName("Should reject malformed tokens")
        void shouldRejectMalformed(String text) {
            String hash = unwrap(token(false, "a")).split(";")[2];
            String token = wrap(text.replace("00000000", hash));

            assertThatThrownBy(() -> CursorCodec.decode(token, CONTEXT)).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> CursorCodec.decode(text, CONTEXT)).isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should reject an oversized token")
        void shouldRejectOversized() {
            String token = wrap("v1;N;00000000;S~" + "A".repeat(CursorCodec.MAX_TOKEN_LENGTH));

            assertThatThrownBy(() -> CursorCodec.decode(token, CONTEXT)).isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should refuse to encode unsupported or null key types")
        void shouldRefuseUnsupportedKeys() {
            assertThatThrownBy(() -> token(false, new Object())).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> token(false, "a", null)).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should not depend on Java serialization")
        void shouldNotUseSerialization() throws Exception {
            String source = java.nio.file.Files.readString(java.nio.file.Path.of(
                    "../jnosql-rest-api/src/main/java/org/eclipse/jnosql/rest/pagination/CursorCodec.java"));

            assertThat(source).doesNotContain("ObjectInputStream").doesNotContain("ObjectOutputStream");
        }
    }
}
