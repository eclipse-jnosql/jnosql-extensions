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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;

import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

public class ActorTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Actor.class);
    }

    @Nested
    @DisplayName("When the actor metadata is inspected")
    class WhenTheActorMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Actor");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Actor.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Actor.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Actor.class);
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
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(6);
                soft.assertThat(fields.contains("id")).as("value of fields.contains(\"id\")").isTrue();
                soft.assertThat(fields.contains("username")).as("value of fields.contains(\"username\")").isTrue();
                soft.assertThat(fields.contains("email")).as("value of fields.contains(\"email\")").isTrue();
                soft.assertThat(fields.contains("contacts")).as("value of fields.contains(\"contacts\")").isTrue();
                soft.assertThat(fields.contains("pet")).as("value of fields.contains(\"pet\")").isTrue();
                soft.assertThat(fields.contains("movieCharacter")).as("value of fields.contains(\"movieCharacter\")").isTrue();
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
            Actor actor = new Actor();
            actor.setId(1L);
            actor.setUsername("otaviojava");
            actor.setEmail("otavio@java.com");
            actor.setContacts(List.of("Poliana", "Maria"));
            Animal ada = new Animal();
            ada.setName("Ada");
            ada.setColor("black");
            actor.setPet(ada);

            FieldMetadata id = groupByName.get("_id");
            FieldMetadata username = groupByName.get("native");
            FieldMetadata email = groupByName.get("email");
            FieldMetadata contacts = groupByName.get("contacts");
            FieldMetadata pet = groupByName.get("pet");

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.read(actor)).as("value of id.read(actor)").isEqualTo(1L);
                soft.assertThat(username.read(actor)).as("value of username.read(actor)").isEqualTo("otaviojava");
                soft.assertThat(email.read(actor)).as("value of email.read(actor)").isEqualTo("otavio@java.com");
                soft.assertThat(contacts.read(actor)).as("value of contacts.read(actor)").isEqualTo(List.of("Poliana", "Maria"));
                soft.assertThat(pet.read(actor)).as("value of pet.read(actor)").isEqualTo(ada);
            });
        }


        @Test
        @DisplayName("Should write mapped field values")
        void shouldWriteMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Actor actor = new Actor();
            Animal ada = new Animal();
            ada.setName("Ada");
            ada.setColor("black");

            FieldMetadata id = groupByName.get("_id");
            FieldMetadata username = groupByName.get("native");
            FieldMetadata email = groupByName.get("email");
            FieldMetadata contacts = groupByName.get("contacts");
            FieldMetadata pet = groupByName.get("pet");

            id.write(actor, 1L);
            username.write(actor, "otaviojava");
            email.write(actor, "otavio@java.com");
            contacts.write(actor, List.of("Poliana", "Maria"));
            pet.write(actor, ada);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.read(actor)).as("value of id.read(actor)").isEqualTo(1L);
                soft.assertThat(username.read(actor)).as("value of username.read(actor)").isEqualTo("otaviojava");
                soft.assertThat(email.read(actor)).as("value of email.read(actor)").isEqualTo("otavio@java.com");
                soft.assertThat(contacts.read(actor)).as("value of contacts.read(actor)").isEqualTo(List.of("Poliana", "Maria"));
                soft.assertThat(pet.read(actor)).as("value of pet.read(actor)").isEqualTo(ada);
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


        @Test
        @DisplayName("Should describe the movie character map")
        void shouldDescribeMovieCharacterMap() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            var movieCharacter = groupByName.get("movieCharacter");

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(movieCharacter).as("value of movieCharacter").isInstanceOf(MapFieldMetadata.class);
                var mapFieldMetadata = (MapFieldMetadata) movieCharacter;
                soft.assertThat(movieCharacter.isId()).as("value of movieCharacter.isId()").isFalse();
                soft.assertThat(movieCharacter.mappingType()).as("value of movieCharacter.mappingType()").isEqualTo(MappingType.MAP);
                soft.assertThat(movieCharacter.fieldName()).as("value of movieCharacter.fieldName()").isEqualTo("movieCharacter");
                soft.assertThat(mapFieldMetadata.keyType()).as("value of mapFieldMetadata.keyType()").isEqualTo(String.class);
                soft.assertThat(mapFieldMetadata.valueType()).as("value of mapFieldMetadata.valueType()").isEqualTo(Object.class);
            });
        }


        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should convert the movie character value to a map")
        void shouldConvertMovieCharacterValueToMap() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            var movieCharacter = groupByName.get("movieCharacter");
            var mapFieldMetadata = (MapFieldMetadata) movieCharacter;
            Object value = mapFieldMetadata.value(Value.of(Map.of("name", "Ada", "color", "black")));

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isInstanceOf(Map.class);
                Map<String, Object> map = (Map<String, Object>) value;
                soft.assertThat(map.get("name")).as("value of map.get(\"name\")").isEqualTo("Ada");
                soft.assertThat(map.get("color")).as("value of map.get(\"color\")").isEqualTo("black");
            });
        }


        @Test
        @DisplayName("Should read the movie character map")
        void shouldReadMovieCharacterMap() {
            Actor actor = new Actor();
            actor.setId(1L);
            actor.setUsername("otaviojava");
            actor.setEmail("otavio@java.com");
            actor.setContacts(List.of("Poliana", "Maria"));
            Animal ada = new Animal();
            ada.setName("Ada");
            ada.setColor("black");
            actor.setPet(ada);
            actor.setMovieCharacter(Map.of("name", "Ada", "color", "black"));

            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            var movieCharacter = groupByName.get("movieCharacter");
            var mapFieldMetadata = (MapFieldMetadata) movieCharacter;

            Object value = mapFieldMetadata.read(actor);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(value).as("value of value").isInstanceOf(Map.class);
                Map<String, Object> map = (Map<String, Object>) value;
                soft.assertThat(map.get("name")).as("value of map.get(\"name\")").isEqualTo("Ada");
                soft.assertThat(map.get("color")).as("value of map.get(\"color\")").isEqualTo("black");
            });
        }


        @Test
        @DisplayName("Should write the movie character map")
        void shouldWriteMovieCharacterMap() {
            Actor actor = new Actor();
            actor.setId(1L);
            actor.setUsername("otaviojava");
            actor.setEmail("otavio@java.com");
            actor.setContacts(List.of("Poliana", "Maria"));
            Animal ada = new Animal();
            ada.setName("Ada");
            ada.setColor("black");
            actor.setPet(ada);

            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            var movieCharacter = groupByName.get("movieCharacter");
            var mapFieldMetadata = (MapFieldMetadata) movieCharacter;

            mapFieldMetadata.write(actor, Map.of("name", "Ada", "color", "black"));
            SoftAssertions.assertSoftly(soft -> {
                Map<String, Object> map = actor.getMovieCharacter();
                soft.assertThat(map.get("name")).as("value of map.get(\"name\")").isEqualTo("Ada");
                soft.assertThat(map.get("color")).as("value of map.get(\"color\")").isEqualTo("black");
            });
        }
    }
}
