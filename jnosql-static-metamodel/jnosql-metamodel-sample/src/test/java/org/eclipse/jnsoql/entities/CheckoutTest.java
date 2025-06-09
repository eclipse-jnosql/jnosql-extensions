/*
 *  Copyright (c) 2024 Otávio Santana and others
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

class CheckoutTest {

    @Test
    void shouldGenerateStaticMetamodel() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(_Checkout.id).isNotNull();
            softly.assertThat(_Checkout.products).isNotNull();
            softly.assertThat(_Checkout.products_name).isNotNull();
            softly.assertThat(_Checkout.products_name).isNotNull();
        });
    }

}