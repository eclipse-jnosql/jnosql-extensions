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
import org.eclipse.jnosql.mapping.metadata.CollectionParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
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

class GuestTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Guest.class);
    }

    @Nested
    @DisplayName("When the guest metadata is inspected")
    class WhenTheGuestMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Guest");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Guest.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Guest.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Guest.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isFalse();
        }


        @Test
        @DisplayName("Should create instance")
        void shouldCreateInstance() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            ConstructorBuilder constructorBuilder = ConstructorBuilder.of(constructor);
            constructorBuilder.add("Ada");
            constructorBuilder.add("2342342");
            constructorBuilder.add(List.of("1231", "12312"));
            Guest guest = constructorBuilder.build();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(guest).as("value of guest").isNotNull();
                s.assertThat(guest.name()).as("value of guest.name()").isEqualTo("Ada");
                s.assertThat(guest.document()).as("value of guest.document()").isEqualTo("2342342");
                s.assertThat(guest.phones()).as("value of guest.phones()").containsExactly("1231", "12312");
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            Guest guest = new Guest("Ada", "2342342", List.of("1231", "12312"));
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata name = groupByName.get("name");
            FieldMetadata document = groupByName.get("document");
            FieldMetadata phones = groupByName.get("phones");
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(name.read(guest)).as("value of name.read(guest)").isEqualTo("Ada");
                s.assertThat(document.read(guest)).as("value of document.read(guest)").isEqualTo("2342342");
                s.assertThat(phones.read(guest)).as("value of phones.read(guest)").isEqualTo(List.of("1231", "12312"));
            });
        }


        @Test
        @DisplayName("Should check constructor")
        void shouldCheckConstructor() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            assertThat(constructor.isDefault()).as("value of constructor.isDefault()").isFalse();
            List<ParameterMetaData> parameters = constructor.parameters();
            assertThat(parameters).as("value of parameters").hasSize(3);

            var name = parameters.get(0);
            var document = parameters.get(1);
            var phones = (CollectionParameterMetaData) parameters.get(2);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(name.name()).as("value of name.name()").isEqualTo("name");
                soft.assertThat(name.type()).as("value of name.type()").isEqualTo(String.class);
                soft.assertThat(name.converter()).as("value of name.converter()").isEmpty();
                soft.assertThat(name.mappingType()).as("value of name.mappingType()").isEqualTo(MappingType.DEFAULT);

                soft.assertThat(document.name()).as("value of document.name()").isEqualTo("document");
                soft.assertThat(document.type()).as("value of document.type()").isEqualTo(String.class);
                soft.assertThat(document.mappingType()).as("value of document.mappingType()").isEqualTo(MappingType.DEFAULT);

                soft.assertThat(phones.name()).as("value of phones.name()").isEqualTo("phones");
                soft.assertThat(phones.type()).as("value of phones.type()").isEqualTo(List.class);
                soft.assertThat(phones.mappingType()).as("value of phones.mappingType()").isEqualTo(MappingType.COLLECTION);
                soft.assertThat(phones.isEmbeddable()).as("value of phones.isEmbeddable()").isFalse();
            });

        }
    }
}
