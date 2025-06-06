/*
 *  Copyright (c) 2025 Otávio Santana and others
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
package org.eclipse.jnsoql.entities;

import jakarta.data.metamodel.NumericAttribute;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrimitiveNumberTest {

    @Test
    void shouldHaveCorrectByteNumberAttribute() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_PrimitiveNumber.byteNumber).isNotNull();
            softly.assertThat(_PrimitiveNumber.byteNumber.name()).isEqualTo("byteNumber");
            softly.assertThat(_PrimitiveNumber.byteNumber).isInstanceOf(NumericAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectShortNumberAttribute() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_PrimitiveNumber.shortNumber).isNotNull();
            softly.assertThat(_PrimitiveNumber.shortNumber.name()).isEqualTo("shortNumber");
            softly.assertThat(_PrimitiveNumber.shortNumber).isInstanceOf(NumericAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectIntNumberAttribute() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_PrimitiveNumber.integer).isNotNull();
            softly.assertThat(_PrimitiveNumber.integer.name()).isEqualTo("integer");
            softly.assertThat(_PrimitiveNumber.integer).isInstanceOf(NumericAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectLongNumberAttribute() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_PrimitiveNumber.longNumber).isNotNull();
            softly.assertThat(_PrimitiveNumber.longNumber.name()).isEqualTo("longNumber");
            softly.assertThat(_PrimitiveNumber.longNumber).isInstanceOf(NumericAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectFloatNumberAttribute() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_PrimitiveNumber.floatNumber).isNotNull();
            softly.assertThat(_PrimitiveNumber.floatNumber.name()).isEqualTo("floatNumber");
            softly.assertThat(_PrimitiveNumber.floatNumber).isInstanceOf(NumericAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectDoubleNumberAttribute() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_PrimitiveNumber.doubleNumber).isNotNull();
            softly.assertThat(_PrimitiveNumber.doubleNumber.name()).isEqualTo("doubleNumber");
            softly.assertThat(_PrimitiveNumber.doubleNumber).isInstanceOf(NumericAttribute.class);
        });
    }
}