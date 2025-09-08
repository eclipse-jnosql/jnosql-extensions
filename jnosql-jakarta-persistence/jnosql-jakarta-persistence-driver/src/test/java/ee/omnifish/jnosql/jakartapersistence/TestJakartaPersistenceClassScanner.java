/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.data.repository.DataRepository;

import java.util.Map;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.mapping.metadata.JakartaPersistenceClassScanner;

/**
 *
 * @author Ondro Mihalyi
 */
public class TestJakartaPersistenceClassScanner implements JakartaPersistenceClassScanner {

    static Set<Class<?>> entities = Set.of();
    static Set<Class<?>> repositories = Set.of();
    static Set<Class<?>> embeddables = Set.of();
    static Map<Class<?>, Set<Class<?>>> repositoriesByClass = Map.of();
    static Set<Class<?>> standardRepositories = Set.of();
    static Set<Class<?>> customRepositories = Set.of();

    @Override
    public Set<Class<?>> entities() {
        return entities;
    }
    @Override
    public Set<Class<?>> repositories() {
        return repositories;
    }

    @Override
    public Set<Class<?>> embeddables() {
        return embeddables;
    }

    @Override
    public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> type) {
        return repositoriesByClass.getOrDefault(type, Set.of());
    }

    @Override
    public Set<Class<?>> repositoriesStandard() {
        return standardRepositories;
    }

    @Override
    public Set<Class<?>> customRepositories() {
        return customRepositories;
    }

}
