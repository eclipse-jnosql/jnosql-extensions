/*
 *  Copyright (c) 2024 Otávio Santana and others
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
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

public class OrdersTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Orders.class);
    }

    @Nested
    @DisplayName("When the orders metadata is inspected")
    class WhenTheOrdersMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Orders");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Orders.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.className()).as("value of entityMetadata.className()").isEqualTo(Orders.class.getName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Orders.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isFalse();
        }


        @Test
        @DisplayName("Should create a new domain instance")
        void shouldCreateNewInstance() {
            Orders orders = entityMetadata.newInstance();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(orders).as("value of orders").isNotNull();
                soft.assertThat(orders).as("value of orders").isInstanceOf(Orders.class);
            });
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(2);
                soft.assertThat(fields.contains("user")).as("value of fields.contains(\"user\")").isTrue();
                soft.assertThat(fields.contains("items")).as("value of fields.contains(\"items\")").isTrue();
            });
        }


        @Test
        @DisplayName("Should index mapped fields by name")
        void shouldIndexMappedFieldsByName() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(groupByName).as("value of groupByName").isNotNull();
                soft.assertThat(groupByName.get("user")).as("value of groupByName.get(\"user\")").isNotNull();
                soft.assertThat(groupByName.get("items")).as("value of groupByName.get(\"items\")").isNotNull();
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Orders orders = new Orders();
            orders.setUser("Poliana");

            Money money = new Money("USD", BigDecimal.TEN);
            Product product = new Product();
            product.setName("table");
            product.setValue(money);
            orders.setItems(Collections.singletonList(product));


            FieldMetadata user = groupByName.get("user");
            FieldMetadata items = groupByName.get("items");

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(user.read(orders)).as("value of user.read(orders)").isEqualTo("Poliana");
                soft.assertThat(items.read(orders)).as("value of items.read(orders)").isEqualTo(Collections.singletonList(product));
            });
        }


        @Test
        @DisplayName("Should write mapped field values")
        void shouldWriteMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Orders orders = new Orders();

            Money money = new Money("USD", BigDecimal.TEN);
            Product product = new Product();
            product.setName("table");
            product.setValue(money);

            FieldMetadata user = groupByName.get("user");
            FieldMetadata items = groupByName.get("items");

            user.write(orders, "Poliana");
            items.write(orders, Collections.singletonList(product));

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(user.read(orders)).as("value of user.read(orders)").isEqualTo("Poliana");
                soft.assertThat(items.read(orders)).as("value of items.read(orders)").isEqualTo(Collections.singletonList(product));
            });
        }


        @Test
        @DisplayName("Should preserve collection element metadata")
        public void shouldPreserveCollectionElementMetadata() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata items = groupByName.get("items");
            var fieldMetadata = (CollectionFieldMetadata) items;

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldMetadata.elementType()).as("value of fieldMetadata.elementType()").isEqualTo(Product.class);
                soft.assertThat(fieldMetadata.collectionInstance()).as("value of fieldMetadata.collectionInstance()").isInstanceOf(List.class);
            });
        }


        @Test
        @DisplayName("Should return converter")
        void shouldReturnConverter() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata items = groupByName.get("items");
            var genericFieldMetadata = (CollectionFieldMetadata) items;

            Class<?> argument = genericFieldMetadata.elementType();
            EntityMetadata product = mappings.get(argument);
            FieldMetadata value = product.fieldMapping("value").get();
            Optional<AttributeConverter<Money, String>> converter = value.newConverter();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(converter).as("value of converter").isNotNull();
                soft.assertThat(converter.isPresent()).as("value of converter.isPresent()").isTrue();

                AttributeConverter<Money, String> attributeConverter = converter.get();
                soft.assertThat(converter).as("value of converter").isNotNull();
                Money money = new Money("USD", BigDecimal.TEN);
                String test = attributeConverter.convertToDatabaseColumn(money);
                soft.assertThat(test).as("value of test").isNotNull();
            });
        }
    }
}
