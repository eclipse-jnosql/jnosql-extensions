/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
 *   Ondro Mihalyi
 */
package ee.omnifish.jnosql.jakartapersistence.scanner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonRepositoryTest {

    private SeContainer cdiContainer;
    private PersonRepository personRepo;

    @BeforeEach
    void init() {
        cdiContainer = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addPackages(Converters.class, EntityConverter.class)
                .addPackages(Reflections.class)
                .addExtensions(ReflectionEntityMetadataExtension.class, JakartaPersistenceExtension.class)
                .addPackages(PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class)
                .addBeanClasses(EntityManagerProducer.class)
                .initialize();
        personRepo = cdiContainer.select(PersonRepository.class).get();

        // cleanup
        getEntityManager().getTransaction().begin();
        getEntityManager().createQuery("delete from Person p").executeUpdate();
        getEntityManager().getTransaction().commit();

        getEntityManager().getTransaction().begin();
    }

    private EntityManager getEntityManager() {
        return cdiContainer.select(EntityManager.class).get();
    }

    @AfterEach
    void cleanup() {
        getEntityManager().getTransaction().commit();
        cdiContainer.close();
    }

    @Test
    void findAll() {
        final List<Person> persons = personRepo.findAll().toList();
        assertThat("queryResult", persons, is(empty()));
        System.out.println("All persons: " + persons);
    }

    @Test
    void count() {
        personRepo.insert(new Person());
        final long count = personRepo.countAll();
        assertThat(count, greaterThan(0L));
    }

    @Test
    void countByNotNull() {
        new PersonBuilder().name("Jakarta").insert(personRepo);
        final long count = personRepo.countByNameNotNull();
        assertThat(count, greaterThan(0L));
    }

    @Test
    void findByXAndYLessThanEqual() {
        final String NAME = "Jakarta";
        new PersonBuilder().name(NAME).age(35).insert(personRepo);

        final List<Person> persons = personRepo.findByNameAndAgeLessThanEqual(NAME, 50);
        assertThat(persons, is(not(empty())));
    }

    @Test
    void findByXIn() {
        final String NAME1 = "Jakarta";
        final String NAME2 = "JNoSQL";
        final String NAME3 = "Data";
        new PersonBuilder().name(NAME1).insert(personRepo);
        new PersonBuilder().name(NAME2).insert(personRepo);
        new PersonBuilder().name(NAME3).insert(personRepo);
        new PersonBuilder().name("No name").insert(personRepo);

        final List<Person> persons = personRepo.findByNameIn(Set.of(NAME1, NAME2, NAME3));
        assertThat(persons, hasSize(3));
    }

    @Test
    void findByNameIgnoreCaseNot() {
        new PersonBuilder().name("Jakarta").insert(personRepo);
        new PersonBuilder().name("Data").insert(personRepo);
        new PersonBuilder().name("No name").insert(personRepo);

        final List<Person> persons = personRepo.findByNameIgnoreCaseNot("no name");
        assertThat(persons, hasSize(2));
    }

    @Test
    void hermesParser() {
        getEntityManager().createQuery("UPDATE Person SET age = age + 1");
    }

    private class PersonBuilder {

        Person p = new Person();

        public PersonBuilder name(String name) {
            p.setName(name);
            return this;
        }

        public PersonBuilder age(long age) {
            p.setAge(age);
            return this;
        }

        public Person build() {
            return p;
        }

        public void insert(PersonRepository personRepo) {
            personRepo.insert(p);
        }
    }

}
