/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.extensions.sql.model.Account;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs native function predicates through real Jakarta Persistence Criteria queries (EclipseLink on Derby).
 */
class NativeFunctionIntegrationTest {

    private static EntityManagerFactory factory;

    private EntityManager entityManager;

    private SqlTemplate template;

    @BeforeAll
    static void createFactory() {
        factory = Persistence.createEntityManagerFactory("testPersistenceUnit");
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @BeforeEach
    void setUp() {
        this.entityManager = factory.createEntityManager();
        this.template = DefaultSqlTemplate.of(entityManager);
        this.template.deleteAll(Account.class);
        this.template.insert(List.of(
                Account.of(1, "Otavio", "OTAVIO", -100),
                Account.of(2, "otavio", "JAVA", 100),
                Account.of(3, "JAVA", "java", -250),
                Account.of(4, "Jakarta", "JAKARTA", 500),
                Account.of(5, "Abc", "ABC", 0)));
        this.entityManager.clear();
    }

    private List<Long> ids(String query) {
        return ids(template.prepare(query));
    }

    private List<Long> ids(PreparedStatement statement) {
        try (Stream<Account> stream = statement.result()) {
            return stream.map(Account::getId).sorted().toList();
        }
    }

    private List<Long> remaining() {
        entityManager.clear();
        return template.<Account>findAll(Account.class).map(Account::getId).sorted().toList();
    }

    static Stream<Arguments> standardFunctions() {
        return Stream.of(
                Arguments.of("UPPER(name) = 'OTAVIO'", List.of(1L, 2L)),
                Arguments.of("LOWER(name) = 'java'", List.of(3L)),
                Arguments.of("LENGTH(name) = 6", List.of(1L, 2L)),
                Arguments.of("LEFT(name, 3) = 'Ota'", List.of(1L)),
                Arguments.of("RIGHT(name, 3) = 'vio'", List.of(1L, 2L)),
                Arguments.of("ABS(balance) = 100", List.of(1L, 2L)),
                Arguments.of("UPPER(LEFT(name, 3)) = 'OTA'", List.of(1L, 2L)),
                Arguments.of("name = UPPER(otherName)", List.of(3L)),
                Arguments.of("UPPER(name) = UPPER(otherName)", List.of(1L, 3L, 4L, 5L)),
                Arguments.of("LEFT(name, LENGTH(otherName)) = 'Otavio'", List.of(1L)),
                Arguments.of("ABS(balance) > 99 AND ABS(balance) <= 250", List.of(1L, 2L, 3L)),
                Arguments.of("ABS(balance) BETWEEN 100 AND 250", List.of(1L, 2L, 3L)),
                Arguments.of("UPPER(name) LIKE 'OTA%'", List.of(1L, 2L)),
                Arguments.of("UPPER(name) = 'OTAVIO' OR (ABS(balance) > 200 AND name = 'JAVA')",
                        List.of(1L, 2L, 3L)),
                Arguments.of("NOT (UPPER(name) = 'OTAVIO')", List.of(3L, 4L, 5L)));
    }

    @Nested
    @DisplayName("When filtering with JDQL native functions")
    class WhenJdql {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.eclipse.jnosql.extensions.sql.NativeFunctionIntegrationTest#standardFunctions")
        @DisplayName("Should return exactly the matching identifiers")
        void shouldReturnMatchingIds(String predicate, List<Long> expected) {
            assertThat(ids("FROM Account WHERE " + predicate)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should use the evaluated value of an independent function")
        void shouldUseEvaluatedFunctionValue() {
            var statement = template.prepare("FROM Account WHERE name = UPPER(LEFT(:name, 19))");
            statement.bind("name", "java");

            assertThat(ids(statement)).containsExactly(3L);
        }

        @Test
        @DisplayName("Should mix native and evaluable functions")
        void shouldMixNativeAndEvaluableFunctions() {
            var statement = template.prepare("FROM Account WHERE UPPER(name) = UPPER(:name)");
            statement.bind("name", "otavio");

            assertThat(ids(statement)).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("Should partially evaluate the independent argument of a native function")
        void shouldPartiallyEvaluate() {
            var statement = template.prepare("FROM Account WHERE LEFT(name, LENGTH(:prefix)) = :prefix");
            statement.bind("prefix", "Ota");

            assertThat(ids(statement)).containsExactly(1L);
        }

        @Test
        @DisplayName("Should rebind parameters between executions of the same statement")
        void shouldRebindParameters() {
            var statement = template.prepare("FROM Account WHERE LEFT(name, LENGTH(:prefix)) = :prefix");

            assertThat(ids(statement.bind("prefix", "Ota"))).containsExactly(1L);
            assertThat(ids(statement.bind("prefix", "ota"))).containsExactly(2L);
            assertThat(ids(statement.bind("prefix", "Ja"))).containsExactly(4L);
            assertThat(ids(statement.bind("prefix", "Jakarta and more"))).isEmpty();
        }

        @Test
        @DisplayName("Should rebind a parameter used by a native function and an evaluable function")
        void shouldRebindMixedParameter() {
            var statement = template.prepare("FROM Account WHERE UPPER(name) = UPPER(:name)");

            assertThat(ids(statement.bind("name", "otavio"))).containsExactly(1L, 2L);
            assertThat(ids(statement.bind("name", "java"))).containsExactly(3L);
        }

        @Test
        @DisplayName("Should keep parameters whose names share a prefix independent")
        void shouldBindParametersWithSharedPrefix() {
            var statement = template.prepare("FROM Account WHERE UPPER(name) = :name AND otherName = :name_2");

            statement.bind("name_2", "OTAVIO").bind("name", "OTAVIO");

            assertThat(ids(statement)).containsExactly(1L);
        }

        @Test
        @DisplayName("Should cast a non integer native expression used as a length")
        void shouldCastNumericLength() {
            assertThat(ids("FROM Account WHERE LEFT(name, ABS(balance)) = name")).containsExactly(1L, 2L, 3L, 4L);
        }

        @Test
        @DisplayName("Should bind positional parameters")
        void shouldBindPositionalParameters() {
            var statement = template.prepare("FROM Account WHERE UPPER(name) = UPPER(?1)");
            ((SqlPreparedStatement) statement).bind(1, "abc");

            assertThat(ids(statement)).containsExactly(5L);
        }

        @Test
        @DisplayName("Should resolve the Java attribute name instead of the physical column")
        void shouldResolveMappedAttribute() {
            assertThat(ids("FROM Account WHERE UPPER(otherName) = 'JAVA'")).containsExactly(2L, 3L);
        }

        @Test
        @DisplayName("Should count with a native function predicate")
        void shouldCount() {
            var statement = template.prepare("SELECT COUNT(this) FROM Account WHERE UPPER(name) = 'OTAVIO'");

            assertThat(statement.count()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should sort and paginate with a native function predicate")
        void shouldSortAndPaginate() {
            var condition = CriteriaCondition.of(
                    new CriteriaOperand.Function(NativeFunction.of("UPPER",
                            FunctionArgument.attribute("name"))),
                    Condition.EQUALS, new CriteriaOperand.Function(NativeFunction.of("UPPER",
                            FunctionArgument.attribute("otherName"))));
            var select = SelectQuery.builder().from("Account").where(condition)
                    .sort(Sort.desc("id")).skip(1).limit(2).build();

            try (Stream<Account> stream = template.select(select)) {
                assertThat(stream.map(Account::getId).toList()).containsExactly(4L, 3L);
            }
        }

        @Test
        @DisplayName("Should delete only the matching records")
        void shouldDelete() {
            template.prepare("DELETE FROM Account WHERE UPPER(name) = 'OTAVIO'").result();

            assertThat(remaining()).containsExactly(3L, 4L, 5L);
        }

        @Test
        @DisplayName("Should update only the matching records")
        void shouldUpdate() {
            template.prepare("UPDATE Account SET balance = 7 WHERE LENGTH(name) = 6").result();
            entityManager.clear();

            var updated = template.<Account>findAll(Account.class)
                    .filter(account -> account.getBalance() == 7).map(Account::getId).sorted().toList();
            assertThat(updated).containsExactly(1L, 2L);
        }
    }

    @Nested
    @DisplayName("When combining native functions with cursor pagination")
    class WhenCursor {

        @Test
        @DisplayName("Should keep the native predicate and the cursor condition")
        void shouldKeepBothConditions() {
            var condition = CriteriaCondition.of(
                    new CriteriaOperand.Function(NativeFunction.of("LENGTH",
                            FunctionArgument.attribute("name"))),
                    Condition.GREATER_EQUALS_THAN, new CriteriaOperand.Constant(Value.of(4)));
            var select = SelectQuery.builder().from("Account").where(condition)
                    .sort(Sort.asc("id")).build();

            CursoredPage<Account> first = template.selectCursor(select, PageRequest.ofSize(2));
            CursoredPage<Account> second = template.selectCursor(select, first.nextPageRequest());

            assertThat(first.content()).extracting(Account::getId).containsExactly(1L, 2L);
            assertThat(second.content()).extracting(Account::getId).containsExactly(3L, 4L);
            assertThat(second.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("When the native function cannot be translated")
    class WhenUnsupported {

        @Test
        @DisplayName("Should reject a delete with an unsupported function before executing it")
        void shouldRejectUnsupportedDelete() {
            var condition = CriteriaCondition.of(
                    new CriteriaOperand.Function(NativeFunction.of("REVERSE",
                            FunctionArgument.attribute("name"))),
                    Condition.EQUALS, new CriteriaOperand.Constant(Value.of("oiva")));

            assertThatThrownBy(() -> template.delete(DeleteQuery.builder().from("Account").where(condition).build()))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThat(remaining()).containsExactly(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("Should reject an update with an unsupported function before executing it")
        void shouldRejectUnsupportedUpdate() {
            var condition = CriteriaCondition.of(
                    new CriteriaOperand.Function(NativeFunction.of("REVERSE",
                            FunctionArgument.attribute("name"))),
                    Condition.EQUALS, new CriteriaOperand.Constant(Value.of("oiva")));
            UpdateQuery update = new UpdateStructure("Account",
                    List.of(Element.of("balance", 1L)), condition);

            assertThatThrownBy(() -> template.update(update)).isInstanceOf(UnsupportedOperationException.class);
            assertThat(template.<Account>findAll(Account.class).map(Account::getBalance).sorted().toList())
                    .containsExactly(-250L, -100L, 0L, 100L, 500L);
        }
    }

    @Nested
    @DisplayName("When inspecting null, empty and Unicode semantics")
    class WhenEdgeCases {

        @Test
        @DisplayName("Should not match null attributes and should match empty strings")
        void shouldHandleNullAndEmpty() {
            template.insert(List.of(Account.of(10, null, "x", 1), Account.of(11, "", "x", 1)));

            assertThat(ids("FROM Account WHERE LENGTH(name) = 0")).containsExactly(11L);
            assertThat(ids("FROM Account WHERE UPPER(name) = ''")).containsExactly(11L);
            assertThat(ids("FROM Account WHERE LEFT(name, 3) = ''")).containsExactly(11L);
            assertThat(ids("FROM Account WHERE RIGHT(name, 3) = ''")).containsExactly(11L);
            assertThat(ids("FROM Account WHERE LENGTH(name) >= 0 AND id >= 10")).containsExactly(11L);
        }

        @Test
        @DisplayName("Should handle zero and excessive lengths in LEFT and RIGHT")
        void shouldHandleBoundaryLengths() {
            assertThat(ids("FROM Account WHERE LEFT(name, 0) = ''")).containsExactly(1L, 2L, 3L, 4L, 5L);
            assertThat(ids("FROM Account WHERE RIGHT(name, 0) = ''")).containsExactly(1L, 2L, 3L, 4L, 5L);
            assertThat(ids("FROM Account WHERE LEFT(name, 100) = name")).containsExactly(1L, 2L, 3L, 4L, 5L);
            assertThat(ids("FROM Account WHERE RIGHT(name, 100) = name")).containsExactly(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("Should apply database case conversion and length to accented characters")
        void shouldHandleAccentedCharacters() {
            template.insert(List.of(Account.of(20, "Ação", "x", 1)));

            assertThat(ids("FROM Account WHERE LENGTH(name) = 4 AND id = 20")).containsExactly(20L);
            assertThat(ids("FROM Account WHERE UPPER(name) = 'AÇÃO'")).containsExactly(20L);
            assertThat(ids("FROM Account WHERE LOWER(name) = 'ação'")).containsExactly(20L);
            assertThat(ids("FROM Account WHERE LEFT(name, 2) = 'Aç'")).containsExactly(20L);
            assertThat(ids("FROM Account WHERE RIGHT(name, 2) = 'ão'")).containsExactly(20L);
        }

        @Test
        @DisplayName("Should document the database length semantics of supplementary characters")
        void shouldReportSupplementaryCharacterLength() {
            template.insert(List.of(Account.of(30, "A\uD83D\uDE00B", "x", 1)));

            // The core counts code points (3); Derby counts UTF-16 code units (4)
            List<Long> codePoints = ids("FROM Account WHERE LENGTH(name) = 3 AND id = 30");
            List<Long> codeUnits = ids("FROM Account WHERE LENGTH(name) = 4 AND id = 30");

            assertThat(codePoints.size() + codeUnits.size()).isEqualTo(1);
            assertThat(codeUnits).as("Derby counts UTF-16 code units").containsExactly(30L);
        }

        @Test
        @DisplayName("Should preserve the sign semantics of ABS for negative, positive and zero values")
        void shouldHandleAbsSigns() {
            assertThat(ids("FROM Account WHERE ABS(balance) = 250")).containsExactly(3L);
            assertThat(ids("FROM Account WHERE ABS(balance) = 0")).containsExactly(5L);
            assertThat(ids("FROM Account WHERE ABS(balance) = 500")).containsExactly(4L);
        }

        @Test
        @DisplayName("Should not apply an additional conversion to UPPER with IGNORE_CASE")
        void shouldIgnoreCaseWithExplicitFunction() {
            var condition = CriteriaCondition.of(
                    new CriteriaOperand.Function(NativeFunction.of("LOWER",
                            FunctionArgument.attribute("name"))),
                    Condition.EQUALS, new CriteriaOperand.Constant(Value.of("JAVA")));
            var ignoring = CriteriaCondition.of(
                    Element.of(Condition.IGNORE_CASE.getNameField(),
                            condition), Condition.IGNORE_CASE);

            List<Long> exact = template.<Account>select(SelectQuery.builder().from("Account")
                    .where(condition).build()).map(Account::getId).toList();
            List<Long> insensitive = template.<Account>select(SelectQuery.builder().from("Account")
                    .where(ignoring).build()).map(Account::getId).sorted().toList();

            assertThat(exact).isEmpty();
            assertThat(insensitive).containsExactly(3L);
        }
    }

    private record UpdateStructure(String name, List<Element> sets, CriteriaCondition value)
            implements UpdateQuery {

        @Override
        public java.util.Optional<CriteriaCondition> where() {
            return java.util.Optional.ofNullable(value);
        }

        @Override
        public SelectQuery toSelectQuery() {
            return null;
        }
    }
}
