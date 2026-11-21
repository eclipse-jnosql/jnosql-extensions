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

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.jnosql.rest.pagination.CursorCodec;
import org.eclipse.jnosql.rest.pagination.CursorPageResponse;
import org.eclipse.jnosql.rest.pagination.PageResponse;
import org.eclipse.jnosql.rest.pagination.Pagination;
import org.eclipse.jnosql.rest.tests.basic.Customer;
import org.eclipse.jnosql.rest.tests.custom.Order;
import org.eclipse.jnosql.rest.tests.paging.CursorOrderRepository;
import org.eclipse.jnosql.rest.tests.paging.CursorOrderRepositoryResource;
import org.eclipse.jnosql.rest.tests.paging.OffsetCustomerRepository;
import org.eclipse.jnosql.rest.tests.paging.OffsetCustomerRepositoryResource;
import org.eclipse.jnosql.rest.tests.paging.OrderDTO;
import org.eclipse.jnosql.rest.tests.paging.OrderDTOMapperImpl;
import org.eclipse.jnosql.rest.tests.paging.PagedProductRepository;
import org.eclipse.jnosql.rest.tests.paging.PagedProductRepositoryResource;
import org.eclipse.jnosql.rest.tests.sample.Product;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@EnableWeld
@DisplayName("Generated pagination endpoints with CDI")
class PaginationCdiTest {

    private static final OffsetCustomerRepository CUSTOMERS = Mockito.mock(OffsetCustomerRepository.class);
    private static final CursorOrderRepository ORDERS = Mockito.mock(CursorOrderRepository.class);
    private static final PagedProductRepository PRODUCTS = Mockito.mock(PagedProductRepository.class);

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                    .addBeanClasses(OffsetCustomerRepositoryResource.class, CursorOrderRepositoryResource.class,
                            PagedProductRepositoryResource.class, OrderDTOMapperImpl.class))
            .addBeans(MockBean.<OffsetCustomerRepository>builder().types(OffsetCustomerRepository.class)
                    .scope(ApplicationScoped.class).creating(CUSTOMERS).build())
            .addBeans(MockBean.<CursorOrderRepository>builder().types(CursorOrderRepository.class)
                    .scope(ApplicationScoped.class).creating(ORDERS).build())
            .addBeans(MockBean.<PagedProductRepository>builder().types(PagedProductRepository.class)
                    .scope(ApplicationScoped.class).creating(PRODUCTS).build())
            .activate(RequestScoped.class)
            .build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(CUSTOMERS, ORDERS, PRODUCTS);
    }

    @SuppressWarnings("unchecked")
    private static <T> Page<T> page(PageRequest request, boolean next, boolean previous, boolean totals,
                                    List<T> content) {
        Page<T> page = Mockito.mock(Page.class);
        when(page.content()).thenReturn(content);
        when(page.pageRequest()).thenReturn(request);
        when(page.hasNext()).thenReturn(next);
        when(page.hasPrevious()).thenReturn(previous);
        when(page.hasTotals()).thenReturn(totals);
        if (totals) {
            when(page.totalElements()).thenReturn(145L);
            when(page.totalPages()).thenReturn(8L);
        }
        return page;
    }

    @SuppressWarnings("unchecked")
    private static <T> CursoredPage<T> cursored(PageRequest request, List<T> content, PageRequest next,
                                                PageRequest previous) {
        CursoredPage<T> page = Mockito.mock(CursoredPage.class);
        when(page.content()).thenReturn(content);
        when(page.pageRequest()).thenReturn(request);
        when(page.hasNext()).thenReturn(next != null);
        when(page.hasPrevious()).thenReturn(previous != null);
        if (next != null) {
            when(page.nextPageRequest()).thenReturn(next);
        }
        if (previous != null) {
            when(page.previousPageRequest()).thenReturn(previous);
        }
        return page;
    }

    private static PageRequest after(Object... keys) {
        return PageRequest.afterCursor(PageRequest.Cursor.forKey(keys), 1, 20, false);
    }

    private static PageRequest before(Object... keys) {
        return PageRequest.beforeCursor(PageRequest.Cursor.forKey(keys), 1, 20, false);
    }

    @Nested
    @DisplayName("Offset pagination on a BasicRepository")
    class OffsetPagination {

        private OffsetCustomerRepositoryResource resource() {
            return weld.select(OffsetCustomerRepositoryResource.class).get();
        }

        @Test
        @DisplayName("Should use defaults, request no totals and return the first page")
        void shouldReturnFirstPage() {
            Customer customer = new Customer();
            when(CUSTOMERS.findByName(any(), any())).thenAnswer(invocation -> page(PageRequest.ofPage(1, 20, false), true, false, false, List.of(customer)));

            PageResponse<Customer> response = resource().findByName("ana", null, null, null);

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            verify(CUSTOMERS, times(1)).findByName(Mockito.eq("ana"), request.capture());
            assertThat(request.getValue().page()).isEqualTo(1);
            assertThat(request.getValue().size()).isEqualTo(20);
            assertThat(request.getValue().requestTotal()).isFalse();
            assertThat(response.content()).containsExactly(customer);
            assertThat(response.offset()).isZero();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.hasPrevious()).isFalse();
            assertThat(response.totalElements()).isNull();
            assertThat(response.totalPages()).isNull();
            verifyNoMoreInteractions(CUSTOMERS);
        }

        @Test
        @DisplayName("Should not read totals when the page does not carry them")
        void shouldNotReadTotals() {
            Page<Customer> page = page(PageRequest.ofPage(1, 20, false), false, false, false, List.of());
            when(CUSTOMERS.findByName(any(), any())).thenReturn(page);

            resource().findByName("ana", null, null, null);

            verify(page, never()).totalElements();
            verify(page, never()).totalPages();
        }

        @Test
        @DisplayName("Should pass a zero-based page offset and size, and return totals when requested")
        void shouldReturnMiddlePageWithTotals() {
            when(CUSTOMERS.findByName(any(), any())).thenAnswer(invocation -> page(PageRequest.ofPage(3, 10, true), true, true, true, List.of(new Customer())));

            PageResponse<Customer> response = resource().findByName("ana", "10", "2", "true");

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            verify(CUSTOMERS).findByName(Mockito.eq("ana"), request.capture());
            assertThat(request.getValue().page()).isEqualTo(3);
            assertThat(request.getValue().size()).isEqualTo(10);
            assertThat(request.getValue().requestTotal()).isTrue();
            assertThat(response.offset()).isEqualTo(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.hasPrevious()).isTrue();
            assertThat(response.totalElements()).isEqualTo(145);
            assertThat(response.totalPages()).isEqualTo(8);
        }

        @Test
        @DisplayName("Should flag the last page and an empty page")
        void shouldReturnLastAndEmptyPages() {
            Page<Customer> lastPage = page(PageRequest.ofPage(8, 20, true), false, true, true,
                    List.of(new Customer()));
            Page<Customer> emptyPage = page(PageRequest.ofPage(1, 20, true), false, false, true, List.of());
            when(CUSTOMERS.findByName(any(), any())).thenReturn(lastPage, emptyPage);

            PageResponse<Customer> last = resource().findByName("ana", null, "7", "true");
            PageResponse<Customer> empty = resource().findByName("zed", null, null, "true");

            assertThat(last.hasNext()).isFalse();
            assertThat(last.hasPrevious()).isTrue();
            assertThat(empty.content()).isEmpty();
            assertThat(empty.hasNext()).isFalse();
        }

        @Test
        @DisplayName("Should reject invalid pagination before touching the repository")
        void shouldRejectInvalidInput() {
            OffsetCustomerRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.findByName("ana", null, "-1", null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findByName("ana", "0", null, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findByName("ana", "101", null, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findByName("ana", "x", null, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findByName(null, null, null, null))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(CUSTOMERS);
        }

        @Test
        @DisplayName("Should keep exposing the standard BasicRepository operations")
        void shouldKeepBasicOperations() {
            assertThat(java.util.Arrays.stream(OffsetCustomerRepositoryResource.class.getDeclaredMethods())
                    .map(java.lang.reflect.Method::getName)).contains("findByName", "findById", "save");
        }
    }

    @Nested
    @DisplayName("Cursor pagination on a custom repository")
    class CursorPagination {

        private static final UUID CUSTOMER = UUID.randomUUID();

        private CursorOrderRepositoryResource resource() {
            return weld.select(CursorOrderRepositoryResource.class).get();
        }

        private String context() {
            return Pagination.context("findByCustomerId", CUSTOMER);
        }

        @Test
        @DisplayName("Should start without a cursor and issue opaque next and previous tokens")
        void shouldIssueTokens() {
            Order order = new Order();
            order.setId(UUID.randomUUID());
            order.setStatus("open");
            order.setCustomerId(CUSTOMER);
            UUID last = UUID.randomUUID();
            when(ORDERS.findByCustomerId(any(), any())).thenAnswer(invocation -> cursored(PageRequest.ofSize(20),
                    List.of(order), after("open", last), before("open", order.getId())));

            CursorPageResponse<OrderDTO> response = resource().findByCustomerId(CUSTOMER, null, null);

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            verify(ORDERS, times(1)).findByCustomerId(Mockito.eq(CUSTOMER), request.capture());
            assertThat(request.getValue().cursor()).isEmpty();
            assertThat(request.getValue().size()).isEqualTo(20);
            assertThat(request.getValue().requestTotal()).isFalse();
            assertThat(response.content()).containsExactly(new OrderDTO(order.getId(), "open"));
            assertThat(response.hasNext()).isTrue();
            assertThat(response.hasPrevious()).isTrue();
            CursorCodec.Token next = CursorCodec.decode(response.nextCursor(), context());
            CursorCodec.Token previous = CursorCodec.decode(response.previousCursor(), context());
            assertThat(next.previous()).isFalse();
            assertThat((List<Object>) next.cursor().elements()).containsExactly("open", last);
            assertThat(previous.previous()).isTrue();
            verifyNoMoreInteractions(ORDERS);
        }

        @Test
        @DisplayName("Should continue forward from a next token")
        void shouldContinueForward() {
            String token = CursorCodec.encode(after("open", 7L), context());
            when(ORDERS.findByCustomerId(any(), any())).thenAnswer(invocation -> cursored(after("open", 7L), List.of(), null,
                    before("x")));

            resource().findByCustomerId(CUSTOMER, "5", token);

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            verify(ORDERS, times(1)).findByCustomerId(Mockito.eq(CUSTOMER), request.capture());
            assertThat(request.getValue().mode()).isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            assertThat((List<Object>) request.getValue().cursor().orElseThrow().elements())
                    .containsExactly("open", 7L);
            assertThat(request.getValue().size()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should continue backward from a previous token")
        void shouldContinueBackward() {
            String token = CursorCodec.encode(before("open", 7L), context());
            when(ORDERS.findByCustomerId(any(), any())).thenAnswer(invocation -> cursored(before("open", 7L), List.of(),
                    after("x"), null));

            resource().findByCustomerId(CUSTOMER, null, token);

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            verify(ORDERS).findByCustomerId(Mockito.eq(CUSTOMER), request.capture());
            assertThat(request.getValue().mode()).isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
        }

        @Test
        @DisplayName("Should issue no cursor and not call navigation when none is available")
        void shouldIssueNoCursorAtTheEnd() {
            CursoredPage<Order> page = cursored(PageRequest.ofSize(20), List.of(), null, null);
            when(ORDERS.findByCustomerId(any(), any())).thenReturn(page);

            CursorPageResponse<OrderDTO> response = resource().findByCustomerId(CUSTOMER, null, null);

            assertThat(response.content()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.hasPrevious()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.previousCursor()).isNull();
            verify(page, never()).nextPageRequest();
            verify(page, never()).previousPageRequest();
        }

        @Test
        @DisplayName("Should reject a token issued for different criteria without calling the repository")
        void shouldRejectOtherCriteria() {
            String token = CursorCodec.encode(after("open", 7L), Pagination.context("findByCustomerId",
                    UUID.randomUUID()));

            assertThatThrownBy(() -> resource().findByCustomerId(CUSTOMER, null, token))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource().findByCustomerId(CUSTOMER, null, "garbage"))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(ORDERS);
        }

        @Test
        @DisplayName("Should let a provider UnsupportedOperationException through without an offset fallback")
        void shouldPropagateUnsupportedProvider() {
            when(ORDERS.findByCustomerId(any(), any())).thenThrow(new UnsupportedOperationException("no cursors"));

            assertThatThrownBy(() -> resource().findByCustomerId(CUSTOMER, null, null))
                    .isInstanceOf(UnsupportedOperationException.class);
            verify(ORDERS, times(1)).findByCustomerId(any(), any());
            verifyNoMoreInteractions(ORDERS);
        }

        @Test
        @DisplayName("Should map offset page content to DTOs without the excluded attribute")
        void shouldMapOffsetContent() {
            Order order = new Order();
            order.setId(UUID.randomUUID());
            order.setStatus("open");
            order.setCustomerId(CUSTOMER);
            when(ORDERS.findByStatus(any(), any())).thenAnswer(invocation -> page(PageRequest.ofPage(1, 20, false), false, false, false, List.of(order)));

            PageResponse<OrderDTO> response = resource().findByStatus("open", null, null, null);

            assertThat(response.content()).containsExactly(new OrderDTO(order.getId(), "open"));
            assertThat(java.util.Arrays.stream(OrderDTO.class.getRecordComponents())
                    .map(java.lang.reflect.RecordComponent::getName)).doesNotContain("customerId");
        }
    }

    @Nested
    @DisplayName("Paginated repository without DTOs")
    class WithoutDtos {

        @Test
        @DisplayName("Should return entities in an offset page of a repository without DTOs")
        void shouldReturnEntities() {
            Product product = new Product();
            when(PRODUCTS.findByCategory(any(), any())).thenAnswer(invocation -> page(PageRequest.ofPage(1, 20, false), false, false, false, List.of(product)));

            PageResponse<Product> response = weld.select(PagedProductRepositoryResource.class).get()
                    .findByCategory("books", null, null, null);

            assertThat(response.content()).containsExactly(product);
        }
    }
}
