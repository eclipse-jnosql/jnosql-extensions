/*
 *  Copyright (c) 2020 Otávio Santana and others
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
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Person.class);
    }

    @Nested
    @DisplayName("When the person metadata is inspected")
    class WhenThePersonMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Person");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Person.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Person.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Person.class);
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
            assertThat(person).as("value of person")
                    .isNotNull().isInstanceOf(Person.class);
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(5);
                soft.assertThat(fields.contains("id")).as("value of fields.contains(\"id\")").isTrue();
                soft.assertThat(fields.contains("username")).as("value of fields.contains(\"username\")").isTrue();
                soft.assertThat(fields.contains("email")).as("value of fields.contains(\"email\")").isTrue();
                soft.assertThat(fields.contains("contacts")).as("value of fields.contains(\"contacts\")").isTrue();
                soft.assertThat(fields.contains("pet")).as("value of fields.contains(\"pet\")").isTrue();
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
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Person person = new Person();
            person.setId(1L);
            person.setUsername("otaviojava");
            person.setEmail("otavio@java.com");
            person.setContacts(List.of("Poliana", "Maria"));
            Animal ada = new Animal();
            ada.setName("Ada");
            ada.setColor("black");
            person.setPet(ada);

            FieldMetadata id = groupByName.get("_id");
            FieldMetadata username = groupByName.get("native");
            FieldMetadata email = groupByName.get("email");
            FieldMetadata contacts = groupByName.get("contacts");
            FieldMetadata pet = groupByName.get("pet");

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.read(person)).as("value of id.read(person)").isEqualTo(1L);
                soft.assertThat(username.read(person)).as("value of username.read(person)").isEqualTo("otaviojava");
                soft.assertThat(email.read(person)).as("value of email.read(person)").isEqualTo("otavio@java.com");
                soft.assertThat(contacts.read(person)).as("value of contacts.read(person)").isEqualTo(List.of("Poliana", "Maria"));
                soft.assertThat(pet.read(person)).as("value of pet.read(person)").isEqualTo(ada);
            });
        }


        @Test
        @DisplayName("Should write mapped field values")
        void shouldWriteMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Person person = new Person();
            Animal ada = new Animal();
            ada.setName("Ada");
            ada.setColor("black");

            FieldMetadata id = groupByName.get("_id");
            FieldMetadata username = groupByName.get("native");
            FieldMetadata email = groupByName.get("email");
            FieldMetadata contacts = groupByName.get("contacts");
            FieldMetadata pet = groupByName.get("pet");

            id.write(person, 1L);
            username.write(person, "otaviojava");
            email.write(person, "otavio@java.com");
            contacts.write(person, List.of("Poliana", "Maria"));
            pet.write(person, ada);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.read(person)).as("value of id.read(person)").isEqualTo(1L);
                soft.assertThat(username.read(person)).as("value of username.read(person)").isEqualTo("otaviojava");
                soft.assertThat(email.read(person)).as("value of email.read(person)").isEqualTo("otavio@java.com");
                soft.assertThat(contacts.read(person)).as("value of contacts.read(person)").isEqualTo(List.of("Poliana", "Maria"));
                soft.assertThat(pet.read(person)).as("value of pet.read(person)").isEqualTo(ada);
            });
        }


        @Test
        @DisplayName("Should preserve collection element metadata")
        void shouldPreserveCollectionElementMetadata() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata contacts = groupByName.get("contacts");
            var fieldMetadata = (CollectionFieldMetadata) contacts;
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldMetadata.elementType()).as("value of fieldMetadata.elementType()").isEqualTo(String.class);
                soft.assertThat(fieldMetadata.collectionInstance()).as("value of fieldMetadata.collectionInstance()").isInstanceOf(List.class);
            });
        }


        @Test
        @DisplayName("Should describe the entity association")
        void shouldDescribeEntityAssociation() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata pet = groupByName.get("pet");
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(pet.isId()).as("value of pet.isId()").isFalse();
                soft.assertThat(pet.mappingType()).as("value of pet.mappingType()").isEqualTo(MappingType.ENTITY);
            });
        }


        @Test
        @DisplayName("Should expose custom annotation values")
        void shouldGetCustomAnnotation() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata email = groupByName.get("email");
            Optional<String> value = email.value(CustomAnnotation.class);
            assertThat(value).as("value of value").isNotEmpty().get().isEqualTo("email");
        }
    }
}
