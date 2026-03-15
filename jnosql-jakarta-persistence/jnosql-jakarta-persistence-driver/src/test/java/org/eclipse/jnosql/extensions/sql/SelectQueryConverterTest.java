/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@EnableWeld
class SelectQueryConverterTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    DefaultSqlTemplateTest.class,
                    SelectQueryConverterTest.class
            )
            .build();

    @Produces
    @ApplicationScoped
    public SqlTemplate createEntityManager() {
        EntityManagerFactory persistenceUnit = Persistence.createEntityManagerFactory("testPersistenceUnit");
        EntityManager entityManager = persistenceUnit.createEntityManager();
        return DefaultSqlTemplate.of(entityManager);
    }

    @Inject
    private SqlTemplate template;

    @BeforeEach
    void setUp() {
        this.template.deleteAll(Computer.class);
    }

    @Nested
    @DisplayName("When retrieving computers using SelectQuery ordering and pagination")
    class WhenSelectWithOrderingAndPagination {

        @Test
        @DisplayName("Should return all stored computers when no ordering or pagination constraints are applied")
        void shouldReturnAllComputers() {

            template.insert(Computer.of("MacBook Pro", 2021));
            template.insert(Computer.of("ThinkPad", 2020));
            template.insert(Computer.of("XPS", 2022));

            var select = SelectQuery.select()
                    .from("Computer")
                    .build();

            var result = template.select(select).toList();

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Should return computers ordered from the oldest to the newest release")
        void shouldOrderByReleaseAscending() {

            template.insert(Computer.of("MacBook Pro", 2021));
            template.insert(Computer.of("ThinkPad", 2020));
            template.insert(Computer.of("XPS", 2022));

            var select = SelectQuery.select()
                    .from("Computer")
                    .orderBy("release")
                    .asc()
                    .build();

            var result = template.<Computer>select(select).toList();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result).hasSize(3);
                soft.assertThat(result.get(0).getRelease()).isEqualTo(2020);
                soft.assertThat(result.get(1).getRelease()).isEqualTo(2021);
            });
        }

        @Test
        @DisplayName("Should return computers ordered from the newest to the oldest release")
        void shouldOrderByReleaseDescending() {

            template.insert(Computer.of("MacBook Pro", 2021));
            template.insert(Computer.of("ThinkPad", 2020));
            template.insert(Computer.of("XPS", 2022));

            var select = SelectQuery.select()
                    .from("Computer")
                    .orderBy("release")
                    .desc()
                    .build();

            var result = template.<Computer>select(select).toList();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result).hasSize(3);
                soft.assertThat(result.get(0).getRelease()).isEqualTo(2022);
                soft.assertThat(result.get(1).getRelease()).isEqualTo(2021);
            });
        }

        @Test
        @DisplayName("Should restrict the number of returned computers when a limit constraint is defined")
        void shouldLimitResults() {

            template.insert(Computer.of("MacBook Pro", 2021));
            template.insert(Computer.of("ThinkPad", 2020));
            template.insert(Computer.of("XPS", 2022));

            var select = SelectQuery.select()
                    .from("Computer")
                    .orderBy("release")
                    .desc()
                    .limit(2)
                    .build();

            var result = template.select(select).toList();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should skip the initial result when a skip offset is applied")
        void shouldSkipFirstResult() {

            template.insert(Computer.of("MacBook Pro", 2021));
            template.insert(Computer.of("ThinkPad", 2020));
            template.insert(Computer.of("XPS", 2022));

            var select = SelectQuery.select()
                    .from("Computer")
                    .orderBy("release")
                    .desc()
                    .skip(1)
                    .build();

            var result = template.select(select).toList();

            Assertions.assertThat(result).hasSize(2);
        }
    }


}