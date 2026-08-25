/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
 *   Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping.reflection;

import jakarta.data.repository.DataRepository;

import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.mapping.metadata.JakartaPersistenceClassScanner;


public class ReflectionJakartaPersistenceClassScanner implements JakartaPersistenceClassScanner {

    @Override
    public Set<Class<?>> entities() {
        return PersistenceClassScannerSingleton.INSTANCE.entities();
    }

    @Override
    public Set<Class<?>> repositories() {
        return PersistenceClassScannerSingleton.INSTANCE.repositories();
    }

    @Override
    public Set<Class<?>> embeddables() {
        return PersistenceClassScannerSingleton.INSTANCE.embeddables();
    }

    @Override
    public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter) {
        return PersistenceClassScannerSingleton.INSTANCE.repositories(filter);
    }

    @Override
    public Set<Class<?>> repositoriesStandard() {
        return PersistenceClassScannerSingleton.INSTANCE.repositoriesStandard();
    }

    @Override
    public Set<Class<?>> customRepositories() {
        return PersistenceClassScannerSingleton.INSTANCE.customRepositories();
    }

}
