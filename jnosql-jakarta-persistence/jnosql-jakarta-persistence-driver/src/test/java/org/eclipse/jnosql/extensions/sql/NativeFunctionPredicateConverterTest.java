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

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PredicateConverter with native functions")
@SuppressWarnings({"unchecked", "rawtypes"})
class NativeFunctionPredicateConverterTest {

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Root<?> root;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Path<String> name;

    @Mock
    private Path<String> otherName;

    @Mock
    private Path<Integer> balance;

    @Mock
    private Expression<String> upperName;

    @Mock
    private Expression<String> upperOtherName;

    @Mock
    private Expression<Integer> lengthExpression;

    @Mock
    private Predicate predicate;

    @Mock
    private Predicate other;

    private PredicateConverter converter;

    @BeforeEach
    void setUp() {
        Map<String, Path<?>> paths = Map.of("name", name, "otherName", otherName, "balance", balance);
        converter = new PredicateConverter((r, property, em) -> paths.get(property));
        when(name.getJavaType()).thenReturn((Class) String.class);
        when(otherName.getJavaType()).thenReturn((Class) String.class);
        when(balance.getJavaType()).thenReturn((Class) Integer.class);
        when(upperName.getJavaType()).thenReturn((Class) String.class);
        when(upperOtherName.getJavaType()).thenReturn((Class) String.class);
        when(cb.upper(name)).thenReturn(upperName);
        when(cb.upper(otherName)).thenReturn(upperOtherName);
    }

    private static CriteriaOperand function(String name, FunctionArgument... arguments) {
        return new CriteriaOperand.Function(NativeFunction.of(name, arguments));
    }

    private static CriteriaOperand constant(Object value) {
        return new CriteriaOperand.Constant(Value.of(value));
    }

    private Predicate convert(CriteriaCondition condition) {
        return converter.toPredicate(condition, cb, root, entityManager);
    }

    @Test
    @DisplayName("Should apply UPPER to the resolved attribute and compare with a value")
    void shouldTranslateNativeFunctionOnTheLeft() {
        when(cb.equal(upperName, "OTAVIO")).thenReturn(predicate);

        var result = convert(CriteriaCondition.of(function("UPPER", FunctionArgument.attribute("name")),
                Condition.EQUALS, constant("OTAVIO")));

        assertThat(result).isSameAs(predicate);
        verify(cb).upper(name);
    }

    @Test
    @DisplayName("Should compare an attribute with a native function on the right")
    void shouldTranslateNativeFunctionOnTheRight() {
        when(cb.equal(upperOtherName, upperOtherName)).thenReturn(predicate);
        var condition = CriteriaCondition.of(new CriteriaOperand.Attribute("name"), Condition.EQUALS,
                function("UPPER", FunctionArgument.attribute("otherName")));
        when(cb.equal(name, upperOtherName)).thenReturn(predicate);

        assertThat(convert(condition)).isSameAs(predicate);
    }

    @Test
    @DisplayName("Should compare native functions on both sides")
    void shouldTranslateNativeFunctionsOnBothSides() {
        when(cb.equal(upperName, upperOtherName)).thenReturn(predicate);

        var result = convert(CriteriaCondition.of(function("UPPER", FunctionArgument.attribute("name")),
                Condition.EQUALS, function("UPPER", FunctionArgument.attribute("otherName"))));

        assertThat(result).isSameAs(predicate);
    }

    @Test
    @DisplayName("Should evaluate independent arguments and keep native functions native")
    void shouldMixNativeAndEvaluableFunctions() {
        when(cb.equal(upperName, "OTAVIO")).thenReturn(predicate);
        var left = function("UPPER", FunctionArgument.attribute("name"));

        var result = convert(CriteriaCondition.of(left, Condition.EQUALS, constant("OTAVIO")));

        assertThat(result).isSameAs(predicate);
        verify(cb, times(1)).upper(any(Expression.class));
    }

    @Test
    @DisplayName("Should not create a native expression for an evaluated value")
    void shouldNotGenerateNativeFunctionForEvaluatedValue() {
        when(cb.equal(name, "OTAVIO")).thenReturn(predicate);

        var result = convert(CriteriaCondition.of(Element.of("name", "OTAVIO"), Condition.EQUALS));

        assertThat(result).isSameAs(predicate);
        verify(cb, never()).upper(any(Expression.class));
        verify(cb, never()).lower(any(Expression.class));
    }

    @Test
    @DisplayName("Should translate LOWER, LENGTH and ABS")
    void shouldTranslateLowerLengthAndAbs() {
        Expression<String> lower = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> abs = org.mockito.Mockito.mock(Expression.class);
        when(cb.lower(name)).thenReturn(lower);
        when(cb.length(name)).thenReturn(lengthExpression);
        when(cb.abs((Expression) balance)).thenReturn((Expression) abs);
        when(cb.equal(lower, "java")).thenReturn(predicate);
        when(cb.equal(lengthExpression, 6)).thenReturn(predicate);
        when(cb.equal(abs, 100)).thenReturn(predicate);

        assertThat(convert(CriteriaCondition.of(function("LOWER", FunctionArgument.attribute("name")),
                Condition.EQUALS, constant("java")))).isSameAs(predicate);
        assertThat(convert(CriteriaCondition.of(function("LENGTH", FunctionArgument.attribute("name")),
                Condition.EQUALS, constant(6)))).isSameAs(predicate);
        assertThat(convert(CriteriaCondition.of(function("ABS", FunctionArgument.attribute("balance")),
                Condition.EQUALS, constant(100)))).isSameAs(predicate);

        verify(cb).abs((Expression) balance);
    }

    @Test
    @DisplayName("Should translate LEFT as a substring from the first position")
    void shouldTranslateLeft() {
        Expression<Integer> one = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> three = org.mockito.Mockito.mock(Expression.class);
        Expression<String> substring = org.mockito.Mockito.mock(Expression.class);
        when(cb.literal(1)).thenReturn(one);
        when(cb.literal(3)).thenReturn(three);
        when(cb.substring(name, one, three)).thenReturn(substring);
        when(cb.equal(substring, "Ota")).thenReturn(predicate);

        var result = convert(CriteriaCondition.of(function("LEFT", FunctionArgument.attribute("name"),
                FunctionArgument.literal(3)), Condition.EQUALS, constant("Ota")));

        assertThat(result).isSameAs(predicate);
        verify(cb).substring(name, one, three);
    }

    @Test
    @DisplayName("Should translate RIGHT with a start position bounded to one")
    void shouldTranslateRight() {
        Expression<Integer> nameLength = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> three = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> difference = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> start = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> bounded = org.mockito.Mockito.mock(Expression.class);
        Expression<String> substring = org.mockito.Mockito.mock(Expression.class);
        CriteriaBuilder.Case<Integer> selectCase = org.mockito.Mockito.mock(CriteriaBuilder.Case.class);
        when(cb.length(name)).thenReturn(nameLength);
        when(cb.literal(3)).thenReturn(three);
        when(cb.diff(nameLength, three)).thenReturn(difference);
        when(cb.sum(difference, 1)).thenReturn(start);
        when(cb.lt(start, 1)).thenReturn(predicate);
        when(cb.<Integer>selectCase()).thenReturn(selectCase);
        when(selectCase.when(predicate, 1)).thenReturn(selectCase);
        when(selectCase.otherwise(start)).thenReturn(bounded);
        when(cb.substring(name, bounded)).thenReturn(substring);
        when(cb.equal(substring, "vio")).thenReturn(other);

        var result = convert(CriteriaCondition.of(function("RIGHT", FunctionArgument.attribute("name"),
                FunctionArgument.literal(3)), Condition.EQUALS, constant("vio")));

        assertThat(result).isSameAs(other);
        verify(selectCase).when(predicate, 1);
    }

    @Test
    @DisplayName("Should cast and bound a non integer numeric expression used as a length")
    void shouldCastNonIntegerLength() {
        Expression<Long> longBalance = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> casted = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> one = org.mockito.Mockito.mock(Expression.class);
        Expression<String> substring = org.mockito.Mockito.mock(Expression.class);
        when(longBalance.getJavaType()).thenReturn((Class) Long.class);
        when(cb.abs((Expression) balance)).thenReturn((Expression) longBalance);
        when(longBalance.cast(Integer.class)).thenReturn(casted);
        when(cb.literal(1)).thenReturn(one);
        Expression<Integer> nameLength = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> bounded = org.mockito.Mockito.mock(Expression.class);
        CriteriaBuilder.Case<Integer> selectCase = org.mockito.Mockito.mock(CriteriaBuilder.Case.class);
        when(cb.length(name)).thenReturn(nameLength);
        when(cb.gt(casted, nameLength)).thenReturn(other);
        when(cb.<Integer>selectCase()).thenReturn(selectCase);
        when(selectCase.when(other, nameLength)).thenReturn(selectCase);
        when(selectCase.otherwise(casted)).thenReturn(bounded);
        when(cb.substring(name, one, bounded)).thenReturn(substring);
        when(cb.equal(substring, "x")).thenReturn(predicate);
        var abs = NativeFunction.of("ABS", FunctionArgument.attribute("balance"));

        var result = convert(CriteriaCondition.of(function("LEFT", FunctionArgument.attribute("name"),
                new FunctionArgument.NativeArgument(abs)), Condition.EQUALS, constant("x")));

        assertThat(result).isSameAs(predicate);
        verify(longBalance).cast(Integer.class);
    }

    @Test
    @DisplayName("Should translate nested native functions recursively")
    void shouldTranslateNestedFunctions() {
        Expression<Integer> one = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> three = org.mockito.Mockito.mock(Expression.class);
        Expression<String> substring = org.mockito.Mockito.mock(Expression.class);
        Expression<String> upper = org.mockito.Mockito.mock(Expression.class);
        when(cb.literal(1)).thenReturn(one);
        when(cb.literal(3)).thenReturn(three);
        when(cb.substring(name, one, three)).thenReturn(substring);
        when(cb.upper(substring)).thenReturn(upper);
        when(cb.equal(upper, "OTA")).thenReturn(predicate);
        var left = NativeFunction.of("LEFT", FunctionArgument.attribute("name"), FunctionArgument.literal(3));

        var result = convert(CriteriaCondition.of(function("UPPER", new FunctionArgument.NativeArgument(left)),
                Condition.EQUALS, constant("OTA")));

        assertThat(result).isSameAs(predicate);
    }

    @Test
    @DisplayName("Should use an already evaluated length as a literal in a partial evaluation")
    void shouldUsePartiallyEvaluatedLength() {
        Expression<Integer> one = org.mockito.Mockito.mock(Expression.class);
        Expression<Integer> three = org.mockito.Mockito.mock(Expression.class);
        when(cb.literal(1)).thenReturn(one);
        when(cb.literal(3)).thenReturn(three);

        convert(CriteriaCondition.of(function("LEFT", FunctionArgument.attribute("name"),
                new FunctionArgument.ValueArgument(Value.of(3))), Condition.EQUALS, constant("Ota")));

        verify(cb).substring(name, one, three);
        verify(cb, never()).length(any(Expression.class));
    }

    @Test
    @DisplayName("Should keep both conditions in an AND and negate a native predicate")
    void shouldKeepBooleanGrouping() {
        when(cb.equal(upperName, "OTAVIO")).thenReturn(predicate);
        when(cb.equal(name, "JAVA")).thenReturn(other);
        var nativeCondition = CriteriaCondition.of(function("UPPER", FunctionArgument.attribute("name")),
                Condition.EQUALS, constant("OTAVIO"));
        var ordinary = CriteriaCondition.of(Element.of("name", "JAVA"), Condition.EQUALS);

        convert(CriteriaCondition.and(nativeCondition, ordinary));
        convert(nativeCondition.negate());

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        assertThat(captor.getValue()).containsExactly(predicate, other);
        verify(cb).not(predicate);
    }

    @Test
    @DisplayName("Should not apply UPPER twice when IGNORE_CASE meets an explicit UPPER")
    void shouldNotDuplicateCaseConversion() {
        when(cb.equal(upperName, "OTAVIO")).thenReturn(predicate);
        var condition = CriteriaCondition.ignoreCase(CriteriaCondition.of(
                function("UPPER", FunctionArgument.attribute("name")), Condition.EQUALS, constant("otavio")));

        assertThat(convert(condition)).isSameAs(predicate);
        verify(cb).upper(name);
        verify(cb, never()).upper(upperName);
    }

    @Test
    @DisplayName("Should reject an unsupported function name")
    void shouldRejectUnsupportedFunction() {
        var condition = CriteriaCondition.of(function("REVERSE", FunctionArgument.attribute("name")),
                Condition.EQUALS, constant("x"));

        assertThatThrownBy(() -> convert(condition)).isInstanceOf(UnsupportedOperationException.class);
        verify(cb, never()).equal(any(Expression.class), any(Object.class));
    }

    @Test
    @DisplayName("Should reject a string function over a numeric attribute")
    void shouldRejectWrongAttributeType() {
        var condition = CriteriaCondition.of(function("UPPER", FunctionArgument.attribute("balance")),
                Condition.EQUALS, constant("x"));

        assertThatThrownBy(() -> convert(condition)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should reject an attribute that cannot be resolved")
    void shouldRejectUnresolvedAttribute() {
        var condition = CriteriaCondition.of(function("UPPER", FunctionArgument.attribute("missing")),
                Condition.EQUALS, constant("x"));

        assertThatThrownBy(() -> convert(condition)).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("missing");
    }

    @Test
    @DisplayName("Should reject ordering a native function against a non comparable value")
    void shouldRejectNonComparableOrdering() {
        var condition = CriteriaCondition.of(function("UPPER", FunctionArgument.attribute("name")),
                Condition.GREATER_THAN, constant(List.of("a")));

        assertThatThrownBy(() -> convert(condition)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should keep the ordinary equality path untouched")
    void shouldKeepOrdinaryPredicates() {
        when(cb.equal(name, "x")).thenReturn(predicate);

        assertThat(convert(CriteriaCondition.of(Element.of("name", "x"), Condition.EQUALS))).isSameAs(predicate);
        verify(cb, never()).length(any(Expression.class));
    }
}
