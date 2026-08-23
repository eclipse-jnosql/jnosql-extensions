/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and the Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 which accompanies this distribution.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.data.event.LifecycleEvent;
import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.TypeLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class LiteLifecycleEventHandlerTest {

    private Event<Object> events;
    private Event<Object> selectedEvents;
    private LiteLifecycleEventHandler handler;
    private TestLifecycleEventTypes.TestEntity entity;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        events = mock(Event.class);
        selectedEvents = mock(Event.class);
        handler = new LiteLifecycleEventHandler(events);
        entity = new TestLifecycleEventTypes.TestEntity("Ada");
    }

    @Nested
    @DisplayName("When inserting an entity")
    class WhenTheInsertion {

        @Test
        @DisplayName("Should publish the pre-insert event")
        void shouldPublishThePreInsertEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.PRE_INSERT,
                    handler::preInsert,
                    PreInsertEvent.class
            );
        }

        @Test
        @DisplayName("Should publish the post-insert event")
        void shouldPublishThePostInsertEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.POST_INSERT,
                    handler::postInsert,
                    PostInsertEvent.class
            );
        }
    }

    @Nested
    @DisplayName("When updating an entity")
    class WhenTheUpdate {

        @Test
        @DisplayName("Should publish the pre-update event")
        void shouldPublishThePreUpdateEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.PRE_UPDATE,
                    handler::preUpdate,
                    PreUpdateEvent.class
            );
        }

        @Test
        @DisplayName("Should publish the post-update event")
        void shouldPublishThePostUpdateEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.POST_UPDATE,
                    handler::postUpdate,
                    PostUpdateEvent.class
            );
        }
    }

    @Nested
    @DisplayName("When upserting an entity")
    class WhenTheUpsert {

        @Test
        @DisplayName("Should publish the pre-upsert event")
        void shouldPublishThePreUpsertEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.PRE_UPSERT,
                    handler::preUpsert,
                    PreUpsertEvent.class
            );
        }

        @Test
        @DisplayName("Should publish the post-upsert event")
        void shouldPublishThePostUpsertEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.POST_UPSERT,
                    handler::postUpsert,
                    PostUpsertEvent.class
            );
        }
    }

    @Nested
    @DisplayName("When removing an entity")
    class WhenTheRemoval {

        @Test
        @DisplayName("Should publish the pre-delete event")
        void shouldPublishThePreDeleteEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.PRE_DELETE,
                    handler::preDelete,
                    PreDeleteEvent.class
            );
        }

        @Test
        @DisplayName("Should publish the post-delete event")
        void shouldPublishThePostDeleteEvent() {
            assertPublishedEvent(
                    TestLifecycleEventTypes.POST_DELETE,
                    handler::postDelete,
                    PostDeleteEvent.class
            );
        }
    }

    @Nested
    @DisplayName("When validating lifecycle event publication")
    class WhenThePublicationIsValidated {

        @Test
        @DisplayName("Should reject a null entity")
        void shouldRejectANullEntity() {
            assertThatNullPointerException()
                    .as("null lifecycle entity")
                    .isThrownBy(() -> handler.preInsert(null))
                    .withMessage("entity is required");

            verifyNoInteractions(events);
        }

        @Test
        @DisplayName("Should reject an entity without generated event types")
        void shouldRejectAnEntityWithoutGeneratedEventTypes() {
            var unregistered = new UnregisteredEntity();

            assertThatIllegalStateException()
                    .as("entity without lifecycle event types")
                    .isThrownBy(() -> handler.preInsert(unregistered))
                    .withMessageContaining(UnregisteredEntity.class.getName());

            verifyNoInteractions(events);
        }

        @Test
        @DisplayName("Should reject a null event dispatcher")
        void shouldRejectANullEventDispatcher() {
            assertThatNullPointerException()
                    .as("null CDI event dispatcher")
                    .isThrownBy(() -> new LiteLifecycleEventHandler(null))
                    .withMessage("events is required");
        }

        @Test
        @DisplayName("Should use the handler class loader when the context class loader is absent")
        void shouldUseTheHandlerClassLoaderWhenTheContextClassLoaderIsAbsent() {
            var thread = Thread.currentThread();
            var originalClassLoader = thread.getContextClassLoader();
            thread.setContextClassLoader(null);

            try {
                handler = new LiteLifecycleEventHandler(events);

                assertPublishedEvent(
                        TestLifecycleEventTypes.PRE_INSERT,
                        handler::preInsert,
                        PreInsertEvent.class
                );
            } finally {
                thread.setContextClassLoader(originalClassLoader);
            }
        }

        @Test
        @DisplayName("Should reject duplicate event type providers")
        void shouldRejectDuplicateEventTypeProviders(@org.junit.jupiter.api.io.TempDir Path directory)
                throws Exception {
            var service = directory.resolve("META-INF/services/" + LifecycleEventTypes.class.getName());
            Files.createDirectories(service.getParent());
            Files.writeString(service, DuplicateLifecycleEventTypes.class.getName());
            var thread = Thread.currentThread();
            var originalClassLoader = thread.getContextClassLoader();

            try (var classLoader = new URLClassLoader(
                    new java.net.URL[]{directory.toUri().toURL()},
                    originalClassLoader)) {
                thread.setContextClassLoader(classLoader);

                assertThatIllegalStateException()
                        .as("duplicate lifecycle event type providers")
                        .isThrownBy(() -> new LiteLifecycleEventHandler(events))
                        .withMessageContaining(TestLifecycleEventTypes.TestEntity.class.getName());
            } finally {
                thread.setContextClassLoader(originalClassLoader);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void assertPublishedEvent(TypeLiteral<?> eventType,
                                      Consumer<TestLifecycleEventTypes.TestEntity> operation,
                                      Class<? extends LifecycleEvent> eventClass) {
        when(events.select((TypeLiteral) eventType)).thenReturn(selectedEvents);

        operation.accept(entity);

        verify(events).select((TypeLiteral) eventType);
        var event = ArgumentCaptor.forClass(Object.class);
        verify(selectedEvents).fire(event.capture());
        assertSoftly(softly -> {
            softly.assertThat(event.getValue())
                    .as("published lifecycle event type")
                    .isInstanceOf(eventClass);
            softly.assertThat(((LifecycleEvent<?>) event.getValue()).entity())
                    .as("published lifecycle event entity")
                    .isSameAs(entity);
        });
    }

    private static final class UnregisteredEntity {
    }

    public static final class DuplicateLifecycleEventTypes
            implements LifecycleEventTypes<TestLifecycleEventTypes.TestEntity> {

        private final TestLifecycleEventTypes delegate = new TestLifecycleEventTypes();

        @Override
        public Class<TestLifecycleEventTypes.TestEntity> type() {
            return delegate.type();
        }

        @Override
        public TypeLiteral<PreInsertEvent<TestLifecycleEventTypes.TestEntity>> preInsert() {
            return delegate.preInsert();
        }

        @Override
        public TypeLiteral<PostInsertEvent<TestLifecycleEventTypes.TestEntity>> postInsert() {
            return delegate.postInsert();
        }

        @Override
        public TypeLiteral<PreUpdateEvent<TestLifecycleEventTypes.TestEntity>> preUpdate() {
            return delegate.preUpdate();
        }

        @Override
        public TypeLiteral<PostUpdateEvent<TestLifecycleEventTypes.TestEntity>> postUpdate() {
            return delegate.postUpdate();
        }

        @Override
        public TypeLiteral<PreUpsertEvent<TestLifecycleEventTypes.TestEntity>> preUpsert() {
            return delegate.preUpsert();
        }

        @Override
        public TypeLiteral<PostUpsertEvent<TestLifecycleEventTypes.TestEntity>> postUpsert() {
            return delegate.postUpsert();
        }

        @Override
        public TypeLiteral<PreDeleteEvent<TestLifecycleEventTypes.TestEntity>> preDelete() {
            return delegate.preDelete();
        }

        @Override
        public TypeLiteral<PostDeleteEvent<TestLifecycleEventTypes.TestEntity>> postDelete() {
            return delegate.postDelete();
        }
    }
}
