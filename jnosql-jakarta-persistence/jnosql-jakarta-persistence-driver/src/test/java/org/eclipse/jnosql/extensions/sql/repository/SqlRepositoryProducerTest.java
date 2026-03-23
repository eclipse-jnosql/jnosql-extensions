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
import org.eclipse.jnosql.extensions.sql.model.ComputerRepository;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
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
}