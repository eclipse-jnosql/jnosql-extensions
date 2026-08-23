/*
 *  Copyright (c) 2023 Otávio Santana and others
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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

class ComputerTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Computer.class);
    }

    @Nested
    @DisplayName("When the computer metadata is inspected")
    class WhenTheComputerMetadataIsInspected {


        @Test
        @DisplayName("Should return as embeddable")
        void shouldReturnAsEmbeddable() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata address = groupByName.get("address");
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(address.name()).as("value of address.name()").isEqualTo("address");
                soft.assertThat(address.isId()).as("value of address.isId()").isFalse();
                soft.assertThat(address.mappingType()).as("value of address.mappingType()").isEqualTo(MappingType.EMBEDDED);
            });
        }


        @Test
        @DisplayName("Should return as entity")
        void shouldReturnAsEntity() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata fieldMetadata = groupByName.get("users");
            var users = (CollectionFieldMetadata) fieldMetadata;
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(users.name()).as("value of users.name()").isEqualTo("users");
                soft.assertThat(users.isId()).as("value of users.isId()").isFalse();
                soft.assertThat(users.isEmbeddable()).as("value of users.isEmbeddable()").isTrue();
                soft.assertThat(users.mappingType()).as("value of users.mappingType()").isEqualTo(MappingType.COLLECTION);
                soft.assertThat(users.elementType()).as("value of users.elementType()").isEqualTo(Person.class);
                soft.assertThat(users.collectionInstance()).as("value of users.collectionInstance()").isInstanceOf(List.class);
            });

        }
    }
}
