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

import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.metamodel.ComparableAttribute;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
class WishListTest {

    @Test
    void shouldHaveCorrectIdMetamodel() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WishList.ID).isEqualTo("_id");
            soft.assertThat(_WishList.id).isInstanceOf(ComparableAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectProductsMetamodel() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WishList.PRODUCTS).isEqualTo("products");
            soft.assertThat(_WishList.products).isInstanceOf(BasicAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectTravelsMetamodel() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WishList.TRAVELS).isEqualTo("travels");
            soft.assertThat(_WishList.travels).isInstanceOf(BasicAttribute.class);
        });
    }

    @Test
    void shouldHaveCorrectPeopleMetamodel() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_WishList.PEOPLE).isEqualTo("people");
            soft.assertThat(_WishList.people).isInstanceOf(BasicAttribute.class);
        });
    }
}