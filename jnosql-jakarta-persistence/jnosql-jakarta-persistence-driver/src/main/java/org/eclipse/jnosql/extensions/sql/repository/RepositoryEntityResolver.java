/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;


/**
 * Resolves the entity type from a Jakarta Data repository interface.
 *
 * <p>This resolver inspects the generic type parameters of repository interfaces
 * such as {@code BasicRepository}, {@code CrudRepository}, and {@code DataRepository}
 * to extract the domain entity type.</p>
 *
 * <p>The resolution process traverses interface hierarchies recursively, ensuring
 * support for indirect inheritance and layered repository definitions.</p>
 *
 * <p>This class is stateless and thread-safe.</p>
 */
enum RepositoryEntityResolver {

INSTANCE;

    /**
     * Resolves the entity type (T) from the given repository class.
     *
     * @param repositoryClass the repository interface
     * @return the resolved entity class
     * @throws NullPointerException if {@code repositoryClass} is null
     * @throws IllegalArgumentException if the entity type cannot be resolved
     */
    public Class<?> resolveEntityType(Class<?> repositoryClass) {
        Objects.requireNonNull(repositoryClass, "repositoryClass is required");

        Optional<Class<?>> entityType = resolveFromClass(repositoryClass);

    }

    private Optional<Class<?>> resolveFromClass(Class<?> clazz) {

        for (Type type : clazz.getGenericInterfaces()) {
            Optional<Class<?>> resolved = resolveFromType(type);
            if (resolved.isPresent()) {
                return resolved;
            }
        }

        // 2. Inspect superclass (important for proxies / abstract layers)
        Type superclass = clazz.getGenericSuperclass();

        if (superclass != null) {
            Optional<Class<?>> resolved = resolveFromType(superclass);
            if (resolved.isPresent()) {
                return resolved;
            }

            if (superclass instanceof Class<?> superClass) {
                return resolveFromClass(superClass);
            }
        }

        return Optional.empty();
    }

    private Optional<Class<?>> resolveFromType(Type type) {

        if (type instanceof ParameterizedType parameterizedType) {

            Type rawType = parameterizedType.getRawType();

            if (rawType instanceof Class<?> rawClass) {

                // Check if it's a repository type
                if (isRepositoryType(rawClass)) {
                    Type entityType = parameterizedType.getActualTypeArguments()[0];

                    if (entityType instanceof Class<?> entityClass) {
                        return Optional.of(entityClass);
                    }
                }

                // Recursive resolution (interface extends interface)
                return resolveFromClass(rawClass);
            }
        }

        if (type instanceof Class<?> clazz) {
            return resolveFromClass(clazz);
        }

        return Optional.empty();
    }

    private boolean isRepositoryType(Class<?> clazz) {
        String name = clazz.getName();

        return name.equals("jakarta.data.repository.BasicRepository")
                || name.equals("jakarta.data.repository.CrudRepository")
                || name.equals("jakarta.data.repository.DataRepository")
                || name.equals("org.eclipse.jnosql.mapping.NoSQLRepository");
    }
}
