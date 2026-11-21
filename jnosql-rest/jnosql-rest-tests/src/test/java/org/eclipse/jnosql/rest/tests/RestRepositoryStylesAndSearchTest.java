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

import jakarta.data.constraint.AtLeast;
import jakarta.data.constraint.AtMost;
import jakarta.data.constraint.Between;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.In;
import jakarta.data.constraint.NotNull;
import jakarta.data.constraint.Null;
import jakarta.data.metamodel.Attribute;
import jakarta.data.restrict.BasicRestriction;
import jakarta.data.restrict.CompositeRestriction;
import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import org.eclipse.jnosql.rest.QUERY;
import org.eclipse.jnosql.rest.tests.basic.Customer;
import org.eclipse.jnosql.rest.tests.basic.CustomerRepository;
import org.eclipse.jnosql.rest.tests.basic.CustomerRepositoryResource;
import org.eclipse.jnosql.rest.tests.custom.Order;
import org.eclipse.jnosql.rest.tests.custom.OrderRepository;
import org.eclipse.jnosql.rest.tests.custom.OrderRepositoryResource;
import org.eclipse.jnosql.rest.tests.sample.Product;
import org.eclipse.jnosql.rest.tests.search.ProductDTO;
import org.eclipse.jnosql.rest.tests.search.ProductDTOMapperImpl;
import org.eclipse.jnosql.rest.tests.search.ProductDtoSearchRepository;
import org.eclipse.jnosql.rest.tests.search.ProductDtoSearchRepositoryResource;
import org.eclipse.jnosql.rest.tests.search.ProductDtoSearchRepositorySearchRequest;
import org.eclipse.jnosql.rest.tests.search.ProductSearchRepository;
import org.eclipse.jnosql.rest.tests.search.ProductSearchRepositoryResource;
import org.eclipse.jnosql.rest.tests.search.ProductSearchRepositorySearchRequest;
import org.eclipse.jnosql.rest.tests.search.ProductSearchRepositorySearchRequest.BigDecimalFilter;
import org.eclipse.jnosql.rest.tests.search.ProductSearchRepositorySearchRequest.StringFilter;
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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableWeld
@DisplayName("Repository styles and dynamic restriction search")
class RestRepositoryStylesAndSearchTest {

    private static final CustomerRepository CUSTOMERS = Mockito.mock(CustomerRepository.class);
    private static final OrderRepository ORDERS = Mockito.mock(OrderRepository.class);
    private static final ProductSearchRepository PRODUCTS = Mockito.mock(ProductSearchRepository.class);
    private static final ProductDtoSearchRepository DTO_PRODUCTS = Mockito.mock(ProductDtoSearchRepository.class);

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                    .addBeanClasses(CustomerRepositoryResource.class, OrderRepositoryResource.class,
                            ProductSearchRepositoryResource.class, ProductDtoSearchRepositoryResource.class,
                            ProductDTOMapperImpl.class))
            .addBeans(MockBean.<CustomerRepository>builder().types(CustomerRepository.class)
                    .scope(ApplicationScoped.class).creating(CUSTOMERS).build())
            .addBeans(MockBean.<OrderRepository>builder().types(OrderRepository.class)
                    .scope(ApplicationScoped.class).creating(ORDERS).build())
            .addBeans(MockBean.<ProductSearchRepository>builder().types(ProductSearchRepository.class)
                    .scope(ApplicationScoped.class).creating(PRODUCTS).build())
            .addBeans(MockBean.<ProductDtoSearchRepository>builder().types(ProductDtoSearchRepository.class)
                    .scope(ApplicationScoped.class).creating(DTO_PRODUCTS).build())
            .activate(RequestScoped.class)
            .build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(CUSTOMERS, ORDERS, PRODUCTS, DTO_PRODUCTS);
    }

    private static Set<String> methods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).filter(method -> !method.isSynthetic())
                .map(Method::getName).collect(Collectors.toSet());
    }

    private static StringFilter text(String equal) {
        return new StringFilter(equal, null, null, null, null, null, null, null, null);
    }

    private static BigDecimalFilter price(BigDecimal min, BigDecimal max, List<BigDecimal> between) {
        return new BigDecimalFilter(null, null, null, min, null, max, between, null, null);
    }

    private static ProductSearchRepositorySearchRequest request(StringFilter category, BigDecimalFilter price) {
        return new ProductSearchRepositorySearchRequest(null, null, category, price, null);
    }

    private static String attribute(Restriction<?> restriction) {
        return ((Attribute<?>) ((BasicRestriction<?, ?>) restriction).expression()).name();
    }

    @Nested
    @DisplayName("When the repository extends BasicRepository")
    class WhenBasicRepository {

        @Test
        @DisplayName("Should expose only the operations BasicRepository declares plus the declared methods")
        void shouldExposeSupportedOperations() {
            assertThat(methods(CustomerRepositoryResource.class))
                    .containsExactlyInAnyOrder("save", "delete", "deleteById", "findAll", "findById", "findByName");
        }

        @Test
        @DisplayName("Should not invent insert, update or count operations")
        void shouldNotInventCrudOperations() {
            assertThat(methods(CustomerRepositoryResource.class))
                    .doesNotContain("insert", "update", "insertAll", "updateAll", "saveAll", "count", "existsById");
        }

        @Test
        @DisplayName("Should delegate to the repository through the CDI-managed resource")
        void shouldDelegate() {
            Customer customer = new Customer();
            when(CUSTOMERS.findByName("ana")).thenReturn(List.of(customer));

            var result = weld.select(CustomerRepositoryResource.class).get().findByName("ana");

            assertThat(result).containsExactly(customer);
            verify(CUSTOMERS, times(1)).findByName("ana");
        }
    }

    @Nested
    @DisplayName("When the repository extends no Jakarta Data interface")
    class WhenCustomRepository {

        @Test
        @DisplayName("Should expose declared and inherited methods only, without inventing CRUD")
        void shouldExposeOnlyDeclaredMethods() {
            assertThat(methods(OrderRepositoryResource.class))
                    .containsExactlyInAnyOrder("findByStatus", "countByCustomerId");
        }

        @Test
        @DisplayName("Should use the resource suffix and the configured path")
        void shouldBeNamedAfterTheRepository() {
            assertThat(OrderRepositoryResource.class.getAnnotation(Path.class).value()).isEqualTo("/orders");
        }

        @Test
        @DisplayName("Should delegate declared and inherited methods")
        void shouldDelegate() {
            Order order = new Order();
            when(ORDERS.findByStatus("OPEN")).thenReturn(List.of(order));
            when(ORDERS.countByCustomerId(null)).thenReturn(3L);

            var resource = weld.select(OrderRepositoryResource.class).get();

            assertThat(resource.findByStatus("OPEN")).containsExactly(order);
            verify(ORDERS, times(1)).findByStatus("OPEN");
        }
    }

    @Nested
    @DisplayName("When a Restriction search is exposed")
    class WhenSearching {

        @Test
        @DisplayName("Should use HTTP QUERY on the explicit path with the generated request")
        void shouldDeclareQuery() throws Exception {
            Method search = ProductSearchRepositoryResource.class.getMethod("search",
                    ProductSearchRepositorySearchRequest.class);

            assertThat(search.isAnnotationPresent(QUERY.class)).isTrue();
            assertThat(search.getAnnotation(Path.class).value()).isEqualTo("/search");
            assertThat(search.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Should propagate the OpenAPI operation annotation to the search method")
        void shouldPropagateOpenApi() throws Exception {
            Method search = ProductSearchRepositoryResource.class.getMethod("search",
                    ProductSearchRepositorySearchRequest.class);

            assertThat(search.getAnnotation(org.eclipse.microprofile.openapi.annotations.Operation.class).summary())
                    .isEqualTo("Search products");
        }

        @Test
        @DisplayName("Should convert equality and comparison filters into one composed restriction")
        void shouldBuildTheRestriction() {
            when(PRODUCTS.search(Mockito.any())).thenReturn(List.of(new Product()));
            var request = request(text("books"), price(new BigDecimal("100"), new BigDecimal("500"), null));

            var result = weld.select(ProductSearchRepositoryResource.class).get().search(request);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Restriction<Product>> captor = ArgumentCaptor.forClass(Restriction.class);
            verify(PRODUCTS, times(1)).search(captor.capture());
            assertThat(result).hasSize(1);
            assertThat(captor.getValue()).isInstanceOf(CompositeRestriction.class);
            var parts = ((CompositeRestriction<?>) captor.getValue()).restrictions();
            assertThat(parts).extracting(RestRepositoryStylesAndSearchTest::attribute)
                    .containsExactly("category", "price", "price");
            assertThat(parts).extracting(part -> ((BasicRestriction<?, ?>) part).constraint())
                    .satisfies(constraints -> {
                        assertThat(constraints.get(0)).isInstanceOf(EqualTo.class);
                        assertThat(constraints.get(1)).isInstanceOf(AtLeast.class);
                        assertThat(constraints.get(2)).isInstanceOf(AtMost.class);
                    });
        }

        @Test
        @DisplayName("Should return a single filter as a basic restriction")
        void shouldReturnSingleRestriction() {
            Restriction<Product> restriction = request(text("books"), null).toRestriction();

            assertThat(restriction).isInstanceOf(BasicRestriction.class);
            assertThat(attribute(restriction)).isEqualTo("category");
        }

        @Test
        @DisplayName("Should convert between, in and null checks")
        void shouldConvertBetweenInAndNull() {
            var between = request(null, price(null, null, List.of(BigDecimal.ONE, BigDecimal.TEN)))
                    .toRestriction();
            var in = request(new StringFilter(null, null, null, null, null, null, null, List.of("a", "b"), null),
                    null).toRestriction();
            var isNull = request(new StringFilter(null, null, null, null, null, null, null, null, true), null)
                    .toRestriction();
            var notNull = request(new StringFilter(null, null, null, null, null, null, null, null, false), null)
                    .toRestriction();

            assertThat(((BasicRestriction<?, ?>) between).constraint()).isInstanceOf(Between.class);
            assertThat(((BasicRestriction<?, ?>) in).constraint()).isInstanceOf(In.class);
            assertThat(((BasicRestriction<?, ?>) isNull).constraint()).isInstanceOf(Null.class);
            assertThat(((BasicRestriction<?, ?>) notNull).constraint()).isInstanceOf(NotNull.class);
        }

        @Test
        @DisplayName("Should combine alternatives with a disjunction")
        void shouldCombineAlternatives() {
            var alternatives = new ProductSearchRepositorySearchRequest(null, null, null, null,
                    List.of(request(text("books"), null), request(text("games"), null)));

            var restriction = (CompositeRestriction<?>) alternatives.toRestriction();

            assertThat(restriction.type()).isEqualTo(CompositeRestriction.Type.ANY);
            assertThat(restriction.restrictions()).hasSize(2);
        }

        @Test
        @DisplayName("Should reject an empty request instead of searching without restriction")
        void shouldRejectEmptyRequest() {
            var resource = weld.select(ProductSearchRepositoryResource.class).get();
            var empty = new ProductSearchRepositorySearchRequest(null, null, null, null, null);

            assertThatThrownBy(() -> resource.search(empty)).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> resource.search(null)).isInstanceOf(BadRequestException.class);
            verifyNoInteractions(PRODUCTS);
        }

        @Test
        @DisplayName("Should reject invalid ranges, empty memberships and missing operands")
        void shouldRejectInvalidFilters() {
            assertThatThrownBy(() -> request(null,
                    price(null, null, List.of(BigDecimal.TEN, BigDecimal.ONE))).toRestriction())
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> request(null, price(null, null, List.of(BigDecimal.TEN))).toRestriction())
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> request(null,
                    price(null, null, Arrays.asList(BigDecimal.ONE, null))).toRestriction())
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> request(new StringFilter(null, null, null, null, null, null, null,
                    List.of(), null), null).toRestriction()).isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> request(new StringFilter(null, null, null, null, null, null, null,
                    Arrays.asList("a", null), null), null).toRestriction())
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> request(new StringFilter(null, null, null, null, null, null, null, null, null),
                    null).toRestriction()).isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Should reject an empty list of alternatives")
        void shouldRejectEmptyAlternatives() {
            var request = new ProductSearchRepositorySearchRequest(null, null, null, null, List.of());

            assertThatThrownBy(request::toRestriction).isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("When the search uses DTOs")
    class WhenSearchingWithDtos {

        @Test
        @DisplayName("Should return DTOs and leave excluded attributes out of the request")
        void shouldReturnDtos() {
            Product product = new Product();
            product.setName("book");
            when(DTO_PRODUCTS.search(Mockito.any())).thenReturn(List.of(product));
            var request = new ProductDtoSearchRepositorySearchRequest(null,
                    new ProductDtoSearchRepositorySearchRequest.StringFilter("book", null, null, null, null, null,
                            null, null, null), null, null);

            List<ProductDTO> result = weld.select(ProductDtoSearchRepositoryResource.class).get().search(request);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("book");
            verify(DTO_PRODUCTS, times(1)).search(Mockito.any());
            assertThat(Arrays.stream(ProductDtoSearchRepositorySearchRequest.class.getRecordComponents())
                    .map(component -> component.getName())).doesNotContain("category");
        }
    }
}
