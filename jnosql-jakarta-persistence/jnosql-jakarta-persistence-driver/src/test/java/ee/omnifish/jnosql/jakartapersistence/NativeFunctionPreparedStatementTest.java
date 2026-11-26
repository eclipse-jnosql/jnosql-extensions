/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/Apache-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * String based queries are executed by the persistence provider. These tests document which
 * functions the provider understands in the direct JPQL path.
 */
public class NativeFunctionPreparedStatementTest {

    private SeContainer cdiContainer;
    private PersistenceDocumentTemplate template;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(PersonRepository.class);
        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer().initialize();
        template = new PersistenceDocumentTemplate(cdiContainer.select(PersistenceDatabaseManagerProvider.class).get()
                .getManager(em()));
        em().getTransaction().begin();
        em().createQuery("delete from FunctionRecord r").executeUpdate();
        em().persist(new FunctionRecord(1, "Otavio", "OTAVIO", -100));
        em().persist(new FunctionRecord(2, "otavio", "JAVA", 100));
        em().persist(new FunctionRecord(3, "JAVA", "java", -250));
        em().persist(new FunctionRecord(4, "Jakarta", "JAKARTA", 500));
        em().persist(new FunctionRecord(5, "Abc", "ABC", 0));
        em().flush();
    }

    @AfterEach
    void cleanup() {
        em().getTransaction().commit();
        cdiContainer.close();
    }

    private EntityManager em() {
        return cdiContainer.select(EntityManager.class).get();
    }

    private List<Long> ids(String where, String name, Object value) {
        var statement = template.prepare("SELECT r FROM FunctionRecord r WHERE " + where, "FunctionRecord");
        statement.bind(name, value);
        return statement.<FunctionRecord>result().map(FunctionRecord::getId).toList();
    }

    @Test
    void upperWithNamedParameter() {
        assertThat(ids("UPPER(r.name) = :name", "name", "OTAVIO"), containsInAnyOrder(1L, 2L));
    }

    @Test
    void absWithNamedParameter() {
        assertThat(ids("ABS(r.balance) > :balance", "balance", 99L), containsInAnyOrder(1L, 2L, 3L, 4L));
    }

    @Test
    void lengthWithNamedParameter() {
        assertThat(ids("LENGTH(r.name) = :length", "length", 6), containsInAnyOrder(1L, 2L));
    }

    @Test
    void lowerOfParameter() {
        assertThat(ids("LOWER(r.name) = LOWER(:name)", "name", "JAVA"), containsInAnyOrder(3L));
    }

    @Test
    void leftAndRightInJpql() {
        assertThat(ids("LEFT(r.name, 3) = :value", "value", "Ota"), containsInAnyOrder(1L));
        assertThat(ids("RIGHT(r.name, 3) = :value", "value", "vio"), containsInAnyOrder(1L, 2L));
    }

    @Test
    void nestedFunctions() {
        assertThat(ids("UPPER(LEFT(r.name, 3)) = :value", "value", "OTA"), containsInAnyOrder(1L, 2L));
    }

    @Test
    void preparedStatementIsReusableWithDifferentParameters() {
        var statement = template.prepare("SELECT r FROM FunctionRecord r WHERE UPPER(r.name) = UPPER(:name)", "FunctionRecord");
        statement.bind("name", "otavio");
        assertThat(statement.<FunctionRecord>result().map(FunctionRecord::getId).toList(),
                containsInAnyOrder(1L, 2L));
        statement.bind("name", "java");
        assertThat(statement.<FunctionRecord>result().map(FunctionRecord::getId).toList(),
                containsInAnyOrder(3L));
    }

    @Test
    void positionalParameter() {
        assertThat(ids("UPPER(r.name) = ?1", "1", "ABC"), containsInAnyOrder(5L));
    }

    @Test
    void countKeepsFunctionPredicate() {
        var statement = template.prepare("SELECT COUNT(r) FROM FunctionRecord r WHERE UPPER(r.name) = :name");
        statement.bind("name", "OTAVIO");
        assertThat(statement.count(), is(2L));
    }

    @Test
    void stringUpdateWithFunction() {
        var statement = template.prepare("UPDATE FunctionRecord r SET r.balance = 0 WHERE UPPER(r.name) = :name");
        statement.bind("name", "OTAVIO");
        assertThat(statement.result().count(), is(2L));
        em().clear();
        assertThat(em().find(FunctionRecord.class, 1L).getBalance(), is(0L));
        assertThat(em().find(FunctionRecord.class, 3L).getBalance(), is(-250L));
    }
}
