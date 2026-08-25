/*
 *  Copyright (c) 2020 Otávio Santana and others
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

import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntitiesMetadataTest {

    private EntitiesMetadata mappings;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
    }

    @Test
    void shouldReturnNPEWhenIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> this.mappings.get(null));
    }

    @Test
    void shouldReturnFromClass() {
        EntityMetadata entityMetadata = this.mappings.get(Animal.class);
        Assertions.assertNotNull(entityMetadata);
        Assertions.assertEquals(Animal.class, entityMetadata.type());
        Assertions.assertEquals(Car.class, mappings.get(Car.class).type());
        Assertions.assertEquals(Person.class, mappings.get(Person.class).type());
    }

    @Test
    void shouldReturnFromName() {
        Assertions.assertEquals(Animal.class, mappings.findByName("kind").type());
        Assertions.assertEquals(Car.class, mappings.findByName("car").type());
        Assertions.assertEquals(Person.class, mappings.findByName("Person").type());
    }
}
