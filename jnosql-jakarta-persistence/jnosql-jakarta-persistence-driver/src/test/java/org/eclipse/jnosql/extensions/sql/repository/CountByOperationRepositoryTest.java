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
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@EnableWeld
@DisplayName("Count Operation Repository Tests")
class CountByOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerCountByRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerCountByRepository.class, template);
    }

    @Nested
    @DisplayName("WhenUsingCountByRepository")
    class WhenUsingCountByRepository {

        @BeforeEach
        void setUp() {
            template.deleteAll(Computer.class);
        }

        @Test
        @DisplayName("Should count entities by model")
        void shouldCountByModel() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("ThinkPad", 2023));

            // when
            long count = repository.countByModel("MacBook Pro");

            // then
            SoftAssertions.assertSoftly(softly -> softly.assertThat(count).isEqualTo(2));

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should count entities by release")
        void shouldCountByRelease() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2023));
            var c3 = repository.save(Computer.of("Dell XPS", 2022));

            // when
            long count = repository.countByRelease(2023);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(count).isEqualTo(2);
            });

            // cleanup
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should return zero when no entities match")
        void shouldReturnZeroWhenNoMatch() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            long count = repository.countByModel("NonExisting");

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(count).isZero();
            });

            // cleanup
            repository.deleteById(c1.getId());
        }

        @Test
        @DisplayName("Should not affect data when counting")
        void shouldNotModifyDataWhenCounting() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            long count = repository.countByModel("MacBook Pro");

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(count).isEqualTo(1);
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.getFirst().getModel()).isEqualTo("MacBook Pro");
            });

            // cleanup
            repository.deleteById(c1.getId());
        }
    }

}