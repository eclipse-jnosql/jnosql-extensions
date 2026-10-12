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
import org.eclipse.jnosql.jakartapersistence.mapping.EnsureTransactionInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class PageOutsideTransactionTest {

    private SeContainer cdiContainer;
    private PagePersonRepository repository;

    @BeforeEach
    void init() {
        startContainer();
    }

    private void startContainer() {
        TestJakartaPersistenceClassScanner.standardRepositories =
                Set.of(PagePersonRepository.class, InTransactionPagePersonRepository.class);

        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer()
                .addBeanClasses(InTransactionInterceptor.class)
                .initialize();

        repository = cdiContainer.select(PagePersonRepository.class).get();

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
        System.clearProperty(EnsureTransactionInterceptor.EAGER_PAGE_PROPERTY);
    }

    private void restartWithEagerPage() {
        cdiContainer.close();
        System.setProperty(EnsureTransactionInterceptor.EAGER_PAGE_PROPERTY, "true");
        startContainer();
    }

    @Test
    void totalElementsAccessibleOutsideTransaction() {
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));

        assertThat(page.totalElements(), is(2L));
    }

    @Test
    void pageAccessibleAfterRepositoryTransactionEnds() {
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));
        closeEntityManager();

        assertThat(page.content(), hasSize(2));
        assertThat(page.totalElements(), is(2L));
    }

    // Without eager loading, a Page returned within the caller's transaction is loaded lazily,
    // so the caller reads it before the transaction ends
    @Test
    void pageReadInsideCallerTransaction() {
        EntityManager entityManager = cdiContainer.select(EntityManager.class).get();
        entityManager.getTransaction().begin();
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));
        assertThat(page.content(), hasSize(2));
        assertThat(page.totalElements(), is(2L));
        entityManager.getTransaction().commit();
        closeEntityManager();

        assertThat(page.content(), hasSize(2));
        assertThat(page.totalElements(), is(2L));
    }

    @Test
    void eagerPageAccessibleAfterCallerTransactionEnds() {
        restartWithEagerPage();
        EntityManager entityManager = cdiContainer.select(EntityManager.class).get();
        entityManager.getTransaction().begin();
        Page<Person> page =
                repository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));
        entityManager.getTransaction().commit();
        closeEntityManager();

        assertThat(page.content(), hasSize(2));
        assertThat(page.totalElements(), is(2L));
    }

    // The interceptor binding on the repository interface starts the transaction around the repository and closes
    // the EntityManager when it commits, as @Transactional and a transaction-scoped persistence context do in a
    // container. No application code runs inside that transaction, so the Page must be loaded eagerly.
    @Test
    void eagerPageAccessibleAfterRepositoryInterceptorTransactionEnds() {
        restartWithEagerPage();
        InTransactionPagePersonRepository inTransactionRepository =
                cdiContainer.select(InTransactionPagePersonRepository.class).get();
        Page<Person> page =
                inTransactionRepository.findByNameLike(
                        "Ali%",
                        PageRequest.ofPage(1).size(10));

        assertThat(page.content(), hasSize(2));
        assertThat(page.totalElements(), is(2L));
    }

    // Stands in for a container-managed, transaction-scoped EntityManager, which is closed once the transaction ends
    private void closeEntityManager() {
        cdiContainer.select(EntityManager.class).get().close();
    }
}
