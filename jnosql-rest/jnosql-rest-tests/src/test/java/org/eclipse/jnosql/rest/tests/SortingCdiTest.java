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
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.jnosql.rest.pagination.CursorPageResponse;
import org.eclipse.jnosql.rest.pagination.PageResponse;
import org.eclipse.jnosql.rest.pagination.PaginationRequest;
import org.eclipse.jnosql.rest.pagination.SortCriterion;
import org.eclipse.jnosql.rest.tests.sorting.Book;
import org.eclipse.jnosql.rest.tests.sorting.BookDTO;
import org.eclipse.jnosql.rest.tests.sorting.BookDTOMapperImpl;
import org.eclipse.jnosql.rest.tests.sorting.BookRepository;
import org.eclipse.jnosql.rest.tests.sorting.BookRepositoryResource;
import org.eclipse.jnosql.rest.tests.sorting.BookRepositorySearchRequest;
import org.eclipse.jnosql.rest.tests.sorting.BookRepositorySearchRequest.StringFilter;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableWeld
@DisplayName("Generated Limit, Sort and Order endpoints with CDI")
class SortingCdiTest {

    private static final BookRepository BOOKS = Mockito.mock(BookRepository.class);

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                    .addBeanClasses(BookRepositoryResource.class, BookDTOMapperImpl.class))
            .addBeans(MockBean.<BookRepository>builder().types(BookRepository.class)
                    .scope(ApplicationScoped.class).creating(BOOKS).build())
            .activate(RequestScoped.class)
            .build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(BOOKS);
    }

    private BookRepositoryResource resource() {
        return weld.select(BookRepositoryResource.class).get();
    }

    private static Book book(long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setInternalCode("secret-" + id);
        return book;
    }

    @SuppressWarnings("unchecked")
    private static Page<Book> page(List<Book> content, boolean next) {
        Page<Book> page = Mockito.mock(Page.class);
        when(page.content()).thenReturn(content);
        when(page.pageRequest()).thenReturn(PageRequest.ofPage(1, 20, false));
        when(page.hasNext()).thenReturn(next);
        return page;
    }

    @SuppressWarnings("unchecked")
    private static CursoredPage<Book> cursored(List<Book> content, PageRequest next) {
        CursoredPage<Book> page = Mockito.mock(CursoredPage.class);
        when(page.content()).thenReturn(content);
        when(page.pageRequest()).thenReturn(PageRequest.ofSize(20));
        when(page.hasNext()).thenReturn(next != null);
        if (next != null) {
            when(page.nextPageRequest()).thenReturn(next);
        }
        return page;
    }

    @Nested
    @DisplayName("Paginated findAll")
    class FindAll {

        @Test
        @DisplayName("Should expose a single paginated GET on the collection root")
        void shouldExposeOneRoot() {
            List<Method> roots = Arrays.stream(BookRepositoryResource.class.getDeclaredMethods())
                    .filter(method -> method.getName().equals("findAll")).toList();

            assertThat(roots).hasSize(1);
            assertThat(roots.get(0).getParameterTypes()).containsExactly(String.class, String.class, String.class,
                    String.class);
            assertThat(roots.get(0).isAnnotationPresent(jakarta.ws.rs.GET.class)).isTrue();
            assertThat(roots.get(0).isAnnotationPresent(jakarta.ws.rs.Path.class)).isFalse();
        }

        @Test
        @DisplayName("Should default to the first page of twenty, unsorted, and never call the unbounded findAll")
        void shouldUseDefaults() {
            Book book = book(1, "Dune");
            when(BOOKS.findAll(any(PageRequest.class), Mockito.<Order<Book>>any()))
                    .thenAnswer(call -> page(List.of(book), true));

            PageResponse<BookDTO> response = resource().findAll(null, null, null, null);

            verify(BOOKS).findAll(PageRequest.ofPage(1, 20, false), Order.by());
            verify(BOOKS, never()).findAll();
            assertThat(response.content()).containsExactly(new BookDTO(1L, "Dune", null, null));
            assertThat(response.offset()).isZero();
            assertThat(response.size()).isEqualTo(20);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.totalElements()).isNull();
        }

        @Test
        @DisplayName("Should pass the page offset, size and every sort criterion in order")
        void shouldPassOffsetSizeAndOrder() {
            when(BOOKS.findAll(any(PageRequest.class), Mockito.<Order<Book>>any()))
                    .thenAnswer(call -> page(List.of(), false));

            resource().findAll("25", "2", null, "title:asc,pages:desc");

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Order<Book>> order = ArgumentCaptor.forClass(Order.class);
            verify(BOOKS).findAll(request.capture(), order.capture());
            assertThat(request.getValue().pageNumber()).isEqualTo(3);
            assertThat(request.getValue().size()).isEqualTo(25);
            assertThat(order.getValue().sorts()).containsExactly(Sort.asc("title"), Sort.desc("pages"));
        }

        @Test
        @DisplayName("Should reject invalid pagination and sorting before touching the repository")
        void shouldRejectInvalid() {
            BookRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.findAll("0", null, null, null)).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findAll("101", null, null, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findAll(null, "-1", null, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findAll(null, "99999999999999999999", null, null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findAll(null, null, null, "title:sideways"))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.findAll(null, null, null, "unknown:asc"))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }

        @Test
        @DisplayName("Should not accept sorting by an attribute excluded from the DTO")
        void shouldRejectExcludedAttribute() {
            assertThatThrownBy(() -> resource().findAll(null, null, null, "internalCode:asc"))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }
    }

    @Nested
    @DisplayName("Limit")
    class LimitSupport {

        @Test
        @DisplayName("Should convert limit and startAt into a Jakarta Data Limit and map the result")
        void shouldDelegate() {
            when(BOOKS.byAuthorLimited(any(), any())).thenReturn(List.of(book(7, "Emma")));

            List<BookDTO> result = resource().byAuthorLimited("austen", "10", "5");

            verify(BOOKS).byAuthorLimited("austen", Limit.of(10, 5));
            assertThat(result).containsExactly(new BookDTO(7L, "Emma", null, null));
        }

        @Test
        @DisplayName("Should default to a bounded limit")
        void shouldDefault() {
            when(BOOKS.byAuthorLimited(any(), any())).thenReturn(List.of());

            resource().byAuthorLimited("austen", null, null);

            verify(BOOKS).byAuthorLimited("austen", Limit.of(20, 1));
        }

        @Test
        @DisplayName("Should reject invalid limits without calling the repository")
        void shouldReject() {
            BookRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.byAuthorLimited("a", "0", null)).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorLimited("a", "1000", null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorLimited("a", "x", null))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorLimited("a", "10", "0"))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorLimited(null, "10", null))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }
    }

    @Nested
    @DisplayName("Sort and Order")
    class SortAndOrder {

        @Test
        @DisplayName("Should build a single Sort")
        void shouldBuildSort() {
            when(BOOKS.byAuthorSorted(any(), any())).thenReturn(List.of());

            resource().byAuthorSorted("austen", "pages:desc");

            verify(BOOKS).byAuthorSorted("austen", Sort.desc("pages"));
        }

        @Test
        @DisplayName("Should reject a Sort with several or invalid criteria")
        void shouldRejectSort() {
            BookRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.byAuthorSorted("a", "title:asc,pages:desc"))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorSorted("a", null)).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorSorted("a", "internalCode:asc"))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }

        @Test
        @DisplayName("Should build an Order with mixed directions in the requested order")
        void shouldBuildOrder() {
            when(BOOKS.byAuthorOrdered(any(), any())).thenReturn(List.of());

            resource().byAuthorOrdered("austen", "pages:desc,title:asc,id:desc");

            verify(BOOKS).byAuthorOrdered("austen",
                    Order.by(Sort.desc("pages"), Sort.asc("title"), Sort.desc("id")));
        }

        @Test
        @DisplayName("Should pass an empty Order when no sort is requested")
        void shouldPassEmptyOrder() {
            when(BOOKS.byAuthorOrdered(any(), any())).thenReturn(List.of());

            resource().byAuthorOrdered("austen", null);

            verify(BOOKS).byAuthorOrdered("austen", Order.by());
        }
    }

    @Nested
    @DisplayName("Cursor pagination with a dynamic Order")
    class CursorSupport {

        @Test
        @DisplayName("Should require a total ordering that includes the identifier")
        void shouldRequireTotalOrdering() {
            BookRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.byAuthorCursor("a", "title:asc", null, null))
                    .isInstanceOf(BadRequestException.class).hasMessageContaining("total ordering");
            assertThatThrownBy(() -> resource.byAuthorCursor("a", null, null, null))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }

        @Test
        @DisplayName("Should keep the sort across cursor requests and reject a changed sort")
        void shouldBindCursorToSort() {
            when(BOOKS.byAuthorCursor(any(), any(), any())).thenAnswer(call -> cursored(List.of(book(1, "A")),
                    PageRequest.afterCursor(PageRequest.Cursor.forKey("A", 1L), 1, 20, false)));

            CursorPageResponse<BookDTO> first = resource().byAuthorCursor("x", "title:asc,id:asc", null, null);
            assertThat(first.nextCursor()).isNotBlank();

            CursorPageResponse<BookDTO> second = resource().byAuthorCursor("x", "title:asc,id:asc", null,
                    first.nextCursor());
            assertThat(second.content()).hasSize(1);

            BookRepositoryResource resource = resource();
            assertThatThrownBy(() -> resource.byAuthorCursor("x", "title:desc,id:asc", null, first.nextCursor()))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.byAuthorCursor("x", "id:asc", null, first.nextCursor()))
                    .isInstanceOf(BadRequestException.class);

            ArgumentCaptor<PageRequest> request = ArgumentCaptor.forClass(PageRequest.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Order<Book>> order = ArgumentCaptor.forClass(Order.class);
            verify(BOOKS, Mockito.times(2)).byAuthorCursor(Mockito.eq("x"), request.capture(), order.capture());
            assertThat(request.getAllValues().get(1).mode()).isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            assertThat(order.getAllValues()).allSatisfy(value -> assertThat(value.sorts())
                    .containsExactly(Sort.asc("title"), Sort.asc("id")));
        }
    }

    @Nested
    @DisplayName("Restriction search with pagination and sorting")
    class SearchSupport {

        private BookRepositorySearchRequest request(String author, List<SortCriterion> sort,
                                                    PaginationRequest pagination) {
            return new BookRepositorySearchRequest(null, null, new StringFilter(author, null, null, null, null, null,
                    null, null, null), null, null, sort, pagination);
        }

        @Test
        @DisplayName("Should filter, sort and paginate through the repository")
        void shouldDelegate() {
            when(BOOKS.search(any(), any(), any())).thenAnswer(call -> page(List.of(book(2, "Emma")), false));

            PageResponse<BookDTO> response = resource().search(request("austen",
                    List.of(new SortCriterion("title", "desc"), new SortCriterion("pages", null)),
                    new PaginationRequest(1L, 10, null, null)));

            ArgumentCaptor<PageRequest> pageRequest = ArgumentCaptor.forClass(PageRequest.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Order<Book>> order = ArgumentCaptor.forClass(Order.class);
            verify(BOOKS).search(any(), pageRequest.capture(), order.capture());
            assertThat(pageRequest.getValue().pageNumber()).isEqualTo(2);
            assertThat(pageRequest.getValue().size()).isEqualTo(10);
            assertThat(order.getValue().sorts()).containsExactly(Sort.desc("title"), Sort.asc("pages"));
            assertThat(response.content()).containsExactly(new BookDTO(2L, "Emma", null, null));
        }

        @Test
        @DisplayName("Should reject excluded or unknown sort attributes and still require a filter")
        void shouldReject() {
            BookRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.search(request("a", List.of(new SortCriterion("internalCode", "asc")),
                    null))).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.search(request("a", List.of(new SortCriterion("title", "up")), null)))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.search(new BookRepositorySearchRequest(null, null, null, null, null,
                    List.of(new SortCriterion("title", "asc")), null))).isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }

        @Test
        @DisplayName("Should reject an empty filter object instead of dropping it silently")
        void shouldRejectEmptyFilter() {
            BookRepositoryResource resource = resource();

            assertThatThrownBy(() -> resource.search(new BookRepositorySearchRequest(null, null,
                    new StringFilter("austen", null, null, null, null, null, null, null, null),
                    new BookRepositorySearchRequest.IntegerFilter(null, null, null, null, null, null, null, null,
                            null), null, null, null))).isInstanceOf(BadRequestException.class);
            verifyNoInteractions(BOOKS);
        }
    }
}
