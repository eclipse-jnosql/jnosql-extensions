/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.graph.spi.GraphExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(LiteEntitiesMetadata.class)
@AddExtensions({GraphExtension.class})
public class GraphEntityConverterTest {

    @Inject
    private EntityConverter converter;

    private Element[] documents;

    private final Actor actor = Actor.actorBuilder().withAge()
            .withId()
            .withName()
            .withPhones(asList("234", "2342"))
            .withMovieCharacter(Collections.singletonMap("JavaZone", "Jedi"))
            .withMovieRating(Collections.singletonMap("JavaZone", 10))
            .build();

    @BeforeEach
    public void init() {

        documents = new Element[]{Element.of("_id", 12L),
                Element.of("age", 10), Element.of("name", "Otavio"),
                Element.of("phones", asList("234", "2342"))
                , Element.of("movieCharacter", Collections.singletonMap("JavaZone", "Jedi"))
                , Element.of("movieRating", Collections.singletonMap("JavaZone", 10))};
    }

    @Nested
    @DisplayName("When converting entities")
    class WhenTheConversion {

        @Test
        @DisplayName("Should convert Entity From Document Entity")
        void shouldConvertEntityFromDocumentEntity() {

            Person person = Person.builder().withAge()
                    .withId(12)
                    .withName("Otavio")
                    .withPhones(asList("234", "2342")).build();

            var entity = converter.toCommunication(person);
            assertThat(entity.name()).isEqualTo("Person");
            assertThat(entity.size()).isEqualTo(4);
            assertThat(entity.elements()).contains(Element.of("_id", 12L),
                    Element.of("age", 10), Element.of("name", "Otavio"),
                    Element.of("phones", Arrays.asList("234", "2342")));

        }

        @Test
        @DisplayName("Should convert Document Entity From Entity")
        void shouldConvertDocumentEntityFromEntity() {

            var entity = converter.toCommunication(actor);
            assertThat(entity.name()).isEqualTo("Actor");
            assertThat(entity.size()).isEqualTo(6);

            assertThat(entity.elements()).contains(documents);
        }

        @Test
        @DisplayName("Should convert Document Entity To Entity")
        void shouldConvertDocumentEntityToEntity() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);

            Actor actor = converter.toEntity(Actor.class, entity);
            assertThat(actor).isNotNull();
            assertThat(actor.getAge()).isEqualTo(10);
            assertThat(actor.getId()).isEqualTo(12L);
            assertThat(actor.getPhones()).isEqualTo(asList("234", "2342"));
            assertThat(actor.getMovieCharacter()).isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
            assertThat(actor.getMovieRating()).isEqualTo(Collections.singletonMap("JavaZone", 10));
        }

        @Test
        @DisplayName("Should convert to entity without explicit type")
        void shouldConvertToEntityWithoutExplicitType() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);

            Actor actor = converter.toEntity(entity);
            assertThat(actor).isNotNull();
            assertThat(actor.getAge()).isEqualTo(10);
            assertThat(actor.getId()).isEqualTo(12L);
            assertThat(actor.getPhones()).isEqualTo(asList("234", "2342"));
            assertThat(actor.getMovieCharacter()).isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
            assertThat(actor.getMovieRating()).isEqualTo(Collections.singletonMap("JavaZone", 10));
        }

        @Test
        @DisplayName("Should convert Document Entity To Exist Entity")
        void shouldConvertDocumentEntityToExistEntity() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);
            Actor actor = Actor.actorBuilder().build();
            Actor result = converter.toEntity(actor, entity);

            assertThat(result).isSameAs(actor);
            assertThat(actor.getAge()).isEqualTo(10);
            assertThat(actor.getId()).isEqualTo(12L);
            assertThat(actor.getPhones()).isEqualTo(asList("234", "2342"));
            assertThat(actor.getMovieCharacter()).isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
            assertThat(actor.getMovieRating()).isEqualTo(Collections.singletonMap("JavaZone", 10));
        }

        @Test
        @DisplayName("Should return an error when entity conversion receives null")
        void shouldReturnErrorWhenToEntityIsNull() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);
            Actor actor = Actor.actorBuilder().build();

            assertThatNullPointerException().isThrownBy(() -> converter.toEntity(null, entity));

            assertThatNullPointerException().isThrownBy(() -> converter.toEntity(actor, null));
        }


        @Test
        @DisplayName("Should convert an entity with an embedded movie")
        void shouldConvertEntityWithEmbeddedMovie() {

            Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
            Director director = Director.builderDirector().withAge(12)
                    .withId(12)
                    .withName("Otavio")
                    .withPhones(asList("234", "2342")).withMovie(movie).build();

            var entity = converter.toCommunication(director);
            assertThat(entity.size()).isEqualTo(5);

            assertThat(director.getName()).isEqualTo(getValue(entity.find("name")));
            assertThat(director.getAge()).isEqualTo(getValue(entity.find("age")));
            assertThat(director.getId()).isEqualTo(getValue(entity.find("_id")));
            assertThat(director.getPhones()).isEqualTo(getValue(entity.find("phones")));


            Element subDocument = entity.find("movie").get();
            List<Element> documents = subDocument.get(new TypeReference<>() {
            });

            assertThat(documents.size()).isEqualTo(3);
            assertThat(subDocument.name()).isEqualTo("movie");
            assertThat(documents.stream().filter(c -> "title".equals(c.name())).findFirst().get().get()).isEqualTo(movie.getTitle());
            assertThat(documents.stream().filter(c -> "year".equals(c.name())).findFirst().get().get()).isEqualTo(movie.getYear());
            assertThat(documents.stream().filter(c -> "actors".equals(c.name())).findFirst().get().get()).isEqualTo(movie.getActors());


        }

        @Test
        @DisplayName("Should convert an embedded class from a document")
        void shouldConvertToEmbeddedClassWhenHasSubDocument() {
            Movie movie = new Movie("Matrix", 2012, Collections.singleton("Actor"));
            Director director = Director.builderDirector().withAge(12)
                    .withId(12)
                    .withName("Otavio")
                    .withPhones(asList("234", "2342")).withMovie(movie).build();

            CommunicationEntity entity = converter.toCommunication(director);
            Director director1 = converter.toEntity(entity);

            assertThat(director1.getMovie()).isEqualTo(movie);
            assertThat(director1.getName()).isEqualTo(director.getName());
            assertThat(director1.getAge()).isEqualTo(director.getAge());
            assertThat(director1.getId()).isEqualTo(director.getId());
        }

        @Test
        @DisplayName("Should convert an embedded class from element list")
        void shouldConvertEmbeddedClassFromElementList() {
            Movie movie = new Movie("Matrix", 2012, singleton("Actor"));
            Director director = Director.builderDirector().withAge(12)
                    .withId(12)
                    .withName("Otavio")
                    .withPhones(asList("234", "2342")).withMovie(movie).build();

            var entity = converter.toCommunication(director);
            entity.remove("movie");
            entity.add(Element.of("movie", Arrays.asList(Element.of("title", "Matrix"),
                    Element.of("year", 2012), Element.of("actors", singleton("Actor")))));
            Director director1 = converter.toEntity(entity);

            assertThat(director1.getMovie()).isEqualTo(movie);
            assertThat(director1.getName()).isEqualTo(director.getName());
            assertThat(director1.getAge()).isEqualTo(director.getAge());
            assertThat(director1.getId()).isEqualTo(director.getId());
        }

        @Test
        @DisplayName("Should convert an embedded class from map")
        void shouldConvertEmbeddedClassFromMap() {
            Movie movie = new Movie("Matrix", 2012, singleton("Actor"));
            Director director = Director.builderDirector().withAge(12)
                    .withId(12)
                    .withName("Otavio")
                    .withPhones(asList("234", "2342")).withMovie(movie).build();

            var entity = converter.toCommunication(director);
            entity.remove("movie");
            Map<String, Object> map = new HashMap<>();
            map.put("title", "Matrix");
            map.put("year", 2012);
            map.put("actors", singleton("Actor"));

            entity.add(Element.of("movie", map));
            Director director1 = converter.toEntity(entity);

            assertThat(director1.getMovie()).isEqualTo(movie);
            assertThat(director1.getName()).isEqualTo(director.getName());
            assertThat(director1.getAge()).isEqualTo(director.getAge());
            assertThat(director1.getId()).isEqualTo(director.getId());
        }

        @Test
        @DisplayName("Should convert To Document When has Converter")
        void shouldConvertToDocumentWhenHaConverter() {
            Worker worker = new Worker();
            Job job = new Job();
            job.setCity("Sao Paulo");
            job.setDescription("Java Developer");
            worker.setName("Bob");
            worker.setSalary(new Money("BRL", BigDecimal.TEN));
            worker.setJob(job);
            var entity = converter.toCommunication(worker);
            assertThat(entity.name()).isEqualTo("Worker");
            assertThat(entity.find("name").get().get()).isEqualTo("Bob");
            assertThat(entity.find("city").get().get()).isEqualTo("Sao Paulo");
            assertThat(entity.find("description").get().get()).isEqualTo("Java Developer");
            assertThat(entity.find("money").get().get()).isEqualTo("BRL 10");
        }

        @Test
        @DisplayName("Should convert To Entity When Has Converter")
        void shouldConvertToEntityWhenHasConverter() {
            Worker worker = new Worker();
            Job job = new Job();
            job.setCity("Sao Paulo");
            job.setDescription("Java Developer");
            worker.setName("Bob");
            worker.setSalary(new Money("BRL", BigDecimal.TEN));
            worker.setJob(job);
            var entity = converter.toCommunication(worker);
            Worker worker1 = converter.toEntity(entity);
            assertThat(worker1.getSalary()).isEqualTo(worker.getSalary());
            assertThat(worker1.getJob().getCity()).isEqualTo(job.getCity());
            assertThat(worker1.getJob().getDescription()).isEqualTo(job.getDescription());
        }

        @Test
        @DisplayName("Should convert Embeddable Lazily")
        void shouldConvertEmbeddableLazily() {
            var entity = CommunicationEntity.of("Worker");
            entity.add("name", "Otavio");
            entity.add("money", "BRL 10");

            Worker worker = converter.toEntity(entity);
            assertThat(worker.getName()).isEqualTo("Otavio");
            assertThat(worker.getSalary()).isEqualTo(new Money("BRL", BigDecimal.TEN));
            assertThat(worker.getJob()).isNull();

        }


        @Test
        @DisplayName("Should convert To List Embeddable")
        void shouldConvertToListEmbeddable() {
            AppointmentBook appointmentBook = new AppointmentBook("ids");
            appointmentBook.add(Contact.builder().withType(ContactType.EMAIL)
                    .withName("Ada").withInformation("ada@lovelace.com").build());
            appointmentBook.add(Contact.builder().withType(ContactType.MOBILE)
                    .withName("Ada").withInformation("11 1231231 123").build());
            appointmentBook.add(Contact.builder().withType(ContactType.PHONE)
                    .withName("Ada").withInformation("12 123 1231 123123").build());

            var entity = converter.toCommunication(appointmentBook);
            var contacts = entity.find("contacts").get();
            assertThat(appointmentBook.getId()).isEqualTo("ids");
            List<List<Element>> documents = (List<List<Element>>) contacts.get();

            assertThat(documents.stream().flatMap(Collection::stream)
                    .filter(c -> c.name().equals("contact_name"))
                    .count()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should convert From List Embeddable")
        void shouldConvertFromListEmbeddable() {
            var entity = CommunicationEntity.of("AppointmentBook");
            entity.add(Element.of("_id", "ids"));
            List<List<Element>> documents = new ArrayList<>();

            documents.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.EMAIL),
                    Element.of("information", "ada@lovelace.com")));

            documents.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.MOBILE),
                    Element.of("information", "11 1231231 123")));

            documents.add(asList(Element.of("contact_name", "Ada"), Element.of("type", ContactType.PHONE),
                    Element.of("information", "phone")));

            entity.add(Element.of("contacts", documents));

            AppointmentBook appointmentBook = converter.toEntity(entity);

            List<Contact> contacts = appointmentBook.getContacts();
            assertThat(appointmentBook.getId()).isEqualTo("ids");
            assertThat(contacts.stream().map(Contact::getName).distinct().findFirst().get()).isEqualTo("Ada");

        }


        @Test
        @DisplayName("Should convert Sub Entity")
        void shouldConvertSubEntity() {
            ZipCode zipcode = new ZipCode();
            zipcode.setZip("12321");
            zipcode.setPlusFour("1234");

            Address address = new Address();
            address.setCity("Salvador");
            address.setState("Bahia");
            address.setStreet("Rua Engenheiro Jose Anasoh");
            address.setZipCode(zipcode);

            var documentEntity = converter.toCommunication(address);
            List<Element> documents = documentEntity.elements();
            assertThat(documentEntity.name()).isEqualTo("Address");
            assertThat(documents.size()).isEqualTo(4);
            List<Element> zip = documentEntity.find("zipCode").map(d -> d.get(new TypeReference<List<Element>>() {
            })).orElse(Collections.emptyList());

            assertThat(getValue(documentEntity.find("street"))).isEqualTo("Rua Engenheiro Jose Anasoh");
            assertThat(getValue(documentEntity.find("city"))).isEqualTo("Salvador");
            assertThat(getValue(documentEntity.find("state"))).isEqualTo("Bahia");
            assertThat(getValue(zip.stream().filter(d -> d.name().equals("zip")).findFirst())).isEqualTo("12321");
            assertThat(getValue(zip.stream().filter(d -> d.name().equals("plusFour")).findFirst())).isEqualTo("1234");
        }

        @Test
        @DisplayName("Should convert Document In Sub Entity")
        void shouldConvertDocumentInSubEntity() {

            var entity = CommunicationEntity.of("Address");

            entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
            entity.add(Element.of("city", "Salvador"));
            entity.add(Element.of("state", "Bahia"));
            entity.add(Element.of("zipCode", Arrays.asList(
                    Element.of("zip", "12321"),
                    Element.of("plusFour", "1234"))));
            Address address = converter.toEntity(entity);

            assertThat(address.getStreet()).isEqualTo("Rua Engenheiro Jose Anasoh");
            assertThat(address.getCity()).isEqualTo("Salvador");
            assertThat(address.getState()).isEqualTo("Bahia");
            assertThat(address.getZipCode().getZip()).isEqualTo("12321");
            assertThat(address.getZipCode().getPlusFour()).isEqualTo("1234");

        }

        @Test
        @DisplayName("Should return Null When There Is Not Sub Entity")
        void shouldReturnNullWhenThereIsNotSubEntity() {
            var entity = CommunicationEntity.of("Address");

            entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
            entity.add(Element.of("city", "Salvador"));
            entity.add(Element.of("state", "Bahia"));
            entity.add(Element.of("zip", "12321"));
            entity.add(Element.of("plusFour", "1234"));

            Address address = converter.toEntity(entity);

            assertThat(address.getStreet()).isEqualTo("Rua Engenheiro Jose Anasoh");
            assertThat(address.getCity()).isEqualTo("Salvador");
            assertThat(address.getState()).isEqualTo("Bahia");
            assertThat(address.getZipCode()).isNull();
        }

        @Test
        @DisplayName("Should convert And does not Use Unmodifiable Collection")
        void shouldConvertAndDoNotUseUnmodifiableCollection() {
            var entity = CommunicationEntity.of("vendors");
            entity.add("name", "name");
            entity.add("prefixes", Arrays.asList("value", "value2"));

            Vendor vendor = converter.toEntity(entity);
            vendor.add("value3");

            assertThat(vendor.getPrefixes().size()).isEqualTo(3);

        }

        @Test
        @DisplayName("Should convert Entity To Document With Array")
        void shouldConvertEntityToDocumentWithArray() {
            byte[] contents = {1, 2, 3, 4, 5, 6};

            var entity = CommunicationEntity.of("download");
            entity.add("_id", 1L);
            entity.add("contents", contents);

            Download download = converter.toEntity(entity);
            assertThat(download.getId()).isEqualTo(1L);
            assertThat(download.getContents()).containsExactly(contents);
        }

        @Test
        @DisplayName("Should convert Document To Entity With Array")
        void shouldConvertDocumentToEntityWithArray() {
            byte[] contents = {1, 2, 3, 4, 5, 6};

            Download download = new Download();
            download.setId(1L);
            download.setContents(contents);

            var entity = converter.toCommunication(download);

            assertThat(entity.find("_id").get().get()).isEqualTo(1L);
            final byte[] bytes = entity.find("contents").map(v -> v.get(byte[].class)).orElse(new byte[0]);
            assertThat(bytes).containsExactly(contents);
        }

        @Test
        @DisplayName("Should create user scope from a collection")
        void shouldCreateUserScope() {
            var entity = CommunicationEntity.of("UserScope");
            entity.add("_id", "userName");
            entity.add("scope", "scope");
            entity.add("properties", Collections.singletonList(Element.of("halo", "weld")));

            UserScope user = converter.toEntity(entity);
            assertThat(user).isNotNull();
            assertThat(user.getUserName()).isEqualTo("userName");
            assertThat(user.getScope()).isEqualTo("scope");
            assertThat(user.getProperties()).isEqualTo(Collections.singletonMap("halo", "weld"));

        }

        @Test
        @DisplayName("Should create user scope from an element")
        void shouldCreateUserScopeFromElement() {
            var entity = CommunicationEntity.of("UserScope");
            entity.add("_id", "userName");
            entity.add("scope", "scope");
            entity.add("properties", Element.of("halo", "weld"));

            UserScope user = converter.toEntity(entity);
            assertThat(user).isNotNull();
            assertThat(user.getUserName()).isEqualTo("userName");
            assertThat(user.getScope()).isEqualTo("scope");
            assertThat(user.getProperties()).isEqualTo(Collections.singletonMap("halo", "weld"));

        }

        @Test
        @DisplayName("Should create Lazily Entity")
        void shouldCreateLazilyEntity() {
            var entity = CommunicationEntity.of("Citizen");
            entity.add("id", "10");
            entity.add("name", "Salvador");

            Citizen citizen = converter.toEntity(entity);
            assertThat(citizen).isNotNull();
            assertThat(citizen.getCity()).isNull();
        }

        @Test
        @DisplayName("Should convert Group Embeddable")
        void shouldConvertGroupEmbeddable(){
            CommunicationEntity entity = CommunicationEntity.of("Wine");
            entity.add("_id", "id");
            entity.add("name", "Vin Blanc");
            entity.add("factory", List.of(Element.of("name", "Napa Valley Factory"),
                    Element.of("location", "Napa Valley")));

            Wine wine = converter.toEntity(entity);

            assertSoftly(soft ->{
                WineFactory factory = wine.getFactory();
                soft.assertThat(wine).as("wine").isNotNull();
                soft.assertThat(wine.getId()).as("wine id").isEqualTo("id");
                soft.assertThat(wine.getName()).as("wine name").isEqualTo("Vin Blanc");
                soft.assertThat(factory).as("wine factory").isNotNull();
                soft.assertThat(factory.getName()).as("factory name").isEqualTo("Napa Valley Factory");
                soft.assertThat(factory.getLocation()).as("factory location").isEqualTo("Napa Valley");
            });
        }

        @Test
        @DisplayName("Should convert Group Embeddable To Communication")
        void shouldConvertGroupEmbeddableToCommunication(){

            Wine wine = Wine.of("id", "Vin Blanc", WineFactory.of("Napa Valley Factory", "Napa Valley"));


            var communication = converter.toCommunication(wine);

            assertSoftly(soft ->{
                soft.assertThat(communication).as("communication entity").isNotNull();
                soft.assertThat(communication.name()).as("entity name").isEqualTo("Wine");
                soft.assertThat(communication.find("_id").orElseThrow().get()).as("identifier element").isEqualTo("id");
                soft.assertThat(communication.find("name").orElseThrow().get()).as("name element").isEqualTo("Vin Blanc");
                communication.find("factory").ifPresent(e -> {
                    List<Element> elements = e.get(new TypeReference<>(){});
                    soft.assertThat(elements).as("factory elements").hasSize(2);
                    soft.assertThat(elements.stream().filter(c -> "name".equals(c.name())).findFirst().orElseThrow().get())
                            .as("factory name element")
                            .isEqualTo("Napa Valley Factory");
                    soft.assertThat(elements.stream().filter(c -> "location".equals(c.name())).findFirst().orElseThrow().get())
                            .as("factory location element")
                            .isEqualTo("Napa Valley");
                });

            });
        }


    }

    private Object getValue(Optional<Element> document) {
        return document.map(Element::value).map(Value::get).orElse(null);
    }

}
