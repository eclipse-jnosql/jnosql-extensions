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
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;


@EnableWeld
class UpdateQueryConverterTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    DefaultSqlTemplateTest.class,
                    SelectQueryConverterTest.class
            )
            .build();

    @Produces
    @ApplicationScoped
    public SqlTemplate createEntityManager() {
        EntityManagerFactory persistenceUnit = Persistence.createEntityManagerFactory("testPersistenceUnit");
        EntityManager entityManager = persistenceUnit.createEntityManager();
        return DefaultSqlTemplate.of(entityManager);
    }

    @Inject
    private SqlTemplate template;

    @BeforeEach
    void setUp() {
        this.template.deleteAll(Computer.class);
    }


    @Nested
    @DisplayName("WhenUpdatingWithoutWhereCondition")
    class WhenUpdatingWithoutWhereCondition {

        @Test
        @DisplayName("Should update all entities when no where condition is provided")
        void shouldUpdateAllEntitiesWhenNoWhereConditionIsProvided() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));

            UpdateQuery query = new UpdateStructure(
                    "Computer",
                    List.of(Element.of("model", "Updated")),
                    null
            );

            // when
            template.update(query);

            var result = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsExactlyInAnyOrder("Updated", "Updated");
            });
        }
    }

    @Nested
    @DisplayName("WhenUpdatingWithSimpleConditions")
    class WhenUpdatingWithSimpleConditions {

        @Test
        @DisplayName("Should update entities matching equals condition")
        void shouldUpdateEntitiesMatchingEqualsCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));

            UpdateQuery query = new UpdateStructure(
                    "Computer",
                    List.of(Element.of("model", "Updated")),
                    CriteriaCondition.eq(Element.of("release", 2024))
            );

            // when
            template.update(query);

            var result = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result)
                        .extracting(Computer::getModel, Computer::getRelease)
                        .containsExactlyInAnyOrder(
                                tuple("Updated", 2024L),
                                tuple("ThinkPad", 2023L)
                        );
            });
        }

        @Test
        @DisplayName("Should update entities matching greater than condition")
        void shouldUpdateEntitiesMatchingGreaterThanCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            UpdateQuery query = new UpdateStructure(
                    "Computer",
                    List.of(Element.of("model", "Modern")),
                    CriteriaCondition.gt(Element.of("release", 2022))
            );

            // when
            template.update(query);

            var result = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result)
                        .extracting(Computer::getModel, Computer::getRelease)
                        .containsExactlyInAnyOrder(
                                tuple("Modern", 2024L),
                                tuple("Modern", 2023L),
                                tuple("XPS", 2022L)
                        );
            });
        }

        @Test
        @DisplayName("Should update entities matching between condition")
        void shouldUpdateEntitiesMatchingBetweenCondition() {

            // given
            template.insert(Computer.of("MacBook", 2024));
            template.insert(Computer.of("ThinkPad", 2023));
            template.insert(Computer.of("XPS", 2022));

            UpdateQuery query = new UpdateStructure(
                    "Computer",
                    List.of(Element.of("model", "Recent")),
                    CriteriaCondition.between(Element.of("release", List.of(2023, 2024)))
            );

            // when
            template.update(query);

            var result = template.<Computer>select(SelectQuery.select().from("Computer").build()).toList();

            // then
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result)
                        .extracting(Computer::getModel, Computer::getRelease)
                        .containsExactlyInAnyOrder(
                                tuple("Recent", 2024L),
                                tuple("Recent", 2023L),
                                tuple("XPS", 2022L)
                        );
            });
        }
    }


    record UpdateStructure(String name, List<Element> sets, CriteriaCondition value) implements UpdateQuery {

        @Override
        public Optional<CriteriaCondition> where() {
            return Optional.ofNullable(value);
        }

        @Override
        public SelectQuery toSelectQuery() {
            return null;
        }
    }
}