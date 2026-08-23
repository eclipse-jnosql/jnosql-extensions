/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.lite.mapping.entities.record.Guest;
import org.eclipse.jnosql.lite.mapping.entities.record.Hotel;
import org.eclipse.jnosql.lite.mapping.entities.record.HotelManager;
import org.eclipse.jnosql.lite.mapping.entities.record.Room;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static java.util.Arrays.asList;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(LiteEntitiesMetadata.class)
@AddExtensions({DocumentExtension.class})
public class DocumentEntityConverterRecordTest {

    @Inject
    private EntityConverter converter;

    @Nested
    @DisplayName("When a record is mapped to communication")
    class WhenTheRecordIsMappedToCommunication {


        @Test
        @DisplayName("Should map the record to a communication entity")
        void shouldConvertToCommunication() {
            Room room = new Room(1231, new Guest("Ada", "12321", asList("123", "321")));

            CommunicationEntity entity = converter.toCommunication(room);

            SoftAssertions.assertSoftly(s -> {
                s.assertThat(entity.name()).as("value of entity.name()").isEqualTo("Room");
                s.assertThat(entity.find("_id").orElseThrow().get()).as("value of entity.find(\"_id\").orElseThrow().get()").isEqualTo(1231);
                s.assertThat(entity.find("name")).as("value of entity.find(\"name\")").isPresent().get().isEqualTo(Element.of("name", "Ada"));
                s.assertThat(entity.find("document")).as("value of entity.find(\"document\")").isPresent().get().isEqualTo(Element.of("document", "12321"));
                s.assertThat(entity.find("phones")).as("value of entity.find(\"phones\")").isPresent().get().isEqualTo(Element.of("phones", asList("123", "321")));
            });
        }


        @Test
        @DisplayName("Should map the grouped record to a communication entity")
        void shouldConvertToCommunicationGroup() {
            Hotel hotel = new Hotel("123", new HotelManager("Ada", "12321"));

            CommunicationEntity entity = converter.toCommunication(hotel);

            SoftAssertions.assertSoftly(s -> {
                s.assertThat(entity.name()).as("value of entity.name()").isEqualTo("Hotel");
                s.assertThat(entity.find("_id").orElseThrow().get()).as("value of entity.find(\"_id\").orElseThrow().get()").isEqualTo("123");
                s.assertThat(entity.find("manager")).as("value of entity.find(\"manager\")").isNotEmpty();
            });
        }
    }

    @Nested
    @DisplayName("When communication is mapped to a record")
    class WhenTheCommunicationIsMappedToARecord {


        @Test
        @DisplayName("Should map a communication entity to the record")
        void shouldConvertToDocumentEntity() {
            var entity = CommunicationEntity.of("Room");
            entity.add("_id", 1231);
            entity.add("name", "Ada");
            entity.add("document", "12321");
            entity.add("phones", List.of("123", "321"));

            Room room = converter.toEntity(entity);

            SoftAssertions.assertSoftly(s -> {
                s.assertThat(room.number()).as("value of room.number()").isEqualTo(1231);
                s.assertThat(room.guest().name()).as("value of room.guest().name()").isEqualTo("Ada");
                s.assertThat(room.guest().document()).as("value of room.guest().document()").isEqualTo("12321");
                s.assertThat(room.guest().phones()).as("value of room.guest().phones()").containsExactly("123", "321");
            });
        }


        @Test
        @DisplayName("Should map grouped communication values to the record")
        void shouldConvertToDocumentEntityGroup() {
            var entity = CommunicationEntity.of("Hotel");
            entity.add("_id", "123");
            entity.add("manager", List.of(Element.of("name", "Ada"), Element.of("document", "12321")));

            Hotel room = converter.toEntity(entity);

            SoftAssertions.assertSoftly(s -> {
                s.assertThat(room.number()).as("value of room.number()").isEqualTo("123");
                s.assertThat(room.manager().name()).as("value of room.manager().name()").isEqualTo("Ada");
                s.assertThat(room.manager().document()).as("value of room.manager().document()").isEqualTo("12321");
            });
        }
    }

}
