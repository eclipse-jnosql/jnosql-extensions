/*
 *  Copyright (c) 2023 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ComputerTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Computer.class);
    }

    @Test
    void shouldReturnAsEmbeddable() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata address = groupByName.get("address");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(address.name()).isEqualTo("address");
            soft.assertThat(address.isId()).isFalse();
            soft.assertThat(address.mappingType()).isEqualTo(MappingType.EMBEDDED);
        });
    }

    @Test
    void shouldReturnAsEntity(){
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata fieldMetadata = groupByName.get("users");
        var users = (CollectionFieldMetadata) fieldMetadata;
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(users.name()).isEqualTo("users");
            soft.assertThat(users.isId()).isFalse();
            soft.assertThat(users.isEmbeddable()).isTrue();
            soft.assertThat(users.mappingType()).isEqualTo(MappingType.COLLECTION);
            soft.assertThat(users.elementType()).isEqualTo(Person.class);
            soft.assertThat(users.collectionInstance()).isInstanceOf(List.class);
        });

    }
}