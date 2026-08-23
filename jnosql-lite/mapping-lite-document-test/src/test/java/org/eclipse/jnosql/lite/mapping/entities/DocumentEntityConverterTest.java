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
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
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

import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(LiteEntitiesMetadata.class)
@AddExtensions({DocumentExtension.class})
public class DocumentEntityConverterTest {

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
    @DisplayName("When a domain entity is mapped to communication")
    class WhenTheDomainEntityIsMappedToCommunication {


        @Test
        @DisplayName("Should map a person to communication")
        void shouldMapPersonToCommunication() {

            Person person = Person.builder().withAge()
                    .withId(12)
                    .withName("Otavio")
                    .withPhones(asList("234", "2342")).build();

            var entity = converter.toCommunication(person);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entity.name()).as("value of entity.name()").isEqualTo("Person");
                soft.assertThat(entity.size()).as("value of entity.size()").isEqualTo(4);
                soft.assertThat(entity.elements()).as("value of entity.elements()").contains(Element.of("_id", 12L),
                        Element.of("age", 10), Element.of("name", "Otavio"),
                        Element.of("phones", Arrays.asList("234", "2342")));
            });

        }


        @Test
        @DisplayName("Should map an actor to communication")
        void shouldMapActorToCommunication() {

            var entity = converter.toCommunication(actor);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entity.name()).as("value of entity.name()").isEqualTo("Actor");
                soft.assertThat(entity.size()).as("value of entity.size()").isEqualTo(6);

                soft.assertThat(entity.elements()).as("value of entity.elements()").contains(documents);
            });
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
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entity.size()).as("value of entity.size()").isEqualTo(5);

                soft.assertThat(director.getName()).as("value of director.getName()").isEqualTo(getValue(entity.find("name")));
                soft.assertThat(director.getAge()).as("value of director.getAge()").isEqualTo(getValue(entity.find("age")));
                soft.assertThat(director.getId()).as("value of director.getId()").isEqualTo(getValue(entity.find("_id")));
                soft.assertThat(director.getPhones()).as("value of director.getPhones()").isEqualTo(getValue(entity.find("phones")));


                Element subDocument = entity.find("movie").get();
                List<Element> documents = subDocument.get(new TypeReference<>() {
                });

                soft.assertThat(documents.size()).as("value of documents.size()").isEqualTo(3);
                soft.assertThat(subDocument.name()).as("value of subDocument.name()").isEqualTo("movie");
                soft.assertThat(documents.stream().filter(c -> "title".equals(c.name())).findFirst().get().get()).as("value of documents.stream().filter(c -> \"title\".equals(c.name())).findFirst().get().get()").isEqualTo(movie.getTitle());
                soft.assertThat(documents.stream().filter(c -> "year".equals(c.name())).findFirst().get().get()).as("value of documents.stream().filter(c -> \"year\".equals(c.name())).findFirst().get().get()").isEqualTo(movie.getYear());
                soft.assertThat(documents.stream().filter(c -> "actors".equals(c.name())).findFirst().get().get()).as("value of documents.stream().filter(c -> \"actors\".equals(c.name())).findFirst().get().get()").isEqualTo(movie.getActors());
            });


        }


        @Test
        @DisplayName("Should apply attribute converters when mapping a worker")
        void shouldApplyAttributeConverterWhenMappingWorker() {
            Worker worker = new Worker();
            Job job = new Job();
            job.setCity("Sao Paulo");
            job.setDescription("Java Developer");
            worker.setName("Bob");
            worker.setSalary(new Money("BRL", BigDecimal.TEN));
            worker.setJob(job);
            var entity = converter.toCommunication(worker);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entity.name()).as("value of entity.name()").isEqualTo("Worker");
                soft.assertThat(entity.find("name").get().get()).as("value of entity.find(\"name\").get().get()").isEqualTo("Bob");
                soft.assertThat(entity.find("city").get().get()).as("value of entity.find(\"city\").get().get()").isEqualTo("Sao Paulo");
                soft.assertThat(entity.find("description").get().get()).as("value of entity.find(\"description\").get().get()").isEqualTo("Java Developer");
                soft.assertThat(entity.find("money").get().get()).as("value of entity.find(\"money\").get().get()").isEqualTo("BRL 10");
            });
        }


        @Test
        @DisplayName("Should convert to list embeddable")
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
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(appointmentBook.getId()).as("value of appointmentBook.getId()").isEqualTo("ids");
                List<List<Element>> documents = (List<List<Element>>) contacts.get();

                soft.assertThat(documents.stream().flatMap(Collection::stream)
                        .filter(c -> c.name().equals("contact_name"))
                        .count()).as("value of documents.stream().flatMap(Collection::stream) .filter(c -> c.name().equals(\"contact_name\")) .cou...").isEqualTo(3L);
            });
        }


        @Test
        @DisplayName("Should convert sub entity")
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
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(documentEntity.name()).as("value of documentEntity.name()").isEqualTo("Address");
                soft.assertThat(documents.size()).as("value of documents.size()").isEqualTo(4);
                List<Element> zip = documentEntity.find("zipCode").map(d -> d.get(new TypeReference<List<Element>>() {
                })).orElse(Collections.emptyList());

                soft.assertThat(getValue(documentEntity.find("street"))).as("value of getValue(documentEntity.find(\"street\"))").isEqualTo("Rua Engenheiro Jose Anasoh");
                soft.assertThat(getValue(documentEntity.find("city"))).as("value of getValue(documentEntity.find(\"city\"))").isEqualTo("Salvador");
                soft.assertThat(getValue(documentEntity.find("state"))).as("value of getValue(documentEntity.find(\"state\"))").isEqualTo("Bahia");
                soft.assertThat(getValue(zip.stream().filter(d -> d.name().equals("zip")).findFirst())).as("value of getValue(zip.stream().filter(d -> d.name().equals(\"zip\")).findFirst())").isEqualTo("12321");
                soft.assertThat(getValue(zip.stream().filter(d -> d.name().equals("plusFour")).findFirst())).as("value of getValue(zip.stream().filter(d -> d.name().equals(\"plusFour\")).findFirst())").isEqualTo("1234");
            });
        }


        @Test
        @DisplayName("Should convert document to entity with array")
        void shouldConvertDocumentToEntityWithArray() {
            byte[] contents = {1, 2, 3, 4, 5, 6};

            Download download = new Download();
            download.setId(1L);
            download.setContents(contents);

            var entity = converter.toCommunication(download);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entity.find("_id").get().get()).as("value of entity.find(\"_id\").get().get()").isEqualTo(1L);
                final byte[] bytes = entity.find("contents").map(v -> v.get(byte[].class)).orElse(new byte[0]);
                soft.assertThat(bytes).as("value of bytes").containsExactly(contents);
            });
        }


        @Test
        @DisplayName("Should convert group embeddable to communication")
        void shouldConvertGroupEmbeddableToCommunication() {

            Wine wine = Wine.of("id", "Vin Blanc", WineFactory.of("Napa Valley Factory", "Napa Valley"));


            var communication = converter.toCommunication(wine);

            assertSoftly(soft -> {
                soft.assertThat(communication).as("communication entity").isNotNull();
                soft.assertThat(communication.name()).as("entity name").isEqualTo("Wine");
                soft.assertThat(communication.find("_id").orElseThrow().get()).as("identifier element").isEqualTo("id");
                soft.assertThat(communication.find("name").orElseThrow().get()).as("name element").isEqualTo("Vin Blanc");
                communication.find("factory").ifPresent(e -> {
                    List<Element> elements = e.get(new TypeReference<>() {
                    });
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

    @Nested
    @DisplayName("When communication is mapped to a domain entity")
    class WhenTheCommunicationIsMappedToADomainEntity {


        @Test
        @DisplayName("Should map communication to an actor")
        void shouldMapCommunicationToActor() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);

            Actor actor = converter.toEntity(Actor.class, entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(actor).as("value of actor").isNotNull();
                soft.assertThat(actor.getAge()).as("value of actor.getAge()").isEqualTo(10);
                soft.assertThat(actor.getId()).as("value of actor.getId()").isEqualTo(12L);
                soft.assertThat(actor.getPhones()).as("value of actor.getPhones()").isEqualTo(asList("234", "2342"));
                soft.assertThat(actor.getMovieCharacter()).as("value of actor.getMovieCharacter()").isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
                soft.assertThat(actor.getMovieRating()).as("value of actor.getMovieRating()").isEqualTo(Collections.singletonMap("JavaZone", 10));
            });
        }


        @Test
        @DisplayName("Should convert to entity without explicit type")
        void shouldConvertToEntityWithoutExplicitType() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);

            Actor actor = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(actor).as("value of actor").isNotNull();
                soft.assertThat(actor.getAge()).as("value of actor.getAge()").isEqualTo(10);
                soft.assertThat(actor.getId()).as("value of actor.getId()").isEqualTo(12L);
                soft.assertThat(actor.getPhones()).as("value of actor.getPhones()").isEqualTo(asList("234", "2342"));
                soft.assertThat(actor.getMovieCharacter()).as("value of actor.getMovieCharacter()").isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
                soft.assertThat(actor.getMovieRating()).as("value of actor.getMovieRating()").isEqualTo(Collections.singletonMap("JavaZone", 10));
            });
        }


        @Test
        @DisplayName("Should populate an existing actor")
        void shouldPopulateExistingActor() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);
            Actor actor = Actor.actorBuilder().build();
            Actor result = converter.toEntity(actor, entity);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result).as("value of result").isSameAs(actor);
                soft.assertThat(actor.getAge()).as("value of actor.getAge()").isEqualTo(10);
                soft.assertThat(actor.getId()).as("value of actor.getId()").isEqualTo(12L);
                soft.assertThat(actor.getPhones()).as("value of actor.getPhones()").isEqualTo(asList("234", "2342"));
                soft.assertThat(actor.getMovieCharacter()).as("value of actor.getMovieCharacter()").isEqualTo(Collections.singletonMap("JavaZone", "Jedi"));
                soft.assertThat(actor.getMovieRating()).as("value of actor.getMovieRating()").isEqualTo(Collections.singletonMap("JavaZone", 10));
            });
        }


        @Test
        @DisplayName("Should reject a null target entity")
        void shouldRejectNullTargetEntity() {
            var entity = CommunicationEntity.of("Actor");
            Stream.of(documents).forEach(entity::add);

            assertThatNullPointerException()
                    .as("null target entity rejection")
                    .isThrownBy(() -> converter.toEntity(null, entity));
        }


        @Test
        @DisplayName("Should reject a null communication entity")
        void shouldRejectNullCommunicationEntity() {
            Actor actor = Actor.actorBuilder().build();

            assertThatNullPointerException()
                    .as("null communication entity rejection")
                    .isThrownBy(() -> converter.toEntity(actor, null));
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

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(director1.getMovie()).as("value of director1.getMovie()").isEqualTo(movie);
                soft.assertThat(director1.getName()).as("value of director1.getName()").isEqualTo(director.getName());
                soft.assertThat(director1.getAge()).as("value of director1.getAge()").isEqualTo(director.getAge());
                soft.assertThat(director1.getId()).as("value of director1.getId()").isEqualTo(director.getId());
            });
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

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(director1.getMovie()).as("value of director1.getMovie()").isEqualTo(movie);
                soft.assertThat(director1.getName()).as("value of director1.getName()").isEqualTo(director.getName());
                soft.assertThat(director1.getAge()).as("value of director1.getAge()").isEqualTo(director.getAge());
                soft.assertThat(director1.getId()).as("value of director1.getId()").isEqualTo(director.getId());
            });
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

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(director1.getMovie()).as("value of director1.getMovie()").isEqualTo(movie);
                soft.assertThat(director1.getName()).as("value of director1.getName()").isEqualTo(director.getName());
                soft.assertThat(director1.getAge()).as("value of director1.getAge()").isEqualTo(director.getAge());
                soft.assertThat(director1.getId()).as("value of director1.getId()").isEqualTo(director.getId());
            });
        }


        @Test
        @DisplayName("Should restore converted worker attributes")
        void shouldRestoreConvertedWorkerAttributes() {
            Worker worker = new Worker();
            Job job = new Job();
            job.setCity("Sao Paulo");
            job.setDescription("Java Developer");
            worker.setName("Bob");
            worker.setSalary(new Money("BRL", BigDecimal.TEN));
            worker.setJob(job);
            var entity = converter.toCommunication(worker);
            Worker worker1 = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(worker1.getSalary()).as("value of worker1.getSalary()").isEqualTo(worker.getSalary());
                soft.assertThat(worker1.getJob().getCity()).as("value of worker1.getJob().getCity()").isEqualTo(job.getCity());
                soft.assertThat(worker1.getJob().getDescription()).as("value of worker1.getJob().getDescription()").isEqualTo(job.getDescription());
            });
        }


        @Test
        @DisplayName("Should leave the job unset when embedded data is missing")
        void shouldLeaveMissingJobUnset() {
            var entity = CommunicationEntity.of("Worker");
            entity.add("name", "Otavio");
            entity.add("money", "BRL 10");

            Worker worker = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(worker.getName()).as("value of worker.getName()").isEqualTo("Otavio");
                soft.assertThat(worker.getSalary()).as("value of worker.getSalary()").isEqualTo(new Money("BRL", BigDecimal.TEN));
                soft.assertThat(worker.getJob()).as("value of worker.getJob()").isNull();
            });

        }


        @Test
        @DisplayName("Should convert from list embeddable")
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
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(appointmentBook.getId()).as("value of appointmentBook.getId()").isEqualTo("ids");
                soft.assertThat(contacts.stream().map(Contact::getName).distinct().findFirst().get()).as("value of contacts.stream().map(Contact::getName).distinct().findFirst().get()").isEqualTo("Ada");
            });

        }


        @Test
        @DisplayName("Should convert document in sub entity")
        void shouldConvertDocumentInSubEntity() {

            var entity = CommunicationEntity.of("Address");

            entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
            entity.add(Element.of("city", "Salvador"));
            entity.add(Element.of("state", "Bahia"));
            entity.add(Element.of("zipCode", Arrays.asList(
                    Element.of("zip", "12321"),
                    Element.of("plusFour", "1234"))));
            Address address = converter.toEntity(entity);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(address.getStreet()).as("value of address.getStreet()").isEqualTo("Rua Engenheiro Jose Anasoh");
                soft.assertThat(address.getCity()).as("value of address.getCity()").isEqualTo("Salvador");
                soft.assertThat(address.getState()).as("value of address.getState()").isEqualTo("Bahia");
                soft.assertThat(address.getZipCode().getZip()).as("value of address.getZipCode().getZip()").isEqualTo("12321");
                soft.assertThat(address.getZipCode().getPlusFour()).as("value of address.getZipCode().getPlusFour()").isEqualTo("1234");
            });

        }


        @Test
        @DisplayName("Should leave the zip code unset when embedded data is missing")
        void shouldLeaveZipCodeUnsetWhenEmbeddedDataIsMissing() {
            var entity = CommunicationEntity.of("Address");

            entity.add(Element.of("street", "Rua Engenheiro Jose Anasoh"));
            entity.add(Element.of("city", "Salvador"));
            entity.add(Element.of("state", "Bahia"));
            entity.add(Element.of("zip", "12321"));
            entity.add(Element.of("plusFour", "1234"));

            Address address = converter.toEntity(entity);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(address.getStreet()).as("value of address.getStreet()").isEqualTo("Rua Engenheiro Jose Anasoh");
                soft.assertThat(address.getCity()).as("value of address.getCity()").isEqualTo("Salvador");
                soft.assertThat(address.getState()).as("value of address.getState()").isEqualTo("Bahia");
                soft.assertThat(address.getZipCode()).as("value of address.getZipCode()").isNull();
            });
        }


        @Test
        @DisplayName("Should convert entity to document with array")
        void shouldConvertEntityToDocumentWithArray() {
            byte[] contents = {1, 2, 3, 4, 5, 6};

            var entity = CommunicationEntity.of("download");
            entity.add("_id", 1L);
            entity.add("contents", contents);

            Download download = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(download.getId()).as("value of download.getId()").isEqualTo(1L);
                soft.assertThat(download.getContents()).as("value of download.getContents()").containsExactly(contents);
            });
        }


        @Test
        @DisplayName("Should create user scope from a collection")
        void shouldCreateUserScope() {
            var entity = CommunicationEntity.of("UserScope");
            entity.add("_id", "userName");
            entity.add("scope", "scope");
            entity.add("properties", Collections.singletonList(Element.of("halo", "weld")));

            UserScope user = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(user).as("value of user").isNotNull();
                soft.assertThat(user.getUserName()).as("value of user.getUserName()").isEqualTo("userName");
                soft.assertThat(user.getScope()).as("value of user.getScope()").isEqualTo("scope");
                soft.assertThat(user.getProperties()).as("value of user.getProperties()").isEqualTo(Collections.singletonMap("halo", "weld"));
            });

        }


        @Test
        @DisplayName("Should create user scope from an element")
        void shouldCreateUserScopeFromElement() {
            var entity = CommunicationEntity.of("UserScope");
            entity.add("_id", "userName");
            entity.add("scope", "scope");
            entity.add("properties", Element.of("halo", "weld"));

            UserScope user = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(user).as("value of user").isNotNull();
                soft.assertThat(user.getUserName()).as("value of user.getUserName()").isEqualTo("userName");
                soft.assertThat(user.getScope()).as("value of user.getScope()").isEqualTo("scope");
                soft.assertThat(user.getProperties()).as("value of user.getProperties()").isEqualTo(Collections.singletonMap("halo", "weld"));
            });

        }


        @Test
        @DisplayName("Should leave the city unset when citizen data is missing")
        void shouldLeaveMissingCitizenCityUnset() {
            var entity = CommunicationEntity.of("Citizen");
            entity.add("id", "10");
            entity.add("name", "Salvador");

            Citizen citizen = converter.toEntity(entity);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(citizen).as("value of citizen").isNotNull();
                soft.assertThat(citizen.getCity()).as("value of citizen.getCity()").isNull();
            });
        }


        @Test
        @DisplayName("Should convert group embeddable")
        void shouldConvertGroupEmbeddable() {
            CommunicationEntity entity = CommunicationEntity.of("Wine");
            entity.add("_id", "id");
            entity.add("name", "Vin Blanc");
            entity.add("factory", List.of(Element.of("name", "Napa Valley Factory"),
                    Element.of("location", "Napa Valley")));

            Wine wine = converter.toEntity(entity);

            assertSoftly(soft -> {
                WineFactory factory = wine.getFactory();
                soft.assertThat(wine).as("wine").isNotNull();
                soft.assertThat(wine.getId()).as("wine id").isEqualTo("id");
                soft.assertThat(wine.getName()).as("wine name").isEqualTo("Vin Blanc");
                soft.assertThat(factory).as("wine factory").isNotNull();
                soft.assertThat(factory.getName()).as("factory name").isEqualTo("Napa Valley Factory");
                soft.assertThat(factory.getLocation()).as("factory location").isEqualTo("Napa Valley");
            });
        }
    }

    @Nested
    @DisplayName("When a mapped collection is changed")
    class WhenTheMappedCollectionIsChanged {


        @Test
        @DisplayName("Should create mutable vendor prefixes")
        void shouldCreateMutableVendorPrefixes() {
            var entity = CommunicationEntity.of("vendors");
            entity.add("name", "name");
            entity.add("prefixes", Arrays.asList("value", "value2"));

            Vendor vendor = converter.toEntity(entity);
            vendor.add("value3");

            assertThat(vendor.getPrefixes().size()).as("value of vendor.getPrefixes().size()").isEqualTo(3);

        }
    }

    private Object getValue(Optional<Element> document) {
        return document.map(Element::value).map(Value::get).orElse(null);
    }

}
