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
import org.eclipse.jnosql.extensions.sql.model.ComputerSaveRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
@DisplayName("Save Operation Repository Tests")
class SaveOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerSaveRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerSaveRepository.class, template);
    }

    @Nested
    @DisplayName("WhenSaveSingleEntity")
    class WhenSaveSingleEntity {

        @Test
        @DisplayName("Should insert entity when id is not present")
        void shouldInsertEntityWhenNew() {

            // given
            var computer = Computer.of("new", 2024);

            // when
            var result = repository.save(computer);

            // then
            assertThat(result.getId()).isNotZero();
        }

        @Test
        @DisplayName("Should update entity when id already exists")
        void shouldUpdateEntityWhenExists() {

            // given
            var computer = Computer.of("old", 2020);
            template.insert(computer);

            computer.setModel("updated");

            // when
            var result = repository.save(computer);

            // then
            assertThat(result.getModel()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should save entity when using saveVoid")
        void shouldSaveEntityUsingVoid() {

            // given
            var computer = Computer.of("new", 2024);

            // when
            repository.saveVoid(computer);

            // then
            assertThat(computer.getId()).isNotZero();
        }
    }

    @Nested
    @DisplayName("WhenSaveArray")
    class WhenSaveArray {

        @Test
        @DisplayName("Should insert array when entities are new")
        void shouldInsertArrayWhenNew() {

            // given
            var computers = new Computer[]{
                    Computer.of("a", 2020),
                    Computer.of("b", 2021)
            };

            // when
            var result = repository.save(computers);

            // then
            assertThat(result[0].getId()).isNotZero();
        }

        @Test
        @DisplayName("Should update array when entities exist")
        void shouldUpdateArrayWhenExists() {

            // given
            var computers = new Computer[]{
                    Computer.of("a", 2020),
                    Computer.of("b", 2021)
            };

            for (var c : computers) {
                template.insert(c);
                c.setModel("updated");
            }

            // when
            var result = repository.save(computers);

            // then
            assertThat(result[1].getModel()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should save array when using saveVoid")
        void shouldSaveArrayUsingVoid() {

            // given
            var computers = new Computer[]{
                    Computer.of("a", 2020),
                    Computer.of("b", 2021)
            };

            // when
            repository.saveVoid(computers);

            // then
            assertThat(computers[0].getId()).isNotZero();
        }
    }

    @Nested
    @DisplayName("WhenSaveList")
    class WhenSaveList {

        @Test
        @DisplayName("Should insert list when entities are new")
        void shouldInsertListWhenNew() {

            // given
            var computers = new java.util.ArrayList<>(List.of(
                    Computer.of("a", 2020),
                    Computer.of("b", 2021)
            ));

            // when
            var result = repository.save(computers);

            // then
            assertThat(result.get(0).getId()).isNotZero();
        }

        @Test
        @DisplayName("Should update list when entities exist")
        void shouldUpdateListWhenExists() {

            // given
            var computers = new java.util.ArrayList<>(List.of(
                    Computer.of("a", 2020),
                    Computer.of("b", 2021)
            ));

            computers.forEach(template::insert);
            computers.forEach(c -> c.setModel("updated"));

            // when
            var result = repository.save(computers);

            // then
            assertThat(result.get(1).getModel()).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should save list when using saveVoid")
        void shouldSaveListUsingVoid() {

            // given
            var computers = new java.util.ArrayList<>(List.of(
                    Computer.of("a", 2020),
                    Computer.of("b", 2021)
            ));

            // when
            repository.saveVoid(computers);

            // then
            assertThat(computers.get(0).getId()).isNotZero();
        }
    }

}