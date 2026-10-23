/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Template API (select/delete) queries must accept values that are not
 * {@link Comparable} for equality (issue eclipse-jnosql/jnosql#785).
 */
public class TemplateNonComparableEqualityTest {

    private static final SocialSecurityNumber SSN = new SocialSecurityNumber("123-45-6789");

    private SeContainer cdiContainer;
    private PersonRepository personRepo;
    private PersistenceDocumentTemplate template;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(PersonRepository.class);
        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer().initialize();
        personRepo = cdiContainer.select(PersonRepository.class).get();
        template = new PersistenceDocumentTemplate(cdiContainer.select(PersistenceDatabaseManagerProvider.class).get()
                .getManager(entityManager()));

        entityManager().getTransaction().begin();
        entityManager().createQuery("delete from Person p").executeUpdate();
        entityManager().getTransaction().commit();
        entityManager().getTransaction().begin();

        Person p = new Person();
        p.setName("Jakarta");
        p.setAge(35);
        p.setSsn(SSN);
        personRepo.insert(p);
        Person other = new Person();
        other.setName("Other");
        other.setAge(20);
        other.setSsn(new SocialSecurityNumber("999-99-9999"));
        personRepo.insert(other);
    }

    @AfterEach
    void cleanup() {
        entityManager().getTransaction().commit();
        cdiContainer.close();
    }

    private EntityManager entityManager() {
        return cdiContainer.select(EntityManager.class).get();
    }

    @Test
    void selectByNonComparableEquality() {
        SelectQuery query = SelectQuery.select().from("Person").where("ssn").eq(SSN).build();
        List<Person> result = template.<Person>select(query).toList();
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), is("Jakarta"));
    }

    @Test
    void deleteByNonComparableEquality() {
        DeleteQuery query = DeleteQuery.delete().from("Person").where("ssn").eq(SSN).build();
        assertThat(template.deleteWithCount(query), is(1L));
        assertThat(personRepo.findAll().toList(), hasSize(1));
    }

    @Test
    void repositoryByNonComparableEquality() {
        assertThat(personRepo.findBySsn(SSN), hasSize(1));
    }

    @Test
    void selectByComparableEquality() {
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("name").eq("Jakarta").build()).toList(), hasSize(1));
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("age").eq(35L).build()).toList(), hasSize(1));
    }

    @Test
    void selectByNullEquality() {
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("name").eq("Nobody").build()).toList(), hasSize(0));
    }

    @Test
    void selectByRelationalAndInOperators() {
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("age").gt(30L).build()).toList(), hasSize(1));
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("age").lte(35L).build()).toList(), hasSize(2));
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("age").between(10L, 25L).build()).toList(), hasSize(1));
        assertThat(template.<Person>select(SelectQuery.select().from("Person").where("name").in(List.of("Jakarta", "Other")).build()).toList(), hasSize(2));
    }

    @Test
    void selectByNonComparableInequality() {
        SelectQuery not = SelectQuery.select().from("Person").where("ssn").not().eq(SSN).build();
        assertThat(template.<Person>select(not).toList(), hasSize(1));
    }
}
