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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class RoomTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Room.class);
    }


    @Test
    void shouldGetName() {
        Assertions.assertEquals("Room", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Room.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Room.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Room.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateInstance() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        ConstructorBuilder constructorBuilder = ConstructorBuilder.of(constructor);
        constructorBuilder.add(123);
        constructorBuilder.add(new Guest("Ada", "2342342", List.of("1231", "12312")));
        Room room = constructorBuilder.build();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(room).isNotNull();
            s.assertThat(room.number()).isEqualTo(123);
            s.assertThat(room.guest()).isEqualTo(new Guest("Ada", "2342342", List.of("1231", "12312")));
        });
    }
    
    @Test
    void shouldGetter() {
        var guest = new Guest("Ada", "2342342", List.of("1231", "12312"));
        var room = new Room(123, guest);
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata id = groupByName.get("_id");
        SoftAssertions.assertSoftly(s -> s.assertThat(id.read(room)).isEqualTo(123));
    }

    @Test
    void shouldCheckConstructor() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        org.assertj.core.api.Assertions.assertThat(constructor.isDefault()).isFalse();
        List<ParameterMetaData> parameters = constructor.parameters();
        org.assertj.core.api.Assertions.assertThat(parameters).hasSize(2);

        var number = parameters.get(0);
        var guest = parameters.get(1);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(number.name()).isEqualTo("_id");
            soft.assertThat(number.type()).isEqualTo(int.class);
            soft.assertThat(number.converter()).isEmpty();
            soft.assertThat(number.mappingType()).isEqualTo(MappingType.DEFAULT);

            soft.assertThat(guest.name()).isEqualTo("guest");
            soft.assertThat(guest.type()).isEqualTo(Guest.class);
            soft.assertThat(guest.mappingType()).isEqualTo(MappingType.EMBEDDED);
        });
    }

}
