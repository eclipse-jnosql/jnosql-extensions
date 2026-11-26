/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/Apache-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManagerProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Structured queries with native functions are translated to Criteria expressions and executed
 * by EclipseLink on Derby.
 */
public class NativeFunctionCriteriaTest {

    private static final String ENTITY = "FunctionRecord";

    private SeContainer cdiContainer;
    private PersistenceDocumentTemplate template;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories = Set.of(PersonRepository.class);
        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer().initialize();
        template = new PersistenceDocumentTemplate(cdiContainer.select(PersistenceDatabaseManagerProvider.class).get()
                .getManager(em()));
        em().getTransaction().begin();
        em().createQuery("delete from FunctionRecord r").executeUpdate();
        em().persist(new FunctionRecord(1, "Otavio", "OTAVIO", -100));
        em().persist(new FunctionRecord(2, "otavio", "JAVA", 100));
        em().persist(new FunctionRecord(3, "JAVA", "java", -250));
        em().persist(new FunctionRecord(4, "Jakarta", "JAKARTA", 500));
        em().persist(new FunctionRecord(5, "Abc", "ABC", 0));
        em().persist(new FunctionRecord(6, null, null, 7));
        em().flush();
    }

    @AfterEach
    void cleanup() {
        em().getTransaction().commit();
        cdiContainer.close();
    }

    private EntityManager em() {
        return cdiContainer.select(EntityManager.class).get();
    }

    private static FunctionArgument attr(String name) {
        return FunctionArgument.attribute(name);
    }

    private static FunctionArgument lit(Object value) {
        return FunctionArgument.literal(value);
    }

    private static FunctionArgument fn(String name, FunctionArgument... args) {
        return FunctionArgument.function(NativeFunction.of(name, args));
    }

    private static CriteriaOperand function(String name, FunctionArgument... args) {
        return new CriteriaOperand.Function(NativeFunction.of(name, args));
    }

    private static CriteriaOperand attribute(String name) {
        return new CriteriaOperand.Attribute(name);
    }

    private static CriteriaOperand constant(Object value) {
        return new CriteriaOperand.Constant(Value.of(value));
    }

    private static CriteriaCondition eq(CriteriaOperand left, CriteriaOperand right) {
        return CriteriaCondition.of(left, Condition.EQUALS, right);
    }

    private List<Long> ids(CriteriaCondition condition) {
        return template.<FunctionRecord>select(SelectQuery.builder().from(ENTITY).where(condition).build())
                .map(FunctionRecord::getId).toList();
    }

    @Test
    void upperOnLeft() {
        assertThat(ids(eq(function("UPPER", attr("name")), constant("OTAVIO"))), containsInAnyOrder(1L, 2L));
    }

    @Test
    void lower() {
        assertThat(ids(eq(function("LOWER", attr("name")), constant("java"))), containsInAnyOrder(3L));
    }

    @Test
    void length() {
        assertThat(ids(eq(function("LENGTH", attr("name")), constant(6))), containsInAnyOrder(1L, 2L));
    }

    @Test
    void left() {
        assertThat(ids(eq(function("LEFT", attr("name"), lit(3)), constant("Ota"))), containsInAnyOrder(1L));
    }

    @Test
    void right() {
        assertThat(ids(eq(function("RIGHT", attr("name"), lit(3)), constant("vio"))), containsInAnyOrder(1L, 2L));
    }

    @Test
    void absKeepsIntegerType() {
        assertThat(ids(eq(function("ABS", attr("balance")), constant(100))), containsInAnyOrder(1L, 2L));
    }

    @Test
    void nativeOnRight() {
        assertThat(ids(eq(attribute("name"), function("UPPER", attr("otherName")))), containsInAnyOrder(3L));
    }

    @Test
    void nativeOnBothSides() {
        assertThat(ids(eq(function("UPPER", attr("name")), function("UPPER", attr("otherName")))),
                containsInAnyOrder(1L, 3L, 4L, 5L));
    }

    @Test
    void nativeAndEvaluatedValue() {
        // core evaluates UPPER('otavio') and hands over the constant "OTAVIO"
        assertThat(ids(eq(function("UPPER", attr("name")), constant("OTAVIO"))),
                containsInAnyOrder(1L, 2L));
    }

    @Test
    void nestedNative() {
        assertThat(ids(eq(function("UPPER", fn("LEFT", attr("name"), lit(3))), constant("OTA"))),
                containsInAnyOrder(1L, 2L));
    }

    @Test
    void evaluatedLengthAsArgument() {
        // core evaluates LENGTH('Ota') and hands over the value 3
        assertThat(ids(eq(function("LEFT", attr("name"), lit(3)), constant("Ota"))),
                containsInAnyOrder(1L));
    }

    @Test
    void nativeLengthAsArgument() {
        assertThat(ids(eq(function("LEFT", attr("name"), fn("LENGTH", attr("otherName"))), constant("Otavio"))),
                containsInAnyOrder(1L));
    }

    @Test
    void leftLongerThanText() {
        assertThat(ids(eq(function("LEFT", attr("name"), lit(100)), constant("Abc"))), containsInAnyOrder(5L));
    }

    @Test
    void zeroLengthPrefix() {
        assertThat(ids(eq(function("LEFT", attr("name"), lit(0)), constant(""))), containsInAnyOrder(1L, 2L, 3L, 4L, 5L));
    }

    @Test
    void nullTextIsNeverMatched() {
        assertThat(ids(eq(function("UPPER", attr("name")), constant("X"))).contains(6L), is(false));
    }

    @Test
    void booleanGrouping() {
        CriteriaCondition condition = CriteriaCondition.or(
                CriteriaCondition.and(eq(function("UPPER", attr("name")), constant("OTAVIO")),
                        CriteriaCondition.gt("balance", 0L)),
                CriteriaCondition.not(CriteriaCondition.or(eq(function("LENGTH", attr("name")), constant(6)),
                        eq(function("LENGTH", attr("name")), constant(3)))));
        assertThat(ids(condition), containsInAnyOrder(2L, 3L, 4L));
    }

    @Test
    void ignoreCaseWithFunction() {
        assertThat(ids(CriteriaCondition.ignoreCase(eq(function("LOWER", attr("name")), constant("OTAVIO")))),
                containsInAnyOrder(1L, 2L));
    }

    @Test
    void functionComparison() {
        assertThat(ids(CriteriaCondition.of(function("ABS", attr("balance")), Condition.GREATER_THAN, constant(250))),
                containsInAnyOrder(4L));
    }

    @Test
    void count() {
        SelectQuery query = SelectQuery.builder().from(ENTITY)
                .where(eq(function("UPPER", attr("name")), constant("OTAVIO"))).build();
        assertThat(template.count(query), is(2L));
    }

    @Test
    void exists() {
        assertThat(template.exists(SelectQuery.builder().from(ENTITY)
                .where(eq(function("UPPER", attr("name")), constant("OTAVIO"))).build()), is(true));
        assertThat(template.exists(SelectQuery.builder().from(ENTITY)
                .where(eq(function("UPPER", attr("name")), constant("NOBODY"))).build()), is(false));
    }

    @Test
    void sortedAndLimited() {
        SelectQuery query = SelectQuery.builder().from(ENTITY)
                .where(eq(function("UPPER", attr("name")), constant("OTAVIO")))
                .sort(jakarta.data.Sort.desc("id")).limit(1).build();
        assertThat(template.<FunctionRecord>select(query).map(FunctionRecord::getId).toList(), is(List.of(2L)));
    }

    @Test
    void deleteOnlyMatching() {
        DeleteQuery query = DeleteQuery.builder().from(ENTITY)
                .where(eq(function("UPPER", attr("name")), constant("OTAVIO"))).build();
        assertThat(template.deleteWithCount(query), is(2L));
        em().clear();
        assertThat(ids(CriteriaCondition.gt("id", 0L)), containsInAnyOrder(3L, 4L, 5L, 6L));
    }

    @Test
    void unsupportedFunctionIsRejected() {
        CriteriaCondition condition = eq(function("REVERSE", attr("name")), constant("x"));
        assertThrows(UnsupportedOperationException.class, () -> ids(condition));
    }

    @Test
    void unsupportedDeleteFailsBeforeExecution() {
        DeleteQuery query = DeleteQuery.builder().from(ENTITY)
                .where(eq(function("REVERSE", attr("name")), constant("x"))).build();
        assertThrows(UnsupportedOperationException.class, () -> template.deleteWithCount(query));
        em().clear();
        assertThat(ids(CriteriaCondition.gt("id", 0L)).size(), is(6));
    }

    @Test
    void likeOnNumericFunctionIsRejected() {
        CriteriaCondition condition = CriteriaCondition.of(function("LENGTH", attr("name")),
                Condition.LIKE, constant("6"));
        Throwable thrown = assertThrows(RuntimeException.class, () -> ids(condition));
        assertThat(unsupportedCause(thrown), is(true));
    }

    private static boolean unsupportedCause(Throwable thrown) {
        for (Throwable t = thrown; t != null; t = t.getCause()) {
            if (t instanceof UnsupportedOperationException) {
                return true;
            }
        }
        return false;
    }
}

