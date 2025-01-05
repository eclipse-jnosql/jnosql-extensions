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
package org.eclipse.jnosql.lite.mapping.entities.record;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.entities.Money;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class DocumentRecordTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(DocumentRecord.class);
    }


    @Test
    void shouldGetName() {
        Assertions.assertEquals("DocumentRecord", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(DocumentRecord.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(DocumentRecord.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(DocumentRecord.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertTrue(id.isPresent());
    }

    @Test
    void shouldCreateInstance() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        ConstructorBuilder constructorBuilder = ConstructorBuilder.of(constructor);
        constructorBuilder.add("Ada");
        constructorBuilder.add(Map.of("name", "Ada"));
        constructorBuilder.add(Money.of("EUR 10"));
        DocumentRecord record = constructorBuilder.build();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(record).isNotNull();
            s.assertThat(record.id()).isEqualTo("Ada");
            s.assertThat(record.data()).isEqualTo(Map.of("name", "Ada"));
            s.assertThat(record.money()).isEqualTo(Money.of("EUR 10"));
        });
    }

    @Test
    void shouldGetter() {
        DocumentRecord documentRecord = new DocumentRecord("Ada", Map.of("name", "Ada"), new Money("EUR", BigDecimal.TEN));
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        FieldMetadata id = groupByName.get("_id");
        FieldMetadata data = groupByName.get("data");
        FieldMetadata money = groupByName.get("money");
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(id.read(documentRecord)).isEqualTo("Ada");
            s.assertThat(data.read(documentRecord)).isEqualTo( Map.of("name", "Ada"));
            s.assertThat(money.read(documentRecord)).isEqualTo(new Money("EUR", BigDecimal.TEN));
        });
    }

    @Test
    void shouldCheckConstructor() {
        ConstructorMetadata constructor = entityMetadata.constructor();
        org.assertj.core.api.Assertions.assertThat(constructor.isDefault()).isFalse();
        List<ParameterMetaData> parameters = constructor.parameters();
        org.assertj.core.api.Assertions.assertThat(parameters).hasSize(3);

        var id = parameters.get(0);
        var data = parameters.get(1);
        var money = parameters.get(2);

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(id.name()).isEqualTo("_id");
            soft.assertThat(id.type()).isEqualTo(String.class);
            soft.assertThat(id.converter()).isEmpty();
            soft.assertThat(id.mappingType()).isEqualTo(MappingType.DEFAULT);

            soft.assertThat(data.name()).isEqualTo("data");
            soft.assertThat(data.type()).isEqualTo(Map.class);
            soft.assertThat(id.mappingType()).isEqualTo(MappingType.DEFAULT);

            soft.assertThat(money.name()).isEqualTo("money");
            soft.assertThat(money.type()).isEqualTo(Money.class);
            soft.assertThat(id.mappingType()).isEqualTo(MappingType.DEFAULT);
            soft.assertThat(money.converter()).isNotEmpty();
        });

    }
}
