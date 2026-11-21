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

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.jnosql.rest.pagination.Limits;
import org.eclipse.jnosql.rest.pagination.SortCriterion;
import org.eclipse.jnosql.rest.pagination.Sorting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Limit, Sort and Order runtime support")
class SortingRuntimeTest {

    private static final Set<String> ALLOWED = Set.of("name", "price", "id");

    @Nested
    @DisplayName("Limit")
    class LimitSupport {

        @Test
        @DisplayName("Should default to the first twenty results")
        void shouldDefault() {
            assertThat(Limits.of(null, null)).isEqualTo(Limit.of(20, 1));
        }

        @Test
        @DisplayName("Should accept a limit and a one-based start position")
        void shouldAccept() {
            assertThat(Limits.of("10", "5")).isEqualTo(Limit.of(10, 5));
            assertThat(Limits.of("100", null)).isEqualTo(Limit.of(100, 1));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "101", "x", "", "99999999999"})
        @DisplayName("Should reject an invalid limit")
        void shouldRejectLimit(String limit) {
            assertThatThrownBy(() -> Limits.of(limit, null)).isInstanceOf(BadRequestException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-3", "x", "", "99999999999999999999"})
        @DisplayName("Should reject an invalid start position")
        void shouldRejectStart(String startAt) {
            assertThatThrownBy(() -> Limits.of("10", startAt)).isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("Sort")
    class SortSupport {

        @Test
        @DisplayName("Should build ascending and descending sorts, defaulting to ascending")
        void shouldBuild() {
            assertThat(Sorting.<Object>sort("name:asc", ALLOWED)).isEqualTo(Sort.asc("name"));
            assertThat(Sorting.<Object>sort("price:DESC", ALLOWED)).isEqualTo(Sort.desc("price"));
            assertThat(Sorting.<Object>sort("name", ALLOWED)).isEqualTo(Sort.asc("name"));
        }

        @Test
        @DisplayName("Should reject more than one criterion and a missing one")
        void shouldRejectCount() {
            assertThatThrownBy(() -> Sorting.sort("name:asc,price:desc", ALLOWED))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> Sorting.sort((String) null, ALLOWED)).isInstanceOf(BadRequestException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"secret:asc", "name:up", "name:", ":asc", "name:asc:x", "address.city:asc", ","})
        @DisplayName("Should reject unknown attributes, directions and malformed terms")
        void shouldRejectInvalid(String value) {
            assertThatThrownBy(() -> Sorting.sort(value, ALLOWED)).isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("Order")
    class OrderSupport {

        @Test
        @DisplayName("Should keep the criteria order and mixed directions")
        void shouldKeepOrder() {
            Order<Object> order = Sorting.order("price:desc, name:asc", ALLOWED);

            assertThat(order.sorts()).containsExactly(Sort.desc("price"), Sort.asc("name"));
        }

        @Test
        @DisplayName("Should build an empty order when no sort is requested")
        void shouldBeEmpty() {
            assertThat(Sorting.<Object>order((String) null, ALLOWED).sorts()).isEmpty();
            assertThat(Sorting.<Object>order("", ALLOWED).sorts()).isEmpty();
        }

        @Test
        @DisplayName("Should reject repeated attributes and unknown attributes")
        void shouldReject() {
            assertThatThrownBy(() -> Sorting.order("name:asc,name:desc", ALLOWED))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> Sorting.order("name:asc,secret:desc", ALLOWED))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should require the unique attribute for a total ordering")
        void shouldRequireUnique() {
            assertThat(Sorting.<Object>order("name:asc,id:asc", ALLOWED, "id").sorts()).hasSize(2);
            assertThatThrownBy(() -> Sorting.order("name:asc", ALLOWED, "id"))
                    .isInstanceOf(BadRequestException.class).hasMessageContaining("total ordering");
            assertThatThrownBy(() -> Sorting.order((String) null, ALLOWED, "id"))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should build an order from the criteria of a search body")
        void shouldBuildFromBody() {
            Order<Object> order = Sorting.order(List.of(new SortCriterion("name", "desc"),
                    new SortCriterion("id", null)), ALLOWED, "id");

            assertThat(order.sorts()).containsExactly(Sort.desc("name"), Sort.asc("id"));
            assertThatThrownBy(() -> Sorting.order(List.of(new SortCriterion("secret", "asc")), ALLOWED, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> Sorting.order(List.of(new SortCriterion("name", "sideways")), ALLOWED, null))
                    .isInstanceOf(BadRequestException.class);
            assertThat(Sorting.<Object>order((List<SortCriterion>) null, ALLOWED, null).sorts()).isEmpty();
        }
    }
}
