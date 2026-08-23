/*
 *  Copyright (c) 2022 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities.inheritance;

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import jakarta.nosql.DiscriminatorColumn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class SMSNotificationTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(SmsNotification.class);
    }

    @Nested
    @DisplayName("When the smsnotification metadata is inspected")
    class WhenTheSMSNotificationMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Notification");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(SmsNotification.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.className()).as("value of entityMetadata.className()").isEqualTo(SmsNotification.class.getName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(SmsNotification.class);
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
            SmsNotification notification = entityMetadata.newInstance();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(notification).as("value of notification").isNotNull();
                soft.assertThat(notification).as("value of notification").isInstanceOf(SmsNotification.class);
            });
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(4);
                soft.assertThat(fields.contains("id")).as("value of fields.contains(\"id\")").isTrue();
                soft.assertThat(fields.contains("name")).as("value of fields.contains(\"name\")").isTrue();
                soft.assertThat(fields.contains("phone")).as("value of fields.contains(\"phone\")").isTrue();
            });
        }


        @Test
        @DisplayName("Should index mapped fields by name")
        void shouldIndexMappedFieldsByName() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(groupByName).as("value of groupByName").isNotNull();
                soft.assertThat(groupByName.get("_id")).as("value of groupByName.get(\"_id\")").isNotNull();
                soft.assertThat(groupByName.get("phone")).as("value of groupByName.get(\"phone\")").isNotNull();
            });
        }


        @Test
        @DisplayName("Should get inheritance metadata")
        void shouldGetInheritanceMetadata() {
            InheritanceMetadata inheritance = entityMetadata.inheritance()
                    .orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(inheritance.discriminatorValue()).as("value of inheritance.discriminatorValue()").isEqualTo("SMS");
                soft.assertThat(inheritance.discriminatorColumn()).as("value of inheritance.discriminatorColumn()").isEqualTo(DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN);
                soft.assertThat(inheritance.entity()).as("value of inheritance.entity()").isEqualTo(SmsNotification.class);
                soft.assertThat(inheritance.parent()).as("value of inheritance.parent()").isEqualTo(Notification.class);
            });
        }
    }
}
