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
package org.eclipse.jnosql.lite.mapping.entities;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class WineTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Wine.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("Wine", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Wine.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Wine.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Wine.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        Wine wine = entityMetadata.newInstance();
        Assertions.assertNotNull(wine);
        Assertions.assertTrue(wine instanceof Wine);
    }

    @Test
    void shouldGetFieldsName() {
        List<String> fields = entityMetadata.fieldsName();
        Assertions.assertEquals(3, fields.size());
        Assertions.assertTrue(fields.contains("id"));
        Assertions.assertTrue(fields.contains("name"));
        Assertions.assertTrue(fields.contains("factory"));
    }

    @Test
    void shouldGetFieldsGroupByName() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Assertions.assertNotNull(groupByName);
        Assertions.assertNotNull(groupByName.get("_id"));
        Assertions.assertNotNull(groupByName.get("name"));
        Assertions.assertNotNull(groupByName.get("factory"));
    }


    @Test
    public void shouldReturnGrouping() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata factory = groupByName.get("factory");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(factory.type()).isEqualTo(WineFactory.class);
            soft.assertThat(factory.mappingType()).isEqualTo(MappingType.EMBEDDED_GROUP);
        });
    }
}
