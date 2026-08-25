/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities;

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class CarTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Car.class);
    }

    @Nested
    @DisplayName("When the car metadata is inspected")
    class WhenTheCarMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("car");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Car.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.className()).as("value of entityMetadata.className()").isEqualTo(Car.class.getName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Car.class);
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
            Car car = entityMetadata.newInstance();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(car).as("value of car").isNotNull();
                soft.assertThat(car).as("value of car").isInstanceOf(Car.class);
            });
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(2);
                soft.assertThat(fields.contains("name")).as("value of fields.contains(\"name\")").isTrue();
                soft.assertThat(fields.contains("model")).as("value of fields.contains(\"model\")").isTrue();
            });
        }


        @Test
        @DisplayName("Should index mapped fields by name")
        void shouldIndexMappedFieldsByName() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(groupByName).as("value of groupByName").isNotNull();
                soft.assertThat(groupByName.get("_id")).as("value of groupByName.get(\"_id\")").isNotNull();
                soft.assertThat(groupByName.get("model")).as("value of groupByName.get(\"model\")").isNotNull();
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Car car = new Car();
            car.setModel("sport");
            car.setName("ferrari");

            String name = entityMetadata.columnField("name");
            String model = entityMetadata.columnField("model");
            FieldMetadata fieldName = groupByName.get(name);
            FieldMetadata fieldModel = groupByName.get(model);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldModel.read(car)).as("value of fieldModel.read(car)").isEqualTo("sport");
                soft.assertThat(fieldName.read(car)).as("value of fieldName.read(car)").isEqualTo("ferrari");
            });
        }


        @Test
        @DisplayName("Should write mapped field values")
        void shouldWriteMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Car car = new Car();

            String name = entityMetadata.columnField("name");
            String model = entityMetadata.columnField("model");
            FieldMetadata fieldName = groupByName.get(name);
            FieldMetadata fieldModel = groupByName.get(model);

            fieldModel.write(car, "blue");
            fieldName.write(car, "ada");
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldModel.read(car)).as("value of fieldModel.read(car)").isEqualTo("blue");
                soft.assertThat(fieldName.read(car)).as("value of fieldName.read(car)").isEqualTo("ada");
            });

        }
    }
}
