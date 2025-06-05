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

import org.junit.jupiter.api.Test;
import org.assertj.core.api.SoftAssertions;

class WrapperNumberTest {


    @Test
    void shouldCreateUuidAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.uuid.name()).isEqualTo("_id");
            soft.assertThat(_WrapperNumber.uuid.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.uuid.attributeType()).isEqualTo(java.util.UUID.class);
        });
    }

    @Test
    void shouldCreateIntegerAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.integer.name()).isEqualTo("integer");
            soft.assertThat(_WrapperNumber.integer.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.integer.attributeType()).isEqualTo(java.lang.Integer.class);
        });
    }

    @Test
    void shouldCreateLongNumberAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.longNumber.name()).isEqualTo("longNumber");
            soft.assertThat(_WrapperNumber.longNumber.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.longNumber.attributeType()).isEqualTo(java.lang.Long.class);
        });
    }

    @Test
    void shouldCreateFloatNumberAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.floatNumber.name()).isEqualTo("floatNumber");
            soft.assertThat(_WrapperNumber.floatNumber.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.floatNumber.attributeType()).isEqualTo(java.lang.Float.class);
        });
    }

    @Test
    void shouldCreateDoubleNumberAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.doubleNumber.name()).isEqualTo("doubleNumber");
            soft.assertThat(_WrapperNumber.doubleNumber.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.doubleNumber.attributeType()).isEqualTo(java.lang.Double.class);
        });
    }

    @Test
    void shouldCreateByteNumberAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.byteNumber.name()).isEqualTo("byteNumber");
            soft.assertThat(_WrapperNumber.byteNumber.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.byteNumber.attributeType()).isEqualTo(java.lang.Byte.class);
        });
    }

    @Test
    void shouldCreateShortNumberAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.shortNumber.name()).isEqualTo("shortNumber");
            soft.assertThat(_WrapperNumber.shortNumber.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.shortNumber.attributeType()).isEqualTo(java.lang.Short.class);
        });
    }

    @Test
    void shouldCreateBooleanSampleAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WrapperNumber.booleanSample.name()).isEqualTo("booleanSample");
            soft.assertThat(_WrapperNumber.booleanSample.declaringType()).isEqualTo(WrapperNumber.class);
            soft.assertThat(_WrapperNumber.booleanSample.attributeType()).isEqualTo(java.lang.Boolean.class);
        });
    }
}