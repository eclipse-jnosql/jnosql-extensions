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
import org.eclipse.jnosql.extensions.sql.model.ComputerRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableWeld
 class InsertOperationRepositoryTest  extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerRepository.class, template);
    }

    @Nested
    @DisplayName("WhenInsertSingleEntity")
    class WhenInsertSingleEntity {

        @Test
        @DisplayName("Should return inserted entity when using insert")
        void shouldReturnInsertedEntity() {

            // given
            var computer = new Computer();

            // when
            var result = repository.insert(computer);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should insert entity when using insertVoid")
        void shouldInsertEntityUsingVoid() {

            // given
            var computer = new Computer();

            // when
            repository.insertVoid(computer);

            // then
            assertThat(computer).isNotNull();
        }
    }


    @Nested
    @DisplayName("WhenInsertArray")
    class WhenInsertArray {

        @Test
        @DisplayName("Should return inserted array when using insert")
        void shouldReturnInsertedArray() {

            // given
            var computers = new Computer[]{new Computer(), new Computer()};

            // when
            var result = repository.insert(computers);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should insert array when using insertVoid")
        void shouldInsertArrayUsingVoid() {

            // given
            var computers = new Computer[]{new Computer(), new Computer()};

            // when
            repository.insertVoid(computers);

            // then
            assertThat(computers).hasSize(2);
        }
    }


    @Nested
    @DisplayName("WhenInsertList")
    class WhenInsertList {

        @Test
        @DisplayName("Should return inserted list when using insert")
        void shouldReturnInsertedList() {

            // given
            var computers = List.of(new Computer(), new Computer());

            // when
            var result = repository.insert(computers);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should insert list when using insertVoid")
        void shouldInsertListUsingVoid() {

            // given
            var computers = List.of(new Computer(), new Computer());

            // when
            repository.insertVoid(computers);

            // then
            assertThat(computers).hasSize(2);
        }
    }
}
