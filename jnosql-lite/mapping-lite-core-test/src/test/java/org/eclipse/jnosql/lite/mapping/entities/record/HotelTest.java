/*
 *  Copyright (c) 2025 Otávio Santana and others
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
import org.eclipse.jnosql.mapping.metadata.ArrayParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.CollectionParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MapParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HotelTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Hotel.class);
    }


    @Test
    void shouldGetName() {
        Assertions.assertEquals("Hotel", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Hotel.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Hotel.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Hotel.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertFalse(id.isPresent());
    }


    @Test
    void shouldCheckConstructor() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        org.assertj.core.api.Assertions.assertThat(constructor.isDefault()).isFalse();
        List<ParameterMetaData> parameters = constructor.parameters();
        org.assertj.core.api.Assertions.assertThat(parameters).hasSize(3);

        var document = parameters.get(0);
        var socialMedias = (MapParameterMetaData) parameters.get(1);
        var cities = (ArrayParameterMetaData)parameters.get(2);

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(document.name()).isEqualTo("_id");
            soft.assertThat(document.type()).isEqualTo(String.class);
            soft.assertThat(document.converter()).isEmpty();
            soft.assertThat(document.mappingType()).isEqualTo(MappingType.DEFAULT);

            soft.assertThat(socialMedias.name()).isEqualTo("socialMedias");
            soft.assertThat(socialMedias.type()).isEqualTo(Map.class);
            soft.assertThat(socialMedias.mappingType()).isEqualTo(MappingType.MAP);
            soft.assertThat(socialMedias.isEmbeddable()).isFalse();

            soft.assertThat(cities.name()).isEqualTo("cities");
            soft.assertThat(cities.type()).isEqualTo(String[].class);
            soft.assertThat(cities.mappingType()).isEqualTo(MappingType.ARRAY);
            soft.assertThat(cities.isEmbeddable()).isFalse();
        });

    }
}