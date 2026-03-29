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
import org.eclipse.jnosql.extensions.sql.model.ComputerExistByRepository;
import org.eclipse.jnosql.extensions.sql.model.ComputerFindByRepository;
import org.eclipse.jnosql.extensions.sql.model._Computer;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
@DisplayName("FindBy Operation Repository Tests")
class FindByOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerFindByRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerFindByRepository.class, template);
    }


    @Nested
    @DisplayName("WhenUsingFindByRepository")
    class WhenUsingFindByRepository {

        @Test
        @DisplayName("Should find entities by model")
        void shouldFindByModel() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("ThinkPad", 2023));

            // when
            var result = repository.findByModel("MacBook Pro");

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
        @DisplayName("Should find entities by release returning a set")
        void shouldFindByReleaseReturningSet() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2023));
            var c3 = repository.save(Computer.of("Dell XPS", 2022));

            // when
            var result = repository.findByRelease(2023);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getRelease)
                        .containsOnly(2023L);
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should find by model with additional restriction")
        void shouldFindByModelWithRestriction() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("MacBook Pro", 2021));

            // when

            var result = repository.findByModel(
                    "MacBook Pro",
                    _Computer.release.greaterThan(2022L)
            );

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getRelease()).isEqualTo(2023);
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should find by release with additional restriction")
        void shouldFindByReleaseWithRestriction() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2023));
            var c3 = repository.save(Computer.of("Dell XPS", 2023));

            // when
            var result = repository.findByRelease(
                    2023,
                    _Computer.model.equalTo("ThinkPad")
            );

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.getFirst().getModel()).isEqualTo("ThinkPad");
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should find by model and release")
        void shouldFindByModelAndRelease() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("ThinkPad", 2023));

            // when
            var result = repository.findByModelAndRelease("MacBook Pro", 2023);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getModel()).isEqualTo("MacBook Pro");
                softly.assertThat(result.get(0).getRelease()).isEqualTo(2023);
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
            var result = repository.findByModel("NonExisting");

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });

            // cleanup
            repository.deleteById(c1.getId());
        }
    }
}