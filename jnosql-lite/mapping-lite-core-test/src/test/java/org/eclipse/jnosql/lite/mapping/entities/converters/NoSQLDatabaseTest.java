/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities.converters;

import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class NoSQLDatabaseTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(NoSQLDatabase.class);
    }

    @Nested
    @DisplayName("When the NoSQL database value is resolved")
    class WhenTheDatabaseValueIsResolved {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("NoSQLDatabase");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(NoSQLDatabase.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.className()).as("value of entityMetadata.className()").isEqualTo(NoSQLDatabase.class.getName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(NoSQLDatabase.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id).as("value of id").isPresent();
        }


        @Test
        @DisplayName("Should have auto apply converter")
        void shouldHaveAutoApplyConverter() {
            Optional<FieldMetadata> id = entityMetadata.fieldMapping("id");
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id).as("value of id").isPresent();
                soft.assertThat(id.get().converter()).as("value of id.get().converter()").isPresent();
            });
        }


        @Test
        @DisplayName("Should define converter constructor")
        void shouldDefineConverterConstructor() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(constructor).as("value of constructor").isNotNull();
                List<ParameterMetaData> parameters = constructor.parameters();
                soft.assertThat(parameters).as("value of parameters").hasSize(2);
                ParameterMetaData parameterMetaData = parameters.getFirst();
                soft.assertThat(parameterMetaData.converter()).as("value of parameterMetaData.converter()").isPresent();
            });

        }
    }
}
