/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.extensions.sql.model.ComputerRepository;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@EnableWeld
class SqlRepositoryProducerTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(
            WeldInitiator.createWeld()
                    .addBeanClasses(
                            SqlTemplateFactory.class,
                            SqlRepositoryAdapterTest.class,
                            SqlRepositoryProducer.class
                    )
                    .addPackages(true, CoreDeleteOperation.class)
    );

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    @Nested
    @DisplayName("WhenCreatingRepository")
    class WhenCreatingRepository {

        @Test
        @DisplayName("Should create repository proxy instance")
        void shouldCreateRepositoryInstance() {

            // when
            var repository = producer.get(ComputerRepository.class, template);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(repository).isNotNull();
                softly.assertThat(repository).isInstanceOf(ComputerRepository.class);
            });
        }

        @Test
        @DisplayName("Should create a dynamic proxy implementation")
        void shouldCreateDynamicProxy() {

            // when
            var repository = producer.get(ComputerRepository.class, template);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(Proxy.isProxyClass(repository.getClass())).isTrue();
            });
        }

        @Test
        @DisplayName("Should throw exception when repository class is null")
        void shouldThrowExceptionWhenRepositoryClassIsNull() {

            assertThatThrownBy(() -> producer.get(null, template))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("repository class is required");
        }

        @Test
        @DisplayName("Should throw exception when template is null")
        void shouldThrowExceptionWhenTemplateIsNull() {

            assertThatThrownBy(() -> producer.get(ComputerRepository.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("template is required");
        }
    }

    @Nested
    @DisplayName("WhenUsingRepositoryProxy")
    class WhenUsingRepositoryProxy {

        private ComputerRepository repository;

        @BeforeEach
        void setUp() {
            repository = producer.get(ComputerRepository.class, template);
        }

        @Test
        @DisplayName("Should save entity through proxy")
        void shouldSaveEntityThroughProxy() {

            // given
            var computer = Computer.of("MacBook Pro", 2023);

            // when
            var saved = repository.save(computer);

            // then
            var result = repository.findById(saved.getId()).orElseThrow();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(saved.getId()).isNotNull();
                softly.assertThat(result.getModel()).isEqualTo("MacBook Pro");
            });

            // cleanup
            repository.deleteById(saved.getId());
        }

        @Test
        @DisplayName("Should update entity through proxy")
        void shouldUpdateEntityThroughProxy() {

            // given
            var computer = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            computer.setModel("MacBook Pro M3");
            var updated = repository.save(computer);

            // then
            var result = repository.findById(updated.getId()).orElseThrow();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(updated.getId()).isEqualTo(computer.getId());
                softly.assertThat(result.getModel()).isEqualTo("MacBook Pro M3");
            });

            // cleanup
            repository.deleteById(updated.getId());
        }

        @Test
        @DisplayName("Should delete entity through proxy")
        void shouldDeleteEntityThroughProxy() {

            // given
            var computer = repository.save(Computer.of("MacBook Pro", 2023));

            // when
            repository.deleteById(computer.getId());

            // then
            var result = repository.findById(computer.getId());

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should find all entities through proxy")
        void shouldFindAllThroughProxy() {

            // given
            var c1 = repository.save(Computer.of("MacBook Pro", 2023));
            var c2 = repository.save(Computer.of("ThinkPad", 2022));

            // when
            var result = repository.findAll().toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .contains("MacBook Pro", "ThinkPad");
            });

            // cleanup (important: isolate test)
            repository.deleteById(c1.getId());
            repository.deleteById(c2.getId());
        }
    }
}