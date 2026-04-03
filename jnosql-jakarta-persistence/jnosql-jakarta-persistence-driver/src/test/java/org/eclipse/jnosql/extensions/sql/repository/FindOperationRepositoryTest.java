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

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.extensions.sql.model.ComputerCountByRepository;
import org.eclipse.jnosql.extensions.sql.model.ComputerFindRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@EnableWeld
@DisplayName("Find Operation Repository Tests")
class FindOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerFindRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerFindRepository.class, template);
        template.deleteAll(Computer.class);
    }

    @Nested
    @DisplayName("WhenUsingFindRepository")
    class WhenUsingFindRepository {

        @Test
        @DisplayName("Should return all computers")
        void shouldReturnAllComputers() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2022));

            // when
            var result = repository.allComputers();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
        }

        @Test
        @DisplayName("Should find computer by id")
        void shouldFindById() {

            // given
            var computer = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            var result = repository.computersBy(computer.getId());

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.getFirst().getId()).isEqualTo(computer.getId());
            });

            // cleanup
            repository.deleteById(computer.getId());
        }

        @Test
        @DisplayName("Should find computers by model")
        void shouldFindByModel() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("ThinkPad", 2023));

            // when
            var result = repository.computersBy("MacBook Pro");

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsOnly("MacBook Pro");
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should return projection when finding by release")
        void shouldReturnProjectionByRelease() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2023));
            var c3 = repository.save(Computer.of("Dell XPS", 2022));

            // when
            var result = repository.computersByRelease(2023);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);

                softly.assertThat(result)
                        .extracting("model")
                        .containsOnly("MacBook Pro", "ThinkPad");

                softly.assertThat(result)
                        .extracting("release")
                        .containsOnly(2023L);
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should return empty when no match is found")
        void shouldReturnEmptyWhenNoMatch() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            var result = repository.computersBy("NonExisting");

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });

            // cleanup
            repository.deleteById(c1.getId());
        }
    }

}