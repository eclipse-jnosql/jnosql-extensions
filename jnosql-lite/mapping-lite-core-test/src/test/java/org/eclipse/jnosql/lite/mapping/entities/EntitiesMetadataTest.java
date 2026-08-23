/*
 *  Copyright (c) 2020 Otávio Santana and others
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

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThat;

public class EntitiesMetadataTest {

    private EntitiesMetadata mappings;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
    }

    @Nested
    @DisplayName("When the generated metadata registry is used")
    class WhenTheGeneratedMetadataRegistryIsUsed {


        @Test
        @DisplayName("Should return null value when is null")
        void shouldReturnNPEWhenIsNull() {
            assertThatNullPointerException().as("null input rejection").isThrownBy(() -> mappings.get(null));
        }


        @Test
        @DisplayName("Should return from class")
        void shouldReturnFromClass() {
            EntityMetadata entityMetadata = mappings.get(Animal.class);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(entityMetadata).as("value of entityMetadata").isNotNull();
                soft.assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Animal.class);
                soft.assertThat(mappings.get(Car.class).type()).as("value of mappings.get(Car.class).type()").isEqualTo(Car.class);
                soft.assertThat(mappings.get(Person.class).type()).as("value of mappings.get(Person.class).type()").isEqualTo(Person.class);
            });
        }


        @Test
        @DisplayName("Should return from name")
        void shouldReturnFromName() {
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(mappings.findByName("kind").type()).as("value of mappings.findByName(\"kind\").type()").isEqualTo(Animal.class);
                soft.assertThat(mappings.findByName("car").type()).as("value of mappings.findByName(\"car\").type()").isEqualTo(Car.class);
                soft.assertThat(mappings.findByName("Person").type()).as("value of mappings.findByName(\"Person\").type()").isEqualTo(Person.class);
            });
        }
    }
}
