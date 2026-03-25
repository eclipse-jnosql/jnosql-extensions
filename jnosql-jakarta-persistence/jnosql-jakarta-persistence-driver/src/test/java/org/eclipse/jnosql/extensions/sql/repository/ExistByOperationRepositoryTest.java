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
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.extensions.sql.model.ComputerExistByRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
@DisplayName("ExistBy Operation Repository Tests")
class ExistByOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerExistByRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerExistByRepository.class, template);
    }

    @Nested
    @DisplayName("WhenExistsById")
    class WhenExistsById {

        @Test
        @DisplayName("Should return true when entity exists by id")
        void shouldReturnTrueWhenExistsById() {

            // given
            var computer = Computer.of("model", 2024);
            template.insert(computer);

            // when
            var result = repository.existsById(computer.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when entity does not exist by id")
        void shouldReturnFalseWhenNotExistsById() {

            // given
            long nonExistingId = 9999L;

            // when
            var result = repository.existsById(nonExistingId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("WhenExistsByModel")
    class WhenExistsByModel {

        @Test
        @DisplayName("Should return true when entity exists by model")
        void shouldReturnTrueWhenExistsByModel() {

            // given
            var computer = Computer.of("macbook", 2024);
            template.insert(computer);

            // when
            var result = repository.existsByModel("macbook");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when entity does not exist by model")
        void shouldReturnFalseWhenNotExistsByModel() {

            // given
            String model = "non-existent";

            // when
            var result = repository.existsByModel(model);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("WhenExistsByRelease")
    class WhenExistsByRelease {

        @Test
        @DisplayName("Should return true when entity exists by release")
        void shouldReturnTrueWhenExistsByRelease() {

            // given
            var computer = Computer.of("model", 2024);
            template.insert(computer);

            // when
            var result = repository.existsByRelease(2024);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when entity does not exist by release")
        void shouldReturnFalseWhenNotExistsByRelease() {

            // given
            long release = 1900;

            // when
            var result = repository.existsByRelease(release);

            // then
            assertThat(result).isFalse();
        }
    }

}