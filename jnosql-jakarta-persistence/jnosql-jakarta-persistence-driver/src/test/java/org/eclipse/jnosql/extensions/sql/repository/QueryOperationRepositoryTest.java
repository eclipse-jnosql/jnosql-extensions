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
import org.eclipse.jnosql.extensions.sql.model.ComputerQueryRepository;
import org.eclipse.jnosql.extensions.sql.model.ComputerSaveRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
@DisplayName("Query Operation Repository Tests")
class QueryOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerQueryRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerQueryRepository.class, template);
        this.template.deleteAll(Computer.class);
    }


    @Nested
    @DisplayName("WhenFindByModelUsingNamedParameter")
    class WhenFindByModelUsingNamedParameter {

        @Test
        @DisplayName("Should return matching entities when model exists")
        void shouldReturnMatchingEntities() {

            // given
            var expected = Computer.of("model-x", 2022);
            template.insert(expected);
            template.insert(Computer.of("other", 2020));

            // when
            var result = repository.findByModel("model-x");

            // then
            assertThat(result)
                    .hasSize(1)
                    .first()
                    .extracting(Computer::getModel)
                    .isEqualTo("model-x");
        }

        @Test
        @DisplayName("Should return empty list when model does not exist")
        void shouldReturnEmptyListWhenNotFound() {

            // given
            template.insert(Computer.of("model-x", 2022));

            // when
            var result = repository.findByModel("unknown");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("WhenFindAllComputers")
    class WhenFindAllComputers {

        @Test
        @DisplayName("Should return all persisted computers")
        void shouldReturnAllComputers() {

            // given
            template.insert(Computer.of("a", 2020));
            template.insert(Computer.of("b", 2021));

            // when
            var result = repository.computers();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no data exists")
        void shouldReturnEmptyListWhenNoData() {

            // when
            var result = repository.computers();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("WhenFindByModelUsingPositionalParameter")
    class WhenFindByModelUsingPositionalParameter {

        @Test
        @DisplayName("Should return matching entities when model exists")
        void shouldReturnMatchingEntities() {

            // given
            var expected = Computer.of("model-y", 2023);
            template.insert(expected);
            template.insert(Computer.of("other", 2020));

            // when
            var result = repository.findByModel2("model-y");

            // then
            assertThat(result)
                    .hasSize(1)
                    .first()
                    .extracting(Computer::getModel)
                    .isEqualTo("model-y");
        }

        @Test
        @DisplayName("Should return empty list when model does not exist")
        void shouldReturnEmptyListWhenNotFound() {

            // given
            template.insert(Computer.of("model-y", 2023));

            // when
            var result = repository.findByModel2("unknown");

            // then
            assertThat(result).isEmpty();
        }
    }


}