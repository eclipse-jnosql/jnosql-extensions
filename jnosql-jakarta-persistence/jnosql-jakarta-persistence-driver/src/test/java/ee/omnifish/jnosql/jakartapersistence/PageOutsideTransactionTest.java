/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 which accompanies this distribution.
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PageOutsideTransactionTest {

    private SeContainer cdiContainer;
    private PagePersonRepository repository;
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories =
                Set.of(PagePersonRepository.class);

        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer()
                .initialize();

        repository = cdiContainer.select(PagePersonRepository.class).get();
        entityManager = cdiContainer.select(EntityManager.class).get();

        repository.deleteAllPersons();

        Person alice = new Person();
        alice.setName("Alice");
        alice.setAge(30);
        repository.insert(alice);

        Person alicia = new Person();
        alicia.setName("Alicia");
        alicia.setAge(25);
        repository.insert(alicia);
    }

    @AfterEach
    void cleanup() {
        cdiContainer.close();
    }

    @Test
    void totalElementsAccessibleOutsideTransaction() {
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));

        assertThat(page.totalElements(), is(2L));
    }

    /**
     * A container-managed EntityManager is closed when the transaction started by the repository
     * method ends, so the page must be fully loaded by the time the method returns.
     */
    @Test
    void pageAccessibleAfterEntityManagerIsClosed() {
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));

        entityManager.close();

        assertThat(page.content().size(), is(2));
        assertThat(page.totalElements(), is(2L));
    }

    /**
     * The same, but with the transaction owned by the caller, as with {@code @Transactional} on
     * the repository. The repository method doesn't start the transaction, but it still ends
     * before the page is read.
     */
    @Test
    void pageAccessibleWhenCallerOwnsTheTransaction() {
        entityManager.getTransaction().begin();
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));
        entityManager.getTransaction().commit();

        entityManager.close();

        assertThat(page.content().size(), is(2));
        assertThat(page.totalElements(), is(2L));
    }
}
