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

/**
 * Provides concrete CDI event types for the lifecycle events associated with
 * a specific entity type.
 * <p>
 * Implementations are typically generated at compile time for classes
 * annotated as entities. Each implementation supplies the concrete
 * {@link TypeLiteral} instances required to fire strongly typed Jakarta Data
 * lifecycle events through CDI.
 * </p>
 * <p>
 * The entity type returned by {@link #type()} is used as the lookup key when
 * selecting the provider for a runtime entity instance.
 * </p>
 *
 * @param <T> the entity type supported by this provider
 */
public interface LifecycleEventTypes<T> {

    /**
     * Returns the entity type supported by this provider.
     *
     * @return the entity class
     */
    Class<T> type();

    /**
     * Returns the concrete type literal for the event fired before an entity
     * is inserted.
     *
     * @return the pre-insert event type
     */
    TypeLiteral<PreInsertEvent<T>> preInsert();

    /**
     * Returns the concrete type literal for the event fired after an entity
     * has been successfully inserted.
     *
     * @return the post-insert event type
     */
    TypeLiteral<PostInsertEvent<T>> postInsert();

    /**
     * Returns the concrete type literal for the event fired before an entity
     * is updated.
     *
     * @return the pre-update event type
     */
    TypeLiteral<PreUpdateEvent<T>> preUpdate();

    /**
     * Returns the concrete type literal for the event fired after an entity
     * has been successfully updated.
     *
     * @return the post-update event type
     */
    TypeLiteral<PostUpdateEvent<T>> postUpdate();

    /**
     * Returns the concrete type literal for the event fired before an entity
     * is saved using upsert semantics.
     *
     * @return the pre-upsert event type
     */
    TypeLiteral<PreUpsertEvent<T>> preUpsert();

    /**
     * Returns the concrete type literal for the event fired after an entity
     * has been successfully saved using upsert semantics.
     *
     * @return the post-upsert event type
     */
    TypeLiteral<PostUpsertEvent<T>> postUpsert();

    /**
     * Returns the concrete type literal for the event fired before an entity
     * is deleted.
     *
     * @return the pre-delete event type
     */
    TypeLiteral<PreDeleteEvent<T>> preDelete();

    /**
     * Returns the concrete type literal for the event fired after an entity
     * has been successfully deleted.
     *
     * @return the post-delete event type
     */
    TypeLiteral<PostDeleteEvent<T>> postDelete();
}