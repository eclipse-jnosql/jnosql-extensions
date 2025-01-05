/*
 *  Copyright (c) 2020 Otávio Santana and others
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
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorkerTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Worker.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("Worker", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Worker.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Worker.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Worker.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        Person person = entityMetadata.newInstance();
        Assertions.assertNotNull(person);
        Assertions.assertInstanceOf(Person.class, person);
    }

    @Test
    void shouldGetFieldsName() {
        List<String> fields = entityMetadata.fieldsName();
        Assertions.assertEquals(6, fields.size());
        Assertions.assertTrue(fields.contains("id"));
        Assertions.assertTrue(fields.contains("username"));
        Assertions.assertTrue(fields.contains("email"));
        Assertions.assertTrue(fields.contains("contacts"));
        Assertions.assertTrue(fields.contains("pet"));
        Assertions.assertTrue(fields.contains("salary"));
    }

    @Test
    void shouldGetFieldsGroupByName() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Assertions.assertNotNull(groupByName);
        Assertions.assertNotNull(groupByName.get("_id"));
        Assertions.assertNotNull(groupByName.get("native"));
        Assertions.assertNotNull(groupByName.get("email"));
        Assertions.assertNotNull(groupByName.get("contacts"));
        Assertions.assertNotNull(groupByName.get("pet"));
        Assertions.assertNotNull(groupByName.get("salary"));
    }

    @Test
    void shouldGetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Worker worker = new Worker();
        worker.setId(1L);
        worker.setUsername("otaviojava");
        worker.setEmail("otavio@java.com");
        worker.setContacts(List.of("Poliana", "Maria"));
        Animal ada = new Animal();
        ada.setName("Ada");
        ada.setColor("black");
        worker.setPet(ada);
        worker.setSalary(new Money("USD", BigDecimal.TEN));

        FieldMetadata id = groupByName.get("_id");
        FieldMetadata username = groupByName.get("native");
        FieldMetadata email = groupByName.get("email");
        FieldMetadata contacts = groupByName.get("contacts");
        FieldMetadata pet = groupByName.get("pet");
        FieldMetadata salary = groupByName.get("salary");

        Assertions.assertEquals(1L, id.read(worker));
        Assertions.assertEquals("otaviojava", username.read(worker));
        Assertions.assertEquals("otavio@java.com", email.read(worker));
        Assertions.assertEquals(List.of("Poliana", "Maria"), contacts.read(worker));
        Assertions.assertEquals(ada, pet.read(worker));
        Assertions.assertEquals(new Money("USD", BigDecimal.TEN), salary.read(worker));
    }

    @Test
    void shouldSetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Worker worker = new Worker();
        Animal ada = new Animal();
        ada.setName("Ada");
        ada.setColor("black");

        FieldMetadata id = groupByName.get("_id");
        FieldMetadata username = groupByName.get("native");
        FieldMetadata email = groupByName.get("email");
        FieldMetadata contacts = groupByName.get("contacts");
        FieldMetadata pet = groupByName.get("pet");
        FieldMetadata salary = groupByName.get("salary");

        id.write(worker, 1L);
        username.write(worker, "otaviojava");
        email.write(worker, "otavio@java.com");
        contacts.write(worker, List.of("Poliana", "Maria"));
        pet.write(worker, ada);
        salary.write(worker, new Money("USD", BigDecimal.TEN));

        Assertions.assertEquals(1L, id.read(worker));
        Assertions.assertEquals("otaviojava", username.read(worker));
        Assertions.assertEquals("otavio@java.com", email.read(worker));
        Assertions.assertEquals(List.of("Poliana", "Maria"), contacts.read(worker));
        Assertions.assertEquals(ada, pet.read(worker));
        Assertions.assertEquals(new Money("USD", BigDecimal.TEN), salary.read(worker));
    }

   @Test
    public void shouldReturnGenerics() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata contacts = groupByName.get("contacts");
       var fieldMetadata = (CollectionFieldMetadata) contacts;
       SoftAssertions.assertSoftly(soft -> {
           soft.assertThat(fieldMetadata.elementType()).isEqualTo(String.class);
           soft.assertThat(fieldMetadata.collectionInstance()).isInstanceOf(List.class);
       });
    }
}
