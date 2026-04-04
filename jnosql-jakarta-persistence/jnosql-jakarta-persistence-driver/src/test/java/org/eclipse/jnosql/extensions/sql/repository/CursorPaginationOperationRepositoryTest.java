/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.extensions.sql.model.ComputerCursorPaginationRepository;
import org.eclipse.jnosql.extensions.sql.model.ComputerSaveRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
@DisplayName("Cursor Pagination Operation Repository Tests")
class CursorPaginationOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerCursorPaginationRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerCursorPaginationRepository.class, template);
    }

    @Nested
    @DisplayName("WhenUsingCursorPaginationRepository")
    class WhenUsingCursorPaginationRepository {

        @BeforeEach
        void setUp() {
            template.deleteAll(Computer.class);
        }

        @Test
        @DisplayName("Should return first page ordered by id")
        void shouldReturnFirstPageOrderedById() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("MacBook Pro", 2021));

            // when
            var page = repository.findByModel(
                    "MacBook Pro",
                    PageRequest.ofSize(2)
            );

            // then
            assertThat(page.content()).hasSize(2);
            assertThat(page.content())
                    .extracting(Computer::getId)
                    .isSorted();

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should navigate to next cursor page")
        void shouldNavigateToNextPage() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("MacBook Pro", 2021));

            var firstPage = repository.findByModel(
                    "MacBook Pro",
                    PageRequest.ofSize(2)
            );

            var nextCursor = firstPage.nextPageRequest();

            // when
            var secondPage = repository.findByModel(
                    "MacBook Pro",
                    nextCursor
            );

            // then
            assertThat(secondPage.content()).hasSize(1);

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should return empty when no match is found")
        void shouldReturnEmptyWhenNoMatch() {

            // given
            repository.save(Computer.of("ThinkPad", 2023));

            // when
            var page = repository.findByModel(
                    "MacBook Pro",
                    PageRequest.ofSize(2)
            );

            // then
            assertThat(page.content()).isEmpty();
        }

        @Test
        @DisplayName("Should behave consistently between Find and annotation-based methods")
        void shouldReturnSameResultsForBothMethods() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));

            var page1 = repository.findByModel(
                    "MacBook Pro",
                    PageRequest.ofSize(10)
            );

            var page2 = repository.model(
                    "MacBook Pro",
                    PageRequest.ofSize(10)
            );

            // then
            assertThat(page1.content())
                    .extracting(Computer::getId)
                    .containsExactlyElementsOf(
                            page2.content().stream()
                                    .map(Computer::getId)
                                    .toList()
                    );

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
        }
    }

}