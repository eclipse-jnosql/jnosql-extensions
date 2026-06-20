/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities.converters;

import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class NoSQLDatabaseTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(NoSQLDatabase.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("NoSQLDatabase", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(NoSQLDatabase.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(NoSQLDatabase.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(NoSQLDatabase.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        org.assertj.core.api.Assertions.assertThat(id).isPresent();
    }

    @Test
    void shouldHaveAutoApplyConverter() {
        Optional<FieldMetadata> id = this.entityMetadata.fieldMapping("id");
        org.assertj.core.api.Assertions.assertThat(id).isPresent();
        org.assertj.core.api.Assertions.assertThat(id.get().converter()).isPresent();
    }

    @Test
    void shouldDefineConverterConstructor() {
        ConstructorMetadata constructor = this.entityMetadata.constructor();
        org.assertj.core.api.Assertions.assertThat(constructor).isNotNull();
        List<ParameterMetaData> parameters = constructor.parameters();
        org.assertj.core.api.Assertions.assertThat(parameters).hasSize(2);
        ParameterMetaData parameterMetaData = parameters.getFirst();
        org.assertj.core.api.Assertions.assertThat(parameterMetaData.converter()).isPresent();

    }
}
