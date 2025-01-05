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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SMSNotificationTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(SmsNotification.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("Notification", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(SmsNotification.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(SmsNotification.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(SmsNotification.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        SmsNotification notification = entityMetadata.newInstance();
        Assertions.assertNotNull(notification);
        Assertions.assertInstanceOf(SmsNotification.class, notification);
    }

    @Test
    void shouldGetFieldsName() {
        List<String> fields = entityMetadata.fieldsName();
        Assertions.assertEquals(4, fields.size());
        Assertions.assertTrue(fields.contains("id"));
        Assertions.assertTrue(fields.contains("name"));
        Assertions.assertTrue(fields.contains("phone"));
    }

    @Test
    void shouldGetFieldsGroupByName() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Assertions.assertNotNull(groupByName);
        Assertions.assertNotNull(groupByName.get("_id"));
        Assertions.assertNotNull(groupByName.get("phone"));
    }

    @Test
    void shouldGetInheritanceMetadata() {
        InheritanceMetadata inheritance = this.entityMetadata.inheritance()
                .orElseThrow();
        Assertions.assertEquals("SMS", inheritance.discriminatorValue());
        Assertions.assertEquals(DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN, inheritance.discriminatorColumn());
        Assertions.assertEquals(SmsNotification.class, inheritance.entity());
        Assertions.assertEquals(Notification.class, inheritance.parent());
    }

}
