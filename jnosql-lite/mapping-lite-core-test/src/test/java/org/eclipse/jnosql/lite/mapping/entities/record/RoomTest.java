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
package org.eclipse.jnosql.lite.mapping.entities.record;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Room.class);
    }

    @Nested
    @DisplayName("When the room metadata is inspected")
    class WhenTheRoomMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Room");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Room.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Room.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Room.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isTrue();
        }


        @Test
        @DisplayName("Should create instance")
        void shouldCreateInstance() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            ConstructorBuilder constructorBuilder = ConstructorBuilder.of(constructor);
            constructorBuilder.add(123);
            constructorBuilder.add(new Guest("Ada", "2342342", List.of("1231", "12312")));
            Room room = constructorBuilder.build();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(room).as("value of room").isNotNull();
                s.assertThat(room.number()).as("value of room.number()").isEqualTo(123);
                s.assertThat(room.guest()).as("value of room.guest()").isEqualTo(new Guest("Ada", "2342342", List.of("1231", "12312")));
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            var guest = new Guest("Ada", "2342342", List.of("1231", "12312"));
            var room = new Room(123, guest);
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata id = groupByName.get("_id");
            SoftAssertions.assertSoftly(s -> s.assertThat(id.read(room)).as("value of id.read(room)").isEqualTo(123));
        }


        @Test
        @DisplayName("Should check constructor")
        void shouldCheckConstructor() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            assertThat(constructor.isDefault()).as("value of constructor.isDefault()").isFalse();
            List<ParameterMetaData> parameters = constructor.parameters();
            assertThat(parameters).as("value of parameters").hasSize(2);

            var number = parameters.get(0);
            var guest = parameters.get(1);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(number.name()).as("value of number.name()").isEqualTo("_id");
                soft.assertThat(number.type()).as("value of number.type()").isEqualTo(int.class);
                soft.assertThat(number.converter()).as("value of number.converter()").isEmpty();
                soft.assertThat(number.mappingType()).as("value of number.mappingType()").isEqualTo(MappingType.DEFAULT);

                soft.assertThat(guest.name()).as("value of guest.name()").isEqualTo("guest");
                soft.assertThat(guest.type()).as("value of guest.type()").isEqualTo(Guest.class);
                soft.assertThat(guest.mappingType()).as("value of guest.mappingType()").isEqualTo(MappingType.EMBEDDED);
            });
        }
    }
}
