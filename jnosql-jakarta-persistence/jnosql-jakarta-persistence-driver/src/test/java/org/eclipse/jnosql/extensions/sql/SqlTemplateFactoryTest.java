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
package org.eclipse.jnosql.extensions.sql;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


class SqlTemplateFactoryTest {

    
    private SqlTemplateFactory sqlTemplateFactory;

    @BeforeEach
    void setUp() {
        this.sqlTemplateFactory = new SqlTemplateFactory();
    }

    @Nested
    @DisplayName("WhenCreateSqlTemplate")
    class WhenCreateSqlTemplate {

        @Test
        @DisplayName("Should create SqlTemplate when EntityManager is valid")
        void shouldCreateSqlTemplateWhenEntityManagerIsValid() {

            // given
            EntityManager entityManager = Mockito.mock(EntityManager.class);

            // when
            SqlTemplate result = sqlTemplateFactory.create(entityManager);

            // then
            SoftAssertions.assertSoftly( softly -> {
                softly.assertThat(result).isNotNull();
                softly.assertThat(result).isInstanceOf(SqlTemplate.class);
            });
        }

        @Test
        @DisplayName("Should throw NullPointerException when EntityManager is null")
        void shouldThrowExceptionWhenEntityManagerIsNull() {

            // when / then
            assertThatThrownBy(() -> sqlTemplateFactory.create(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entityManager is required");
        }
    }


}