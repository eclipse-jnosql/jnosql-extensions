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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import jakarta.data.metamodel.TextAttribute;
import jakarta.data.metamodel.NavigableAttribute;

class VehicleTest {
    @Test
    void shouldValidateVehicleIdAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_Vehicle.id.name()).isEqualTo("_id");
            soft.assertThat(_Vehicle.id.declaringType()).isEqualTo(Vehicle.class);
            soft.assertThat(_Vehicle.id).isInstanceOf(TextAttribute.class);
        });
    }

    @Test
    void shouldValidateDriverAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_Vehicle.driver.name()).isEqualTo("driver");
            soft.assertThat(_Vehicle.driver.declaringType()).isEqualTo(Vehicle.class);
            soft.assertThat(_Vehicle.driver).isInstanceOf(NavigableAttribute.class);
        });
    }

    @Test
    void shouldValidateDriverNameAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_Vehicle.driver_name.name()).isEqualTo("driver_name");
            soft.assertThat(_Vehicle.driver_name.declaringType()).isEqualTo(Driver.class);
        });
    }

    @Test
    void shouldValidateDriverLicenseNumberAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_Vehicle.driver_licenseNumber.name()).isEqualTo("driver_license");
            soft.assertThat(_Vehicle.driver_licenseNumber.declaringType()).isEqualTo(Driver.class);
        });
    }
}