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

import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.util.TypeLiteral;

public final class TestLifecycleEventTypes
        implements LifecycleEventTypes<TestLifecycleEventTypes.TestEntity> {

    static final TypeLiteral<PreInsertEvent<TestEntity>> PRE_INSERT = new TypeLiteral<>() {
    };
    static final TypeLiteral<PostInsertEvent<TestEntity>> POST_INSERT = new TypeLiteral<>() {
    };
    static final TypeLiteral<PreUpdateEvent<TestEntity>> PRE_UPDATE = new TypeLiteral<>() {
    };
    static final TypeLiteral<PostUpdateEvent<TestEntity>> POST_UPDATE = new TypeLiteral<>() {
    };
    static final TypeLiteral<PreUpsertEvent<TestEntity>> PRE_UPSERT = new TypeLiteral<>() {
    };
    static final TypeLiteral<PostUpsertEvent<TestEntity>> POST_UPSERT = new TypeLiteral<>() {
    };
    static final TypeLiteral<PreDeleteEvent<TestEntity>> PRE_DELETE = new TypeLiteral<>() {
    };
    static final TypeLiteral<PostDeleteEvent<TestEntity>> POST_DELETE = new TypeLiteral<>() {
    };

    @Override
    public Class<TestEntity> type() {
        return TestEntity.class;
    }

    @Override
    public TypeLiteral<PreInsertEvent<TestEntity>> preInsert() {
        return PRE_INSERT;
    }

    @Override
    public TypeLiteral<PostInsertEvent<TestEntity>> postInsert() {
        return POST_INSERT;
    }

    @Override
    public TypeLiteral<PreUpdateEvent<TestEntity>> preUpdate() {
        return PRE_UPDATE;
    }

    @Override
    public TypeLiteral<PostUpdateEvent<TestEntity>> postUpdate() {
        return POST_UPDATE;
    }

    @Override
    public TypeLiteral<PreUpsertEvent<TestEntity>> preUpsert() {
        return PRE_UPSERT;
    }

    @Override
    public TypeLiteral<PostUpsertEvent<TestEntity>> postUpsert() {
        return POST_UPSERT;
    }

    @Override
    public TypeLiteral<PreDeleteEvent<TestEntity>> preDelete() {
        return PRE_DELETE;
    }

    @Override
    public TypeLiteral<PostDeleteEvent<TestEntity>> postDelete() {
        return POST_DELETE;
    }

    public record TestEntity(String name) {
    }
}
