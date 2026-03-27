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
import org.eclipse.jnosql.extensions.sql.model.ComputerDeleteRepository;
import org.eclipse.jnosql.extensions.sql.model._Computer;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

@EnableWeld
 class DeleteOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerDeleteRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerDeleteRepository.class, template);
        this.template.deleteAll(Computer.class);
    }

    @Nested
    @DisplayName("WhenUsingDeleteOperationRepository")
    class WhenUsingDeleteOperationRepository {

        @Test
        @DisplayName("Should delete single entity")
        void shouldDeleteSingleEntity() {

            // given
            var computer = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            repository.deleteComputer(computer);

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should delete array of entities")
        void shouldDeleteArrayOfEntities() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2022));

            // when
            repository.deleteComputer(new Computer[]{c1, c2});

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should delete list of entities")
        void shouldDeleteListOfEntities() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2022));

            // when
            repository.deleteComputer(List.of(c1, c2));

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should delete entities using restriction")
        void shouldDeleteUsingRestriction() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2022));
            var c3 = repository.save(Computer.of("Dell XPS", 2023));

            // when
            repository.deleteComputer(_Computer.release.equalTo(2023L));

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsExactly("ThinkPad");
            });

            // cleanup
            repository.deleteById(c2.getId());
        }

        @Test
        @DisplayName("Should not fail when deleting empty list")
        void shouldHandleEmptyListGracefully() {

            // when
            repository.deleteComputer(List.of());

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }
    }
}
