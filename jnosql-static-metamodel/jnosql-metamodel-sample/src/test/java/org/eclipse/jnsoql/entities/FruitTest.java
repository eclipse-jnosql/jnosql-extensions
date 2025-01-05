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

import jakarta.data.Sort;
import org.assertj.core.api.SoftAssertions;

import org.eclipse.jnosql.mapping.semistructured.metamodel.attributes.BooleanAttribute;
import org.eclipse.jnosql.mapping.semistructured.metamodel.attributes.StringAttribute;
import org.junit.jupiter.api.Test;

class FruitTest {


    @Test
    void shouldGetId(){
        var id = _Fruit.id;

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(id.name()).isEqualTo("_id");
            soft.assertThat(id.desc()).isEqualTo(Sort.desc("_id"));
            soft.assertThat(_Fruit.ID).isEqualTo("_id");
            soft.assertThat(id).isInstanceOf(StringAttribute.class);
            soft.assertThat(_Fruit.name).isInstanceOf(StringAttribute.class);
            soft.assertThat(_Fruit.isTasty).isInstanceOf(BooleanAttribute.class);
            soft.assertThat(_Fruit.isHealthy).isInstanceOf(BooleanAttribute.class);
        });
    }
}