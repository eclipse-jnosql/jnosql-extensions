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
import org.eclipse.jnosql.extensions.sql.model.ComputerDeleteByRepository;
import org.eclipse.jnosql.extensions.sql.model.ComputerUpdateRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
 class DeleteByOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerDeleteByRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerDeleteByRepository.class, template);
    }

    @Nested
    @DisplayName("WhenUsingDeleteByRepository")
    class WhenUsingDeleteByRepository {

        @Test
        @DisplayName("Should delete entities by model")
        void shouldDeleteByModel() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("MacBook Pro", 2022));
            var c3 = repository.save(Computer.of("ThinkPad", 2023));

            // when
            repository.deleteByModel("MacBook Pro");

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getModel()).isEqualTo("ThinkPad");
            });

            // cleanup
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should delete entities by release and return count")
        void shouldDeleteByReleaseAndReturnCount() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2023));
            var c3 = repository.save(Computer.of("Dell XPS", 2022));

            // when
            int deleted = repository.deleteByRelease(2023);

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(deleted).isEqualTo(2);
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getModel()).isEqualTo("Dell XPS");
            });

            // cleanup
            repository.deleteById(c3.getId());
        }

        @Test
        @DisplayName("Should not delete when model does not match")
        void shouldNotDeleteWhenModelDoesNotMatch() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            repository.deleteByModel("NonExisting");

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getModel()).isEqualTo("MacBook Pro");
            });

            // cleanup
            repository.deleteById(c1.getId());
        }

        @Test
        @DisplayName("Should return zero when release does not match")
        void shouldReturnZeroWhenReleaseDoesNotMatch() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            int deleted = repository.deleteByRelease(1999);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(deleted).isZero();
            });

            // cleanup
            repository.deleteById(c1.getId());
        }
    }
}
