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
import org.eclipse.jnosql.mapping.metadata.MapParameterMetaData;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRecordTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(DocumentRecord.class);
    }

    @Nested
    @DisplayName("When the document record metadata is inspected")
    class WhenTheDocumentRecordMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("DocumentRecord");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(DocumentRecord.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(DocumentRecord.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(DocumentRecord.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isTrue();
        }


        @Test
        @DisplayName("Should create instance")
        void shouldCreateInstance() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            ConstructorBuilder constructorBuilder = ConstructorBuilder.of(constructor);
            constructorBuilder.add("Ada");
            constructorBuilder.add(Map.of("name", "Ada"));
            constructorBuilder.add(Money.of("EUR 10"));
            DocumentRecord record = constructorBuilder.build();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(record).as("value of record").isNotNull();
                s.assertThat(record.id()).as("value of record.id()").isEqualTo("Ada");
                s.assertThat(record.data()).as("value of record.data()").isEqualTo(Map.of("name", "Ada"));
                s.assertThat(record.money()).as("value of record.money()").isEqualTo(Money.of("EUR 10"));
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            DocumentRecord documentRecord = new DocumentRecord("Ada", Map.of("name", "Ada"), new Money("EUR", BigDecimal.TEN));
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            FieldMetadata id = groupByName.get("_id");
            FieldMetadata data = groupByName.get("data");
            FieldMetadata money = groupByName.get("money");
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(id.read(documentRecord)).as("value of id.read(documentRecord)").isEqualTo("Ada");
                s.assertThat(data.read(documentRecord)).as("value of data.read(documentRecord)").isEqualTo(Map.of("name", "Ada"));
                s.assertThat(money.read(documentRecord)).as("value of money.read(documentRecord)").isEqualTo(new Money("EUR", BigDecimal.TEN));
            });
        }


        @Test
        @DisplayName("Should check constructor")
        void shouldCheckConstructor() {
            ConstructorMetadata constructor = entityMetadata.constructor();
            assertThat(constructor.isDefault()).as("value of constructor.isDefault()").isFalse();
            List<ParameterMetaData> parameters = constructor.parameters();
            assertThat(parameters).as("value of parameters").hasSize(3);

            var id = parameters.get(0);
            var data = (MapParameterMetaData) parameters.get(1);
            var money = parameters.get(2);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(id.name()).as("value of id.name()").isEqualTo("_id");
                soft.assertThat(id.type()).as("value of id.type()").isEqualTo(String.class);
                soft.assertThat(id.converter()).as("value of id.converter()").isEmpty();
                soft.assertThat(id.mappingType()).as("value of id.mappingType()").isEqualTo(MappingType.DEFAULT);

                soft.assertThat(data.name()).as("value of data.name()").isEqualTo("data");
                soft.assertThat(data.type()).as("value of data.type()").isEqualTo(Map.class);
                soft.assertThat(data.mappingType()).as("value of data.mappingType()").isEqualTo(MappingType.MAP);
                soft.assertThat(data.isEmbeddable()).as("value of data.isEmbeddable()").isFalse();

                soft.assertThat(money.name()).as("value of money.name()").isEqualTo("money");
                soft.assertThat(money.type()).as("value of money.type()").isEqualTo(Money.class);
                soft.assertThat(id.mappingType()).as("value of id.mappingType()").isEqualTo(MappingType.DEFAULT);
                soft.assertThat(money.converter()).as("value of money.converter()").isNotEmpty();
            });
        }
    }
}
