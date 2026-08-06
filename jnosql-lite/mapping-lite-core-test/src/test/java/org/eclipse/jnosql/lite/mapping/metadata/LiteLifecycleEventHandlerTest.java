/*
 *  Copyright (c) 2026 Otávio Santana and others
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

import jakarta.inject.Inject;
import org.eclipse.jnosql.lite.mapping.entities.Car;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddBeanClasses({
        LiteLifecycleEventHandler.class,
        CarLifecycleEventObserver.class
})
@DisplayName("Lite lifecycle event handler")
class LiteLifecycleEventHandlerTest {

    @Inject
    private LifecycleEventHandler listener;

    @Inject
    private CarLifecycleEventObserver observer;

    private Car car;

    @BeforeEach
    void setUp() {
        this.car = new Car("roadster", "Model 3");
        this.observer.reset();
    }

    @Nested
    @DisplayName("When firing insert events")
    class WhenInsert {

        @Test
        @DisplayName("Should deliver a typed pre-insert event to the entity observer")
        void shouldFirePreInsertEvent() {
            // when
            listener.preInsert(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.PRE_INSERT,
                                    car));
        }

        @Test
        @DisplayName("Should deliver a typed post-insert event to the entity observer")
        void shouldFirePostInsertEvent() {
            // when
            listener.postInsert(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.POST_INSERT,
                                    car));
        }
    }

    @Nested
    @DisplayName("When firing update events")
    class WhenUpdate {

        @Test
        @DisplayName("Should deliver a typed pre-update event to the entity observer")
        void shouldFirePreUpdateEvent() {
            // when
            listener.preUpdate(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.PRE_UPDATE,
                                    car));
        }

        @Test
        @DisplayName("Should deliver a typed post-update event to the entity observer")
        void shouldFirePostUpdateEvent() {
            // when
            listener.postUpdate(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.POST_UPDATE,
                                    car));
        }
    }

    @Nested
    @DisplayName("When firing upsert events")
    class WhenUpsert {

        @Test
        @DisplayName("Should deliver a typed pre-upsert event to the entity observer")
        void shouldFirePreUpsertEvent() {
            // when
            listener.preUpsert(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.PRE_UPSERT,
                                    car));
        }

        @Test
        @DisplayName("Should deliver a typed post-upsert event to the entity observer")
        void shouldFirePostUpsertEvent() {
            // when
            listener.postUpsert(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.POST_UPSERT,
                                    car));
        }
    }

    @Nested
    @DisplayName("When firing delete events")
    class WhenDelete {

        @Test
        @DisplayName("Should deliver a typed pre-delete event to the entity observer")
        void shouldFirePreDeleteEvent() {
            // when
            listener.preDelete(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.PRE_DELETE,
                                    car));
        }

        @Test
        @DisplayName("Should deliver a typed post-delete event to the entity observer")
        void shouldFirePostDeleteEvent() {
            // when
            listener.postDelete(car);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedLifecycleEvent(
                                    ObservedLifecycleEventType.POST_DELETE,
                                    car));
        }
    }
}
