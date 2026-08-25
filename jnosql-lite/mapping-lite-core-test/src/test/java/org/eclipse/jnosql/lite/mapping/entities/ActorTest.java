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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ActorTest {


    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Actor.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("Actor", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Actor.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Actor.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Actor.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        Person person = entityMetadata.newInstance();
        org.assertj.core.api.Assertions.assertThat(person)
                .isNotNull().isInstanceOf(Person.class);
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
        Assertions.assertTrue(fields.contains("movieCharacter"));
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
    }

    @Test
    void shouldGetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
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

        Assertions.assertEquals(1L, id.read(actor));
        Assertions.assertEquals("otaviojava", username.read(actor));
        Assertions.assertEquals("otavio@java.com", email.read(actor));
        Assertions.assertEquals(List.of("Poliana", "Maria"), contacts.read(actor));
        Assertions.assertEquals(ada, pet.read(actor));
    }

    @Test
    void shouldSetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
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

        Assertions.assertEquals(1L, id.read(actor));
        Assertions.assertEquals("otaviojava", username.read(actor));
        Assertions.assertEquals("otavio@java.com", email.read(actor));
        Assertions.assertEquals(List.of("Poliana", "Maria"), contacts.read(actor));
        Assertions.assertEquals(ada, pet.read(actor));
    }

    @Test
    void shouldReturnGenerics() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata contacts = groupByName.get("contacts");
        var fieldMetadata = (CollectionFieldMetadata) contacts;
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(fieldMetadata.elementType()).isEqualTo(String.class);
            soft.assertThat(fieldMetadata.collectionInstance()).isInstanceOf(List.class);
        });
    }

    @Test
    void shouldReadEntityField(){
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata pet = groupByName.get("pet");
        assertThat(pet.isId()).isFalse();
        assertThat(pet.mappingType()).isEqualTo(MappingType.ENTITY);
    }

    @Test
    void shouldGetCustomAnnotation() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata email = groupByName.get("email");
        Optional<String> value = email.value(CustomAnnotation.class);
        assertThat(value).isNotEmpty().get().isEqualTo("email");
    }

    @Test
    void shouldMovieCharacter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        var movieCharacter = groupByName.get("movieCharacter");

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(movieCharacter).isInstanceOf(MapFieldMetadata.class);
            var mapFieldMetadata = (MapFieldMetadata) movieCharacter;
            soft.assertThat(movieCharacter.isId()).isFalse();
            soft.assertThat(movieCharacter.mappingType()).isEqualTo(MappingType.MAP);
            soft.assertThat(movieCharacter.fieldName()).isEqualTo("movieCharacter");
            soft.assertThat(mapFieldMetadata.keyType()).isEqualTo(String.class);
            soft.assertThat(mapFieldMetadata.valueType()).isEqualTo(Object.class);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldValueMap() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        var movieCharacter = groupByName.get("movieCharacter");
        var mapFieldMetadata = (MapFieldMetadata) movieCharacter;
        Object value = mapFieldMetadata.value(Value.of(Map.of("name", "Ada", "color", "black")));

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(value).isInstanceOf(Map.class);
            Map<String, Object> map = (Map<String, Object>) value;
            soft.assertThat(map.get("name")).isEqualTo("Ada");
            soft.assertThat(map.get("color")).isEqualTo("black");
        });
    }

    @Test
    void shouldGetMap() {
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

        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        var movieCharacter = groupByName.get("movieCharacter");
        var mapFieldMetadata = (MapFieldMetadata) movieCharacter;

        Object value = mapFieldMetadata.read(actor);
        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(value).isInstanceOf(Map.class);
            Map<String, Object> map = (Map<String, Object>) value;
            soft.assertThat(map.get("name")).isEqualTo("Ada");
            soft.assertThat(map.get("color")).isEqualTo("black");
        });
    }

    @Test
    void shouldSetMap() {
        Actor actor = new Actor();
        actor.setId(1L);
        actor.setUsername("otaviojava");
        actor.setEmail("otavio@java.com");
        actor.setContacts(List.of("Poliana", "Maria"));
        Animal ada = new Animal();
        ada.setName("Ada");
        ada.setColor("black");
        actor.setPet(ada);

        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        var movieCharacter = groupByName.get("movieCharacter");
        var mapFieldMetadata = (MapFieldMetadata) movieCharacter;

        mapFieldMetadata.write(actor, Map.of("name", "Ada", "color", "black"));
        SoftAssertions.assertSoftly(soft ->{
            Map<String, Object> map = actor.getMovieCharacter();
            soft.assertThat(map.get("name")).isEqualTo("Ada");
            soft.assertThat(map.get("color")).isEqualTo("black");
        });
    }
}
