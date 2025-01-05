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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrdersTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Orders.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("Orders", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Orders.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Orders.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Orders.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertFalse(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        Orders orders = entityMetadata.newInstance();
        Assertions.assertNotNull(orders);
        Assertions.assertInstanceOf(Orders.class, orders);
    }

    @Test
    void shouldGetFieldsName() {
        List<String> fields = entityMetadata.fieldsName();
        Assertions.assertEquals(2, fields.size());
        Assertions.assertTrue(fields.contains("user"));
        Assertions.assertTrue(fields.contains("items"));
    }

    @Test
    void shouldGetFieldsGroupByName() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Assertions.assertNotNull(groupByName);
        Assertions.assertNotNull(groupByName.get("user"));
        Assertions.assertNotNull(groupByName.get("items"));
    }

    @Test
    void shouldGetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Orders orders = new Orders();
        orders.setUser("Poliana");

        Money money = new Money("USD", BigDecimal.TEN);
        Product product = new Product();
        product.setName("table");
        product.setValue(money);
        orders.setItems(Collections.singletonList(product));


        FieldMetadata user = groupByName.get("user");
        FieldMetadata items = groupByName.get("items");

        Assertions.assertEquals("Poliana", user.read(orders));
        Assertions.assertEquals(Collections.singletonList(product), items.read(orders));
    }

    @Test
    void shouldSetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Orders orders = new Orders();

        Money money = new Money("USD", BigDecimal.TEN);
        Product product = new Product();
        product.setName("table");
        product.setValue(money);

        FieldMetadata user = groupByName.get("user");
        FieldMetadata items = groupByName.get("items");

        user.write(orders, "Poliana");
        items.write(orders, Collections.singletonList(product));

        Assertions.assertEquals("Poliana", user.read(orders));
        Assertions.assertEquals(Collections.singletonList(product), items.read(orders));
    }

   @Test
    public void shouldReturnGenerics() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata items = groupByName.get("items");
       var fieldMetadata = (CollectionFieldMetadata) items;

       SoftAssertions.assertSoftly(soft -> {
           soft.assertThat(fieldMetadata.elementType()).isEqualTo(Product.class);
           soft.assertThat(fieldMetadata.collectionInstance()).isInstanceOf(List.class);
       });
    }

    @Test
    void shouldReturnConverter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata items = groupByName.get("items");
        var genericFieldMetadata = (CollectionFieldMetadata) items;

        Class<?> argument = genericFieldMetadata.elementType();
        EntityMetadata product = this.mappings.get(argument);
        FieldMetadata value = product.fieldMapping("value").get();
        Optional<AttributeConverter<Money, String>> converter = value.newConverter();
        Assertions.assertNotNull(converter);
        Assertions.assertTrue(converter.isPresent());

        AttributeConverter<Money, String> attributeConverter =  converter.get();
        Assertions.assertNotNull(converter);
        Money money = new Money("USD", BigDecimal.TEN);
        String test = attributeConverter.convertToDatabaseColumn(money);
        Assertions.assertNotNull(test);
    }
}
