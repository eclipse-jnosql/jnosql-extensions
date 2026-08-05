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

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

@ApplicationScoped
public class LiteLifecycleEventHandler implements LifecycleEventHandler {
    @Override
    public <T> void preDelete(T entity) {

    }

    @Override
    public <T> void preInsert(T entity) {

    }

    @Override
    public <T> void preUpdate(T entity) {

    }

    @Override
    public <T> void preUpsert(T entity) {

    }

    @Override
    public <T> void postDelete(T entity) {

    }

    @Override
    public <T> void postInsert(T entity) {

    }

    @Override
    public <T> void postUpdate(T entity) {

    }

    @Override
    public <T> void postUpsert(T entity) {

    }
}
