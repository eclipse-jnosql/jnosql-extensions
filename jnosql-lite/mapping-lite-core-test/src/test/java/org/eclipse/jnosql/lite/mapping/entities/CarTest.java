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

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CarTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Car.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("car", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Car.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Car.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Car.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        Car car = entityMetadata.newInstance();
        Assertions.assertNotNull(car);
        Assertions.assertInstanceOf(Car.class, car);
    }

    @Test
    void shouldGetFieldsName() {
        List<String> fields = entityMetadata.fieldsName();
        Assertions.assertEquals(2, fields.size());
        Assertions.assertTrue(fields.contains("name"));
        Assertions.assertTrue(fields.contains("model"));
    }

    @Test
    void shouldGetFieldsGroupByName() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Assertions.assertNotNull(groupByName);
        Assertions.assertNotNull(groupByName.get("_id"));
        Assertions.assertNotNull(groupByName.get("model"));
    }

    @Test
    void shouldGetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Car car = new Car();
        car.setModel("sport");
        car.setName("ferrari");

        String name = this.entityMetadata.columnField("name");
        String model = this.entityMetadata.columnField("model");
        FieldMetadata fieldName = groupByName.get(name);
        FieldMetadata fieldModel = groupByName.get(model);

        Assertions.assertEquals("sport", fieldModel.read(car));
        Assertions.assertEquals("ferrari", fieldName.read(car));
    }

    @Test
    void shouldSetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Car car = new Car();

        String name = this.entityMetadata.columnField("name");
        String model = this.entityMetadata.columnField("model");
        FieldMetadata fieldName = groupByName.get(name);
        FieldMetadata fieldModel = groupByName.get(model);

        fieldModel.write(car, "blue");
        fieldName.write(car, "ada");
        Assertions.assertEquals("blue", fieldModel.read(car));
        Assertions.assertEquals("ada", fieldName.read(car));

    }

}
