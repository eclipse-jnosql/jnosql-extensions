/*
 *  Copyright (c) 2025 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities.record;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ArrayParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MapParameterMetaData;
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

class HotelTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Hotel.class);
    }

    @Nested
    @DisplayName("When the hotel metadata is inspected")
    class WhenTheHotelMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Hotel");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Hotel.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Hotel.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Hotel.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isTrue();
        }


        @Test
        @DisplayName("Should check constructor")
        void shouldCheckConstructor() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            assertThat(constructor.isDefault()).as("value of constructor.isDefault()").isFalse();
            List<ParameterMetaData> parameters = constructor.parameters();
            assertThat(parameters).as("value of parameters").hasSize(3);

            var document = parameters.get(0);
            var socialMedias = (MapParameterMetaData) parameters.get(1);
            var cities = (ArrayParameterMetaData) parameters.get(2);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(document.name()).as("value of document.name()").isEqualTo("_id");
                soft.assertThat(document.type()).as("value of document.type()").isEqualTo(String.class);
                soft.assertThat(document.converter()).as("value of document.converter()").isEmpty();
                soft.assertThat(document.mappingType()).as("value of document.mappingType()").isEqualTo(MappingType.DEFAULT);

                soft.assertThat(socialMedias.name()).as("value of socialMedias.name()").isEqualTo("socialMedias");
                soft.assertThat(socialMedias.type()).as("value of socialMedias.type()").isEqualTo(Map.class);
                soft.assertThat(socialMedias.mappingType()).as("value of socialMedias.mappingType()").isEqualTo(MappingType.MAP);
                soft.assertThat(socialMedias.isEmbeddable()).as("value of socialMedias.isEmbeddable()").isFalse();

                soft.assertThat(cities.name()).as("value of cities.name()").isEqualTo("cities");
                soft.assertThat(cities.type()).as("value of cities.type()").isEqualTo(String[].class);
                soft.assertThat(cities.mappingType()).as("value of cities.mappingType()").isEqualTo(MappingType.ARRAY);
                soft.assertThat(cities.isEmbeddable()).as("value of cities.isEmbeddable()").isFalse();
            });

        }
    }
}
