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
import org.eclipse.jnosql.extensions.sql.model.ComputerInsertRepository;
import org.eclipse.jnosql.extensions.sql.model.ComputerUpdateRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
 class UpdateOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerUpdateRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerUpdateRepository.class, template);
    }

    @Nested
    @DisplayName("WhenUpdateSingleEntity")
    class WhenUpdateSingleEntity {

        @Test
        @DisplayName("Should return updated entity when using update")
        void shouldReturnUpdatedEntity() {

            // given
            var computer = Computer.of("old", 2020);
            template.insert(computer);

            computer.setModel("updated");

            // when
            var result = repository.update(computer);

            // then
            assertThat(result.getModel()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should update entity when using updateVoid")
        void shouldUpdateEntityUsingVoid() {

            // given
            var computer = Computer.of("old", 2020);
            template.insert(computer);

            computer.setModel("updated");

            // when
            repository.updateVoid(computer);

            // then
            assertThat(computer.getModel()).isEqualTo("updated");
        }
    }

    @Nested
    @DisplayName("WhenUpdateArray")
    class WhenUpdateArray {

        @Test
        @DisplayName("Should return updated array when using update")
        void shouldReturnUpdatedArray() {

            // given
            var computers = new Computer[]{
                    Computer.of("old1", 2020),
                    Computer.of("old2", 2021)
            };

            for (var c : computers) {
                template.insert(c);
                c.setModel("updated");
            }

            // when
            var result = repository.update(computers);

            // then
            assertThat(result[0].getModel()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should update array when using updateVoid")
        void shouldUpdateArrayUsingVoid() {

            // given
            var computers = new Computer[]{
                    Computer.of("old1", 2020),
                    Computer.of("old2", 2021)
            };

            for (var c : computers) {
                template.insert(c);
                c.setModel("updated");
            }

            // when
            repository.updateVoid(computers);

            // then
            assertThat(computers[1].getModel()).isEqualTo("updated");
        }
    }


    @Nested
    @DisplayName("WhenUpdateList")
    class WhenUpdateList {

        @Test
        @DisplayName("Should return updated list when using update")
        void shouldReturnUpdatedList() {

            // given
            var computers = new java.util.ArrayList<>(List.of(
                    Computer.of("old1", 2020),
                    Computer.of("old2", 2021)
            ));

            computers.forEach(template::insert);
            computers.forEach(c -> c.setModel("updated"));

            // when
            var result = repository.update(computers);

            // then
            assertThat(result.get(0).getModel()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should update list when using updateVoid")
        void shouldUpdateListUsingVoid() {

            // given
            var computers = new java.util.ArrayList<>(List.of(
                    Computer.of("old1", 2020),
                    Computer.of("old2", 2021)
            ));

            computers.forEach(template::insert);
            computers.forEach(c -> c.setModel("updated"));

            // when
            repository.updateVoid(computers);

            // then
            assertThat(computers.get(1).getModel()).isEqualTo("updated");
        }
    }
}
