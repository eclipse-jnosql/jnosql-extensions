/*
 *  Copyright (c) 2023 Otávio Santana and others
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
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.IdNotFoundException;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThat;


@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(LiteEntitiesMetadata.class)
@AddExtensions({KeyValueExtension.class})
public class KeyValueEntityConverterTest {

    @Inject
    private KeyValueEntityConverter converter;

    @Nested
    @DisplayName("When an entity is mapped to a key-value entry")
    class WhenTheEntityIsMappedToAKeyValueEntry {


        @Test
        @DisplayName("Should reject a null entity")
        void shouldRejectNullEntity() {
            assertThatNullPointerException().as("null input rejection").isThrownBy(() -> converter.toKeyValue(null));
        }


        @Test
        @DisplayName("Should reject an entity when identifier metadata is missing")
        void shouldRejectEntityWhenIdentifierMetadataIsMissing() {
            assertThatExceptionOfType(IdNotFoundException.class).as("expected exception").isThrownBy(() -> converter.toKeyValue(new Worker()));
        }


        @Test
        @DisplayName("Should reject an entity when its identifier is null")
        void shouldRejectEntityWhenIdentifierIsNull() {
            assertThatNullPointerException().as("null input rejection").isThrownBy(() -> {
                User user = new User(null, "name", 24);
                converter.toKeyValue(user);
            });
        }


        @Test
        @DisplayName("Should convert to key value")
        void shouldConvertToKeyValue() {
            User user = new User("nickname", "name", 24);
            KeyValueEntity keyValueEntity = converter.toKeyValue(user);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(keyValueEntity.key()).as("value of keyValueEntity.key()").isEqualTo("nickname");
                soft.assertThat(keyValueEntity.value()).as("value of keyValueEntity.value()").isEqualTo(user);
            });
        }


        @Test
        @DisplayName("Should convert the identifier to a key")
        void shouldConvertIdentifierToKey() {
            Car car = new Car();
            car.setPlate(Plate.of("123-BRL"));
            car.setName("Ferrari");
            KeyValueEntity entity = converter.toKeyValue(car);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entity.key()).as("value of entity.key()").isEqualTo("123-BRL");
                soft.assertThat(entity.value()).as("value of entity.value()").isEqualTo(car);
            });
        }


        @Test
        @DisplayName("Should preserve the identifier type as the key")
        void shouldPreserveIdentifierTypeAsKey() {
            Person person = Person.builder().withId(123L).withName("Ada").build();
            KeyValueEntity entity = converter.toKeyValue(person);
            assertThat(entity.key()).as("value of entity.key()").isEqualTo(123L);
        }
    }

    @Nested
    @DisplayName("When a key-value entry is mapped to an entity")
    class WhenTheKeyValueEntryIsMappedToAnEntity {


        @Test
        @DisplayName("Should reject a null key-value entry")
        void shouldRejectNullKeyValueEntry() {
            assertThatNullPointerException().as("null input rejection").isThrownBy(() -> converter.toEntity(User.class, null));
        }


        @Test
        @DisplayName("Should reject a null entity type")
        void shouldRejectNullEntityType() {
            assertThatNullPointerException().as("null input rejection").isThrownBy(() -> converter.toEntity(null,
                    KeyValueEntity.of("user", new User("nickname", "name", 21))));
        }


        @Test
        @DisplayName("Should reject a key-value entry when identifier metadata is missing")
        void shouldRejectKeyValueEntryWhenIdentifierMetadataIsMissing() {
            assertThatExceptionOfType(IdNotFoundException.class).as("expected exception").isThrownBy(() -> converter.toEntity(Worker.class,
                    KeyValueEntity.of("worker", new Worker())));
        }


        @Test
        @DisplayName("Should convert to entity")
        void shouldConvertToEntity() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class,
                    KeyValueEntity.of("user", expectedUser));
            assertThat(user).as("value of user").isEqualTo(expectedUser);
        }


        @Test
        @DisplayName("Should restore the identifier from the key")
        void shouldRestoreIdentifierFromKey() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class,
                    KeyValueEntity.of("nickname", new User(null, "name", 21)));
            assertThat(user).as("value of user").isEqualTo(expectedUser);
        }


        @Test
        @DisplayName("Should prefer the key when the stored identifier differs")
        void shouldPreferKeyWhenStoredIdentifierDiffers() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class,
                    KeyValueEntity.of("nickname", new User("newName", "name", 21)));
            assertThat(user).as("value of user").isEqualTo(expectedUser);
        }


        @Test
        @DisplayName("Should convert value to entity")
        void shouldConvertValueToEntity() {
            User expectedUser = new User("nickname", "name", 21);
            User user = converter.toEntity(User.class, KeyValueEntity.of("nickname", Value.of(expectedUser)));
            assertThat(user).as("value of user").isEqualTo(expectedUser);
        }


        @Test
        @DisplayName("Should restore a converted identifier")
        void shouldRestoreConvertedIdentifier() {
            Car car = new Car();
            car.setName("Ferrari");

            Car ferrari = converter.toEntity(Car.class, KeyValueEntity.of("123-BRL", car));
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(ferrari.getPlate()).as("value of ferrari.getPlate()").isEqualTo(Plate.of("123-BRL"));
                soft.assertThat(ferrari.getName()).as("value of ferrari.getName()").isEqualTo(car.getName());
            });
        }


        @Test
        @DisplayName("Should coerce the key to the identifier type")
        void shouldCoerceKeyToIdentifierType() {

            Person person = Person.builder().withName("Ada").build();
            Person ada = converter.toEntity(Person.class, KeyValueEntity.of("123", person));

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(ada.getId()).as("value of ada.getId()").isEqualTo(123L);
                soft.assertThat(person.getName()).as("value of person.getName()").isEqualTo(ada.getName());
            });
        }
    }

}
