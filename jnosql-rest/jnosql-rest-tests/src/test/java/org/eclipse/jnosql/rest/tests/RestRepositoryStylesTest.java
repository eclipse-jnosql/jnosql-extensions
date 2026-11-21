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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Path;
import org.eclipse.jnosql.rest.tests.basic.Customer;
import org.eclipse.jnosql.rest.tests.basic.CustomerRepository;
import org.eclipse.jnosql.rest.tests.basic.CustomerRepositoryResource;
import org.eclipse.jnosql.rest.tests.custom.Order;
import org.eclipse.jnosql.rest.tests.custom.OrderRepository;
import org.eclipse.jnosql.rest.tests.custom.OrderRepositoryResource;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
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
@DisplayName("Repository styles")
class RestRepositoryStylesTest {

    private static final CustomerRepository CUSTOMERS = Mockito.mock(CustomerRepository.class);
    private static final OrderRepository ORDERS = Mockito.mock(OrderRepository.class);

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                    .addBeanClasses(CustomerRepositoryResource.class, OrderRepositoryResource.class))
            .addBeans(MockBean.<CustomerRepository>builder().types(CustomerRepository.class)
                    .scope(ApplicationScoped.class).creating(CUSTOMERS).build())
            .addBeans(MockBean.<OrderRepository>builder().types(OrderRepository.class)
                    .scope(ApplicationScoped.class).creating(ORDERS).build())
            .activate(RequestScoped.class)
            .build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(CUSTOMERS, ORDERS);
    }

    private static Set<String> methods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).filter(method -> !method.isSynthetic())
                .map(Method::getName).collect(Collectors.toSet());
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
}
