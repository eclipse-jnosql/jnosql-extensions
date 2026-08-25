/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.validation;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
class DatabaseMockProducer implements Supplier<DatabaseManager> {

    @Override
    @Produces
    @Database(DatabaseType.DOCUMENT)
    public DatabaseManager get() {
        DatabaseManager collectionManager = Mockito.mock(DatabaseManager.class);

        var entity = CommunicationEntity.of("person");
        entity.add(Element.of("name", "Ada"));
        entity.add(Element.of("age", 30));
        entity.add(Element.of("salary", BigDecimal.TEN));
        entity.add(Element.of("phones", singletonList("22342342")));

        when(collectionManager.insert(Mockito.any(CommunicationEntity.class))).thenReturn(entity);
        when(collectionManager.update(Mockito.any(CommunicationEntity.class))).thenReturn(entity);
        return collectionManager;
    }

    @Produces
    @Database(DatabaseType.COLUMN)
    public DatabaseManager get2() {
        DatabaseManager manager = Mockito.mock(DatabaseManager.class);

        CommunicationEntity entity = CommunicationEntity.of("person");
        entity.add(Element.of("name", "Ada"));
        entity.add(Element.of("age", 30));
        entity.add(Element.of("salary", BigDecimal.TEN));
        entity.add(Element.of("phones", singletonList("22342342")));
        when(manager.insert(Mockito.any(CommunicationEntity.class))).thenReturn(entity);
        when(manager.update(Mockito.any(CommunicationEntity.class))).thenReturn(entity);

        return manager;
    }
}
