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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableWeld
class NoSQLRepositorySqlAdapterTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    SqlTemplateFactory.class,
                    NoSQLRepositorySqlAdapterTest.class
            )
            .build();

    @Inject
    private SqlTemplate template;

    private NoSQLRepository<Computer, Long> repository;

    @Produces
    @ApplicationScoped
    public SqlTemplate createEntityManager() {
        EntityManagerFactory persistenceUnit = Persistence.createEntityManagerFactory("testPersistenceUnit");
        var entityManager = persistenceUnit.createEntityManager();
        var sqlTemplateFactory = new SqlTemplateFactory();
        return sqlTemplateFactory.create(entityManager);
    }

    @BeforeEach
    void setUp() {
        repository = new NoSQLRepositorySqlAdapter<> (Computer.class, template);
    }

    @Nested
    @DisplayName("WhenFindAllOperations")
    class WhenFindAllOperations {

        @Test
        @DisplayName("Should return all persisted entities")
        void shouldReturnAllPersistedEntities() {

            // given
            var computer1 = Computer.of("MacBook Pro", 2023);
            var computer2 = Computer.of("ThinkPad", 2022);

            repository.insert(computer1);
            repository.insert(computer2);

            // when
            var result = repository.findAll().toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsExactlyInAnyOrder("MacBook Pro", "ThinkPad");
            });
        }

        @Test
        @DisplayName("Should return paged result with sorting using Jakarta Data API")
        void shouldReturnPagedResultWithSorting() {

            // given
            repository.insert(Computer.of("MacBook Pro", 2023));
            repository.insert(Computer.of("ThinkPad", 2022));
            repository.insert(Computer.of("Dell XPS", 2021));

            var pageRequest = PageRequest.ofPage(1, 2, true);
            Order<Computer> sort = Order.by(Sort.asc("model"));

            // when
            var page = repository.findAll(pageRequest, sort);
            var content = page.content();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(content).hasSize(2);
                softly.assertThat(content)
                        .extracting(Computer::getModel)
                        .containsExactly("Dell XPS", "MacBook Pro");
                softly.assertThat(page.hasNext()).isTrue();
            });
        }

        @Test
        @DisplayName("Should throw exception when pageRequest is null")
        void shouldThrowExceptionWhenPageRequestIsNull() {

            Order<Computer> sort = Order.by(Sort.asc("model"));

            assertThatThrownBy(() -> repository.findAll(null, sort))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("pageRequest is required");
        }

        @Test
        @DisplayName("Should throw exception when sortBy is null")
        void shouldThrowExceptionWhenSortIsNull() {

            var pageRequest = PageRequest.ofPage(1, 2, true);

            assertThatThrownBy(() -> repository.findAll(pageRequest, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("sortBy is required");
        }
    }
}