/*
 *  Copyright (c) 2020 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.entities;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Order.class);
    }

    @Nested
    @DisplayName("When the order metadata is inspected")
    class WhenTheOrderMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Order");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Order.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Order.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Order.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isTrue();
        }


        @Test
        @DisplayName("Should create a new domain instance")
        void shouldCreateNewInstance() {
            Order order = entityMetadata.newInstance();
            assertThat(order).as("value of order")
                    .isNotNull().isInstanceOf(Order.class);
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(3);

                soft.assertThat(fields.contains("id")).as("value of fields.contains(\"id\")").isTrue();
                soft.assertThat(fields.contains("users")).as("value of fields.contains(\"users\")").isTrue();
                soft.assertThat(fields.contains("products")).as("value of fields.contains(\"products\")").isTrue();
            });
        }


        @Test
        @DisplayName("Should index mapped fields by name")
        void shouldIndexMappedFieldsByName() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(groupByName).as("value of groupByName").isNotNull();
                soft.assertThat(groupByName.get("_id")).as("value of groupByName.get(\"_id\")").isNotNull();
                soft.assertThat(groupByName.get("users")).as("value of groupByName.get(\"users\")").isNotNull();
            });
        }


        @Test
        @DisplayName("Should users")
        void shouldUsers() {
            var groupByName = entityMetadata.fieldsGroupByName();
            var users = groupByName.get("users");
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(users).as("value of users").isNotNull();
                soft.assertThat(users).as("value of users").isInstanceOf(ArrayFieldMetadata.class);
                var arrayFieldMetadata = (ArrayFieldMetadata) users;
                soft.assertThat(arrayFieldMetadata.fieldName()).as("value of arrayFieldMetadata.fieldName()").isEqualTo("users");
                soft.assertThat(arrayFieldMetadata.type()).as("value of arrayFieldMetadata.type()").isEqualTo(String[].class);
                soft.assertThat(arrayFieldMetadata.isEmbeddable()).as("value of arrayFieldMetadata.isEmbeddable()").isFalse();
                soft.assertThat(arrayFieldMetadata.elementType()).as("value of arrayFieldMetadata.elementType()").isEqualTo(String.class);
                soft.assertThat(arrayFieldMetadata.mappingType()).as("value of arrayFieldMetadata.mappingType()").isEqualTo(MappingType.ARRAY);
            });
        }


        @Test
        @DisplayName("Should products")
        void shouldProducts() {
            var groupByName = entityMetadata.fieldsGroupByName();
            var products = groupByName.get("products");
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(products).as("value of products").isNotNull();
                soft.assertThat(products).as("value of products").isInstanceOf(ArrayFieldMetadata.class);
                var arrayFieldMetadata = (ArrayFieldMetadata) products;
                soft.assertThat(arrayFieldMetadata.fieldName()).as("value of arrayFieldMetadata.fieldName()").isEqualTo("products");
                soft.assertThat(arrayFieldMetadata.type()).as("value of arrayFieldMetadata.type()").isEqualTo(Product[].class);
                soft.assertThat(arrayFieldMetadata.isEmbeddable()).as("value of arrayFieldMetadata.isEmbeddable()").isTrue();
                soft.assertThat(arrayFieldMetadata.elementType()).as("value of arrayFieldMetadata.elementType()").isEqualTo(Product.class);
                soft.assertThat(arrayFieldMetadata.mappingType()).as("value of arrayFieldMetadata.mappingType()").isEqualTo(MappingType.ARRAY);
            });
        }


        @Test
        @DisplayName("Should get users")
        void shouldGetUsers() {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUsers(new String[]{"Ada", "Lucas"});
            order.setProducts(new Product[]{product("TV"), product("Radio")});
            var groupByName = entityMetadata.fieldsGroupByName();
            var users = (ArrayFieldMetadata) groupByName.get("users");
            var value = users.read(order);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isNotNull();
                soft.assertThat(value).as("value of value").isInstanceOf(String[].class);
                var usersValue = (String[]) value;
                soft.assertThat(usersValue).as("value of usersValue").containsExactly("Ada", "Lucas");
            });
        }


        @Test
        @DisplayName("Should set users")
        void shouldSetUsers() {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setProducts(new Product[]{product("TV"), product("Radio")});
            var groupByName = entityMetadata.fieldsGroupByName();
            var users = (ArrayFieldMetadata) groupByName.get("users");
            users.write(order, new String[]{"Ada", "Lucas"});
            var value = order.getUsers();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isNotNull();
                soft.assertThat(value).as("value of value").isInstanceOf(String[].class);
                soft.assertThat(value).as("value of value").containsExactly("Ada", "Lucas");
            });
        }


        @Test
        @DisplayName("Should get products")
        void shouldGetProducts() {
            var tv = product("TV");
            var radio = product("Radio");
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUsers(new String[]{"Ada", "Lucas"});
            order.setProducts(new Product[]{tv, radio});
            var groupByName = entityMetadata.fieldsGroupByName();
            var products = (ArrayFieldMetadata) groupByName.get("products");
            var value = products.read(order);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isNotNull();
                soft.assertThat(value).as("value of value").isInstanceOf(Product[].class);
                soft.assertThat((Product[]) value).as("value of (Product[]) value").containsExactly(tv, radio);
            });
        }


        @Test
        @DisplayName("Should set products")
        void shouldSetProducts() {
            var tv = product("TV");
            var radio = product("Radio");
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUsers(new String[]{"Ada", "Lucas"});
            var groupByName = entityMetadata.fieldsGroupByName();
            var products = (ArrayFieldMetadata) groupByName.get("products");
            products.write(order, new Product[]{tv, radio});
            var value = order.getProducts();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isNotNull();
                soft.assertThat(value).as("value of value").isInstanceOf(Product[].class);
                soft.assertThat(value).as("value of value").containsExactly(tv, radio);
            });
        }


        @Test
        @DisplayName("Should array instance users")
        void shouldArrayInstanceUsers() {
            var groupByName = entityMetadata.fieldsGroupByName();
            var users = (ArrayFieldMetadata) groupByName.get("users");
            List<String> names = List.of("Ada", "Lucas");
            var value = users.arrayInstance(names);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isNotNull();
                soft.assertThat(value).as("value of value").isInstanceOf(String[].class);
                soft.assertThat((String[]) value).as("value of (String[]) value").containsExactly("Ada", "Lucas");
            });
        }


        @Test
        @DisplayName("Should array instance products")
        void shouldArrayInstanceProducts() {
            var tv = product("TV");
            var radio = product("Radio");
            var groupByName = entityMetadata.fieldsGroupByName();
            var products = (ArrayFieldMetadata) groupByName.get("products");
            List<Product> names = List.of(tv, radio);
            var value = products.arrayInstance(names);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isNotNull();
                soft.assertThat(value).as("value of value").isInstanceOf(Product[].class);
                soft.assertThat((Product[]) value).as("value of (Product[]) value").containsExactly(tv, radio);
            });
        }
    }

    private Product product(String name) {
        Product product = new Product();
        product.setName(name);
        return product;
    }


}
