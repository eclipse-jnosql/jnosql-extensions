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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.spring.boot.autoconfigure;

import jakarta.data.event.LifecycleEvent;
import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class SpringLifecycleEventHandler implements LifecycleEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public SpringLifecycleEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    private <T> void publishEvent(T entity, Function<T,? extends LifecycleEvent<T>> toEvent) {
        ofNullable(entity)
                .map(toEvent)
                .ifPresent(this.eventPublisher::publishEvent);
    }

    @Override
    public <T> void preDelete(T entity) {
        publishEvent(entity, PreDeleteEvent::new);
    }

    @Override
    public <T> void preInsert(T entity) {
        publishEvent(entity, PreInsertEvent::new);
    }

    @Override
    public <T> void preUpdate(T entity) {
        publishEvent(entity, PreUpdateEvent::new);
    }

    @Override
    public <T> void preUpsert(T entity) {
        publishEvent(entity, PreUpsertEvent::new);
    }

    @Override
    public <T> void postDelete(T entity) {
        publishEvent(entity, PostDeleteEvent::new);
    }

    @Override
    public <T> void postInsert(T entity) {
        publishEvent(entity, PostInsertEvent::new);
    }

    @Override
    public <T> void postUpdate(T entity) {
        publishEvent(entity, PostUpdateEvent::new);
    }

    @Override
    public <T> void postUpsert(T entity) {
        publishEvent(entity, PostUpsertEvent::new);
    }
}
