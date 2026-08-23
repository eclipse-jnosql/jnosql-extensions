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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkerTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Worker.class);
    }

    @Nested
    @DisplayName("When the worker metadata is inspected")
    class WhenTheWorkerMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Worker");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Worker.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.className()).as("value of entityMetadata.className()").isEqualTo(Worker.class.getName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Worker.class);
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
            Person person = entityMetadata.newInstance();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(person).as("value of person").isNotNull();
                soft.assertThat(person).as("value of person").isInstanceOf(Person.class);
            });
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(6);
                soft.assertThat(fields.contains("id")).as("value of fields.contains(\"id\")").isTrue();
                soft.assertThat(fields.contains("username")).as("value of fields.contains(\"username\")").isTrue();
                soft.assertThat(fields.contains("email")).as("value of fields.contains(\"email\")").isTrue();
                soft.assertThat(fields.contains("contacts")).as("value of fields.contains(\"contacts\")").isTrue();
                soft.assertThat(fields.contains("pet")).as("value of fields.contains(\"pet\")").isTrue();
                soft.assertThat(fields.contains("salary")).as("value of fields.contains(\"salary\")").isTrue();
            });
        }


        @Test
        @DisplayName("Should index mapped fields by name")
        void shouldIndexMappedFieldsByName() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(groupByName).as("value of groupByName").isNotNull();
                soft.assertThat(groupByName.get("_id")).as("value of groupByName.get(\"_id\")").isNotNull();
                soft.assertThat(groupByName.get("native")).as("value of groupByName.get(\"native\")").isNotNull();
                soft.assertThat(groupByName.get("email")).as("value of groupByName.get(\"email\")").isNotNull();
                soft.assertThat(groupByName.get("contacts")).as("value of groupByName.get(\"contacts\")").isNotNull();
                soft.assertThat(groupByName.get("pet")).as("value of groupByName.get(\"pet\")").isNotNull();
                soft.assertThat(groupByName.get("salary")).as("value of groupByName.get(\"salary\")").isNotNull();
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
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

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.read(worker)).as("value of id.read(worker)").isEqualTo(1L);
                soft.assertThat(username.read(worker)).as("value of username.read(worker)").isEqualTo("otaviojava");
                soft.assertThat(email.read(worker)).as("value of email.read(worker)").isEqualTo("otavio@java.com");
                soft.assertThat(contacts.read(worker)).as("value of contacts.read(worker)").isEqualTo(List.of("Poliana", "Maria"));
                soft.assertThat(pet.read(worker)).as("value of pet.read(worker)").isEqualTo(ada);
                soft.assertThat(salary.read(worker)).as("value of salary.read(worker)").isEqualTo(new Money("USD", BigDecimal.TEN));
            });
        }


        @Test
        @DisplayName("Should write mapped field values")
        void shouldWriteMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
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

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.read(worker)).as("value of id.read(worker)").isEqualTo(1L);
                soft.assertThat(username.read(worker)).as("value of username.read(worker)").isEqualTo("otaviojava");
                soft.assertThat(email.read(worker)).as("value of email.read(worker)").isEqualTo("otavio@java.com");
                soft.assertThat(contacts.read(worker)).as("value of contacts.read(worker)").isEqualTo(List.of("Poliana", "Maria"));
                soft.assertThat(pet.read(worker)).as("value of pet.read(worker)").isEqualTo(ada);
                soft.assertThat(salary.read(worker)).as("value of salary.read(worker)").isEqualTo(new Money("USD", BigDecimal.TEN));
            });
        }


        @Test
        @DisplayName("Should preserve collection element metadata")
        public void shouldPreserveCollectionElementMetadata() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata contacts = groupByName.get("contacts");
            var fieldMetadata = (CollectionFieldMetadata) contacts;
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldMetadata.elementType()).as("value of fieldMetadata.elementType()").isEqualTo(String.class);
                soft.assertThat(fieldMetadata.collectionInstance()).as("value of fieldMetadata.collectionInstance()").isInstanceOf(List.class);
            });
        }
    }
}
