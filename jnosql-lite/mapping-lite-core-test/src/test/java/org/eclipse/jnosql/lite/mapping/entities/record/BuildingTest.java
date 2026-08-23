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

class BuildingTest {
    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Building.class);
    }

    @Nested
    @DisplayName("When the building metadata is inspected")
    class WhenTheBuildingMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Building");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Building.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Building.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Building.class);
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
            assertThat(parameters).as("value of parameters").hasSize(2);

            var id = parameters.get(0);
            var guests = (MapParameterMetaData) parameters.get(1);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.name()).as("value of id.name()").isEqualTo("_id");
                soft.assertThat(id.type()).as("value of id.type()").isEqualTo(Long.class);
                soft.assertThat(id.converter()).as("value of id.converter()").isEmpty();
                soft.assertThat(id.mappingType()).as("value of id.mappingType()").isEqualTo(MappingType.DEFAULT);

                soft.assertThat(guests.name()).as("value of guests.name()").isEqualTo("guests");
                soft.assertThat(guests.type()).as("value of guests.type()").isEqualTo(Map.class);
                soft.assertThat(guests.keyType()).as("value of guests.keyType()").isEqualTo(String.class);
                soft.assertThat(guests.valueType()).as("value of guests.valueType()").isEqualTo(Guest.class);
                soft.assertThat(guests.mappingType()).as("value of guests.mappingType()").isEqualTo(MappingType.MAP);
                soft.assertThat(guests.isEmbeddable()).as("value of guests.isEmbeddable()").isTrue();

            });

        }
    }
}
