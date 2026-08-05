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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Fires strongly typed Jakarta Data lifecycle events using event type
 * information generated at compile time.
 * <p>
 * The handler discovers {@link LiteLifecycleEventTypeProviderElement}
 * implementations through the Java Service Provider Interface. Providers are
 * loaded once when this application-scoped bean is created and indexed by
 * their supported entity type.
 * </p>
 * <p>
 * Each lifecycle operation uses the concrete {@link TypeLiteral} supplied by
 * the provider associated with the runtime entity class. This allows CDI
 * observers to receive events such as {@code PreInsertEvent<Customer>} without
 * constructing parameterized types dynamically at runtime.
 * </p>
 */
@ApplicationScoped
class LiteLifecycleEventHandler implements LifecycleEventHandler {


    private final Event<Object> events;

    private final Map<Class<?>, LiteLifecycleEventTypeProviderElement<?>>
            providers;


    /**
     * Creates the handler and loads generated lifecycle event providers using
     * the current thread context class loader.
     *
     * @param events the CDI event dispatcher
     */
    @Inject
    LiteLifecycleEventHandler(@Any Event<Object> events) {
        this(events, contextClassLoader());
    }

    LiteLifecycleEventHandler() {
        this.events = null;
        this.providers = null;
    }

    /**
     * Creates the handler using the supplied class loader.
     * <p>
     * This constructor primarily allows tests and runtime integrations to
     * control which SPI providers are visible.
     * </p>
     *
     * @param events      the CDI event dispatcher
     * @param classLoader the class loader used to discover providers
     */
    LiteLifecycleEventHandler(Event<Object> events, ClassLoader classLoader) {

        this.events = Objects.requireNonNull(events, "events is required");
        this.providers = loadProviders(Objects.requireNonNull(classLoader, "classLoader is required"));
    }

    @Override
    public <T> void preDelete(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);
        fire(new PreDeleteEvent<>(safeEntity), provider.preDelete());
    }

    @Override
    public <T> void preInsert(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PreInsertEvent<>(safeEntity), provider.preInsert());
    }

    @Override
    public <T> void preUpdate(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PreUpdateEvent<>(safeEntity), provider.preUpdate());
    }

    @Override
    public <T> void preUpsert(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PreUpsertEvent<>(safeEntity), provider.preUpsert());
    }

    @Override
    public <T> void postDelete(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PostDeleteEvent<>(safeEntity), provider.postDelete());
    }

    @Override
    public <T> void postInsert(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PostInsertEvent<>(safeEntity), provider.postInsert());
    }

    @Override
    public <T> void postUpdate(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PostUpdateEvent<>(safeEntity), provider.postUpdate());
    }

    @Override
    public <T> void postUpsert(T entity) {
        T safeEntity = requireEntity(entity);
        var provider = provider(safeEntity);

        fire(new PostUpsertEvent<>(safeEntity), provider.postUpsert());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void fire(
            Object event,
            TypeLiteral<?> eventType) {

        /*
         * The generated provider guarantees that the TypeLiteral and event
         * instance use the same entity type. The raw cast is isolated here
         * because Event<Object> cannot express that relationship at compile
         * time.
         */
        events.select((TypeLiteral) eventType)
                .fire(event);
    }

    @SuppressWarnings("unchecked")
    private <T> LiteLifecycleEventTypeProviderElement<T> provider(T entity) {

        LiteLifecycleEventTypeProviderElement<?> provider =
                providers.get(entity.getClass());

        if (provider == null) {
            throw new IllegalStateException(
                    "No lifecycle event type provider was found for entity "
                            + entity.getClass().getName());
        }

        /*
         * The cast is safe because every provider is indexed using the class
         * returned by that same provider's type() method.
         */
        return (LiteLifecycleEventTypeProviderElement<T>) provider;
    }

    private static Map<Class<?>, LiteLifecycleEventTypeProviderElement<?>> loadProviders(ClassLoader classLoader) {

        Map<Class<?>, LiteLifecycleEventTypeProviderElement<?>> providers = new HashMap<>();

        ServiceLoader.load(LiteLifecycleEventTypeProviderElement.class,
                        classLoader).forEach(provider -> register(providers, provider));

        return Map.copyOf(providers);
    }

    private static void register(Map<Class<?>, LiteLifecycleEventTypeProviderElement<?>> providers, LiteLifecycleEventTypeProviderElement<?> provider) {

        Objects.requireNonNull(provider, "provider is required");

        Class<?> entityType = Objects.requireNonNull(provider.type(), "provider entity type is required");

        LiteLifecycleEventTypeProviderElement<?> previous =
                providers.putIfAbsent(entityType, provider);

        if (previous != null) {
            throw new IllegalStateException(
                    "Multiple lifecycle event type providers were found for "
                            + entityType.getName()
                            + ": "
                            + previous.getClass().getName()
                            + " and "
                            + provider.getClass().getName());
        }
    }

    private static ClassLoader contextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return LiteLifecycleEventHandler.class.getClassLoader();
    }

    private static <T> T requireEntity(T entity) {
        return Objects.requireNonNull(entity, "entity is required");
    }
}
