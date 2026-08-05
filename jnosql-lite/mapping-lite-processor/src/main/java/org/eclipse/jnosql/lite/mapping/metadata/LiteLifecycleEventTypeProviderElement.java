/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.util.TypeLiteral;

public interface LiteLifecycleEventTypeProviderElement<T> {

    Class<T> type();

    TypeLiteral<PreInsertEvent<T>> preInsert();

    TypeLiteral<PostInsertEvent<T>> postInsert();

    TypeLiteral<PreUpdateEvent<T>> preUpdate();

    TypeLiteral<PostUpdateEvent<T>> postUpdate();

    TypeLiteral<PreUpsertEvent<T>> preUpsert();

    TypeLiteral<PostUpsertEvent<T>> postUpsert();

    TypeLiteral<PreDeleteEvent<T>> preDelete();

    TypeLiteral<PostDeleteEvent<T>> postDelete();
}