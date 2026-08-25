/*
 *  Copyright (c) 2024 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnsoql.entities;

import jakarta.data.Sort;
import jakarta.data.metamodel.SortableAttribute;
import jakarta.data.metamodel.TextAttribute;
import org.assertj.core.api.SoftAssertions;

import org.junit.jupiter.api.Test;


class FruitTest {


    @Test
    void shouldGetId(){
        var id = _Fruit.id;

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(id.name()).isEqualTo("_id");
            soft.assertThat(id.desc()).isEqualTo(Sort.desc("_id"));
            soft.assertThat(_Fruit.ID).isEqualTo("_id");
            soft.assertThat(id).isInstanceOf(TextAttribute.class);
            soft.assertThat(_Fruit.name).isInstanceOf(TextAttribute.class);
            soft.assertThat(_Fruit.isTasty).isInstanceOf(SortableAttribute.class);
            soft.assertThat(_Fruit.isHealthy).isInstanceOf(SortableAttribute.class);
        });
    }
}