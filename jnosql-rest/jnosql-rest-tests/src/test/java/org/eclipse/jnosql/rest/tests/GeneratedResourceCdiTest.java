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
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.eclipse.jnosql.rest.tests.sample.Address;
import org.eclipse.jnosql.rest.tests.sample.Customer;
import org.eclipse.jnosql.rest.tests.sample.CustomerDTO;
import org.eclipse.jnosql.rest.tests.sample.CustomerDTOMapper;
import org.eclipse.jnosql.rest.tests.sample.CustomerDTOMapperImpl;
import org.eclipse.jnosql.rest.tests.sample.CustomerCreateRequest;
import org.eclipse.jnosql.rest.tests.sample.CustomerRepository;
import org.eclipse.jnosql.rest.tests.sample.CustomerRepositoryRepository;
import org.eclipse.jnosql.rest.tests.sample.CustomerUpdateRequest;
import org.eclipse.jnosql.rest.tests.sample.Product;
import org.eclipse.jnosql.rest.tests.sample.ProductRepository;
import org.eclipse.jnosql.rest.tests.sample.ProductRepositoryRepository;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableWeld
@DisplayName("Generated resources managed by CDI")
class GeneratedResourceCdiTest {

    private static final ProductRepository PRODUCTS = Mockito.mock(ProductRepository.class);
    private static final CustomerRepository CUSTOMERS = Mockito.mock(CustomerRepository.class);

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                    .addBeanClasses(ProductRepositoryRepository.class, CustomerRepositoryRepository.class,
                            CustomerDTOMapperImpl.class))
            .addBeans(MockBean.<ProductRepository>builder().types(ProductRepository.class)
                    .scope(ApplicationScoped.class).creating(PRODUCTS).build())
            .addBeans(MockBean.<CustomerRepository>builder().types(CustomerRepository.class)
                    .scope(ApplicationScoped.class).creating(CUSTOMERS).build())
            .activate(RequestScoped.class)
            .build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(PRODUCTS, CUSTOMERS);
    }

    private ProductRepositoryRepository products() {
        return weld.select(ProductRepositoryRepository.class).get();
    }

    private CustomerRepositoryRepository customers() {
        return weld.select(CustomerRepositoryRepository.class).get();
    }

    @Nested
    @DisplayName("When Weld creates the resources")
    class WhenWeldCreatesTheResources {

        @Test
        @DisplayName("Should discover the generated resources as beans")
        void shouldDiscoverTheResources() {
            BeanManager manager = weld.getBeanManager();

            assertThat(manager.getBeans(ProductRepositoryRepository.class)).hasSize(1);
            assertThat(manager.getBeans(CustomerRepositoryRepository.class)).hasSize(1);
        }

        @Test
        @DisplayName("Should discover the MapStruct implementation as a CDI bean of the mapper interface")
        void shouldDiscoverTheMapper() {
            assertThat(weld.select(CustomerDTOMapper.class).get()).isInstanceOf(CustomerDTOMapperImpl.class);
        }

        @Test
        @DisplayName("Should create the instance with the injection constructor and no null dependency")
        void shouldInjectEveryDependency() throws Exception {
            Object product = contextualInstance(ProductRepositoryRepository.class);
            Object customer = contextualInstance(CustomerRepositoryRepository.class);

            assertThat(field(product, "repository")).isInstanceOf(ProductRepository.class);
            assertThat(field(customer, "repository")).isInstanceOf(CustomerRepository.class);
            assertThat(field(customer, "mapper")).isInstanceOf(CustomerDTOMapper.class).isNotNull();
        }

        @Test
        @DisplayName("Should never run repository calls with null dependencies through a CDI-managed instance")
        void shouldDelegateThroughTheManagedInstance() {
            UUID id = UUID.randomUUID();
            Product product = new Product();
            when(PRODUCTS.findById(id)).thenReturn(Optional.of(product));

            assertThat(products().findById(id)).isSameAs(product);
        }

        private Object contextualInstance(Class<?> type) {
            BeanManager manager = weld.getBeanManager();
            Bean<?> bean = manager.resolve(manager.getBeans(type));
            products().existsByName("probe");
            customers().existsByName("probe");
            return manager.getContext(RequestScoped.class).get(bean);
        }

        private Object field(Object target, String name) throws Exception {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        }
    }

    @Nested
    @DisplayName("When a resource without DTOs delegates")
    class WhenTheEntityIsTheRepresentation {

        @Test
        @DisplayName("Should return the entity found by id")
        void shouldFindById() {
            UUID id = UUID.randomUUID();
            Product product = new Product();
            when(PRODUCTS.findById(id)).thenReturn(Optional.of(product));

            assertThat(products().findById(id)).isSameAs(product);
            verify(PRODUCTS, times(1)).findById(id);
        }

        @Test
        @DisplayName("Should answer not found when the id does not exist")
        void shouldFailWhenTheIdIsUnknown() {
            when(PRODUCTS.findById(Mockito.any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> products().findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should return every entity and close the stream")
        void shouldFindAll() {
            Product product = new Product();
            boolean[] closed = {false};
            when(PRODUCTS.findAll()).thenReturn(Stream.of(product).onClose(() -> closed[0] = true));

            assertThat(products().findAll()).containsExactly(product);
            assertThat(closed[0]).isTrue();
        }

        @Test
        @DisplayName("Should pass the query parameter to a @Find method")
        void shouldFindByCategory() {
            Product product = new Product();
            when(PRODUCTS.findByCategory("books")).thenReturn(List.of(product));

            assertThat(products().findByCategory("books")).containsExactly(product);
            verify(PRODUCTS).findByCategory("books");
        }

        @Test
        @DisplayName("Should reject a missing query parameter without calling the repository")
        void shouldRejectMissingQueryParameter() {
            assertThatThrownBy(() -> products().findByCategory(null)).isInstanceOf(BadRequestException.class);
            verifyNoInteractions(PRODUCTS);
        }

        @Test
        @DisplayName("Should return scalar results of count and exists queries")
        void shouldCountAndCheckExistence() {
            when(PRODUCTS.countByCategory("books")).thenReturn(3L);
            when(PRODUCTS.existsByName("book")).thenReturn(true);

            assertThat(products().countByCategory("books")).isEqualTo(3L);
            assertThat(products().existsByName("book")).isTrue();
        }

        @Test
        @DisplayName("Should answer 201 with the inserted entity")
        void shouldInsert() {
            Product product = new Product();
            when(PRODUCTS.insert(product)).thenReturn(product);

            Response response = products().insert(product);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getEntity()).isSameAs(product);
        }

        @Test
        @DisplayName("Should update and delete through the repository")
        void shouldUpdateAndDelete() {
            UUID id = UUID.randomUUID();
            Product product = new Product();
            product.setPrice(BigDecimal.TEN);
            when(PRODUCTS.update(product)).thenReturn(product);

            assertThat(products().update(product)).isSameAs(product);
            products().deleteById(id);
            verify(PRODUCTS).deleteById(id);
        }

        @Test
        @DisplayName("Should delegate an explicitly exposed deleteBy query")
        void shouldDeleteByCategory() {
            products().deleteByCategory("books");

            verify(PRODUCTS).deleteByCategory("books");
        }

        @Test
        @DisplayName("Should propagate repository exceptions")
        void shouldPropagateRepositoryExceptions() {
            when(PRODUCTS.countByCategory("books")).thenThrow(new IllegalStateException("database down"));

            assertThatThrownBy(() -> products().countByCategory("books"))
                    .isInstanceOf(IllegalStateException.class).hasMessage("database down");
        }
    }

    @Nested
    @DisplayName("When a resource with DTOs delegates")
    class WhenTheDtoIsTheRepresentation {

        private Customer customer() {
            Address address = new Address();
            address.setCity("Lisbon");
            Customer customer = new Customer();
            customer.setId(1L);
            customer.setName("Ada");
            customer.setAge(36);
            customer.setNickname("ada");
            customer.setPasswordHash("secret-hash");
            customer.setAddress(address);
            customer.setTags(List.of("vip"));
            customer.setPreviousAddresses(List.of(address));
            customer.setAttributes(Map.of("k", "v"));
            return customer;
        }

        @Test
        @DisplayName("Should map the entity to a DTO with nested, collection and optional values")
        void shouldMapEntityToDto() {
            when(CUSTOMERS.findById(1L)).thenReturn(Optional.of(customer()));

            CustomerDTO dto = customers().findById(1L);

            assertThat(dto.name()).isEqualTo("Ada");
            assertThat(dto.age()).isEqualTo(36);
            assertThat(dto.nickname()).isEqualTo("ada");
            assertThat(dto.address().city()).isEqualTo("Lisbon");
            assertThat(dto.tags()).containsExactly("vip");
            assertThat(dto.previousAddresses()).extracting("city").containsExactly("Lisbon");
            assertThat(dto.attributes()).containsEntry("k", "v");
        }

        @Test
        @DisplayName("Should never expose an excluded attribute in the response DTO")
        void shouldNotExposeExcludedAttributes() {
            assertThat(CustomerDTO.class.getRecordComponents())
                    .extracting("name").doesNotContain("passwordHash");
            assertThat(CustomerDTO.class.getDeclaredFields()).extracting("name").doesNotContain("passwordHash");
        }

        @Test
        @DisplayName("Should map collections of entities")
        void shouldMapLists() {
            when(CUSTOMERS.findByName("Ada")).thenReturn(List.of(customer()));
            when(CUSTOMERS.findAll()).thenReturn(Stream.of(customer()));

            assertThat(customers().findByName("Ada")).extracting("name").containsExactly("Ada");
            assertThat(customers().findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Should map the create request to an entity, insert it and answer 201 with the DTO")
        void shouldInsert() {
            when(CUSTOMERS.insert(Mockito.any(Customer.class))).thenAnswer(call -> call.getArgument(0));

            Response response = customers().insert(new CustomerCreateRequest(7L, "Grace", 40, "gm", "hash",
                    null, List.of("new"), null, null));

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getEntity()).isInstanceOf(CustomerDTO.class);
            assertThat(((CustomerDTO) response.getEntity()).name()).isEqualTo("Grace");
            verify(CUSTOMERS).insert(Mockito.argThat(entity -> entity.getId() == 7L
                    && "hash".equals(entity.getPasswordHash())));
        }

        @Test
        @DisplayName("Should update only the provided attributes of the stored entity")
        void shouldUpdateOnlyProvidedAttributes() {
            Customer stored = customer();
            when(CUSTOMERS.findById(1L)).thenReturn(Optional.of(stored));
            when(CUSTOMERS.update(stored)).thenReturn(stored);

            CustomerDTO dto = customers().update(1L, new CustomerUpdateRequest("Ada L.", null, null, null, null,
                    null, null, null));

            assertThat(dto.name()).isEqualTo("Ada L.");
            assertThat(dto.age()).isEqualTo(36);
            assertThat(stored.getPasswordHash()).isEqualTo("secret-hash");
            verify(CUSTOMERS).update(stored);
        }

        @Test
        @DisplayName("Should answer not found when updating an unknown entity")
        void shouldNotUpdateUnknownEntities() {
            when(CUSTOMERS.findById(9L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customers().update(9L, new CustomerUpdateRequest(null, null, null, null,
                    null, null, null, null))).isInstanceOf(NotFoundException.class);
            verify(CUSTOMERS, Mockito.never()).update(Mockito.any(Customer.class));
        }

        @Test
        @DisplayName("Should box primitive query parameters and reject a missing value")
        void shouldRequirePrimitiveQueryParameters() {
            when(CUSTOMERS.findByAge(36)).thenReturn(Optional.of(customer()));

            assertThat(customers().findByAge(36).name()).isEqualTo("Ada");
            assertThatThrownBy(() -> customers().findByAge(null)).isInstanceOf(BadRequestException.class);
        }
    }
}
