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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;


@EnableWeld
class DeleteQueryConverterTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    DefaultSqlTemplateTest.class
            )
            .build();

    @Produces
    @ApplicationScoped
    public EntityManager createEntityManager() {
        return Persistence.createEntityManagerFactory("testPersistenceUnit")
                .createEntityManager();
    }

    @Inject
    private EntityManager entityManager;

    private SqlTemplate template;

    @BeforeEach
    void setUp() {
        this.template = DefaultSqlTemplate.of(entityManager);
        this.template.deleteAll(Computer.class);
    }

    @Nested
    @DisplayName("WhenDeletingWithoutCondition")
    class WhenDeletingWithoutCondition {

        @Test
        @DisplayName("Should delete all entities when no condition is provided")
        void shouldDeleteAllEntitiesWhenNoConditionIsProvided() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .build();

            // when
            template.delete(query);

            var remaining = template.select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining).isEmpty();
            });
        }
    }

    @Nested
    @DisplayName("WhenDeletingWithConditions")
    class WhenDeletingWithConditions {

        @Test
        @DisplayName("Should delete entities matching equals condition")
        void shouldDeleteEntitiesMatchingEqualsCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("release")
                    .eq(2024)
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getRelease)
                        .containsExactly(2023L);
            });
        }

        @Test
        @DisplayName("Should delete entities matching greater than condition")
        void shouldDeleteEntitiesMatchingGreaterThanCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("release")
                    .gt(2022)
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getRelease)
                        .containsExactly(2022L);
            });
        }

        @Test
        @DisplayName("Should delete entities matching between condition")
        void shouldDeleteEntitiesMatchingBetweenCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("release")
                    .between(2023, 2024)
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getRelease)
                        .containsExactly(2022L);
            });
        }

        @Test
        @DisplayName("Should delete entities matching like condition")
        void shouldDeleteEntitiesMatchingLikeCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("model")
                    .like("Mac%")
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getModel)
                        .containsExactlyInAnyOrder("ThinkPad", "XPS");
            });
        }
    }

    @Nested
    @DisplayName("WhenDeletingWithLogicalConditions")
    class WhenDeletingWithLogicalConditions {

        @Test
        @DisplayName("Should delete entities matching AND condition")
        void shouldDeleteEntitiesMatchingAndCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("MacBook", 2023));
            template.insert(Computer.of("ThinkPad", 2023));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("release")
                    .eq(2023)
                    .and("model")
                    .eq("MacBook")
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(
                    SelectQuery.select().from("Computer").build()
            ).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getModel, Computer::getRelease)
                        .containsExactlyInAnyOrder(
                                tuple("MacBook", 2024L),
                                tuple("ThinkPad", 2023L)
                        );
            });
        }

        @Test
        @DisplayName("Should delete entities matching OR condition")
        void shouldDeleteEntitiesMatchingOrCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("model")
                    .eq("MacBook")
                    .or("model")
                    .eq("ThinkPad")
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(
                    SelectQuery.select().from("Computer").build()
            ).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getModel)
                        .containsExactly("XPS");
            });
        }

        @Test
        @DisplayName("Should delete entities matching NOT condition")
        void shouldDeleteEntitiesMatchingNotCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            var query = DeleteQuery.delete()
                    .from("Computer")
                    .where("release")
                    .not()
                    .eq(2024)
                    .build();

            // when
            template.delete(query);

            var remaining = template.<Computer>select(
                    SelectQuery.select().from("Computer").build()
            ).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(remaining)
                        .extracting(Computer::getRelease)
                        .containsExactly(2024L);
            });
        }
    }


}