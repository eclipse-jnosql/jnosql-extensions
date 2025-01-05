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

class GuestTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Guest.class);
    }


    @Test
    void shouldGetName() {
        Assertions.assertEquals("Guest", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Guest.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Guest.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Guest.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertFalse(id.isPresent());
    }

    @Test
    void shouldCreateInstance() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        ConstructorBuilder constructorBuilder = ConstructorBuilder.of(constructor);
        constructorBuilder.add("Ada");
        constructorBuilder.add("2342342");
        constructorBuilder.add(List.of("1231", "12312"));
        Guest guest = constructorBuilder.build();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(guest).isNotNull();
            s.assertThat(guest.name()).isEqualTo("Ada");
            s.assertThat(guest.document()).isEqualTo("2342342");
            s.assertThat(guest.phones()).containsExactly("1231", "12312");
        });
    }

    @Test
    void shouldGetter() {
        Guest guest = new Guest("Ada", "2342342", List.of("1231", "12312"));
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata name = groupByName.get("name");
        FieldMetadata document = groupByName.get("document");
        FieldMetadata phones = groupByName.get("phones");
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(name.read(guest)).isEqualTo("Ada");
            s.assertThat(document.read(guest)).isEqualTo("2342342");
            s.assertThat(phones.read(guest)).isEqualTo(List.of("1231", "12312"));
        });
    }

    @Test
    void shouldCheckConstructor() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        org.assertj.core.api.Assertions.assertThat(constructor.isDefault()).isFalse();
        List<ParameterMetaData> parameters = constructor.parameters();
        org.assertj.core.api.Assertions.assertThat(parameters).hasSize(3);

        var name = parameters.get(0);
        var document = parameters.get(1);
        var phones = parameters.get(2);

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(name.name()).isEqualTo("name");
            soft.assertThat(name.type()).isEqualTo(String.class);
            soft.assertThat(name.converter()).isEmpty();
            soft.assertThat(name.mappingType()).isEqualTo(MappingType.DEFAULT);

            soft.assertThat(document.name()).isEqualTo("document");
            soft.assertThat(document.type()).isEqualTo(String.class);
            soft.assertThat(name.mappingType()).isEqualTo(MappingType.DEFAULT);

            soft.assertThat(phones.name()).isEqualTo("phones");
            soft.assertThat(phones.type()).isEqualTo(List.class);
            soft.assertThat(name.mappingType()).isEqualTo(MappingType.DEFAULT);
        });

    }



}
