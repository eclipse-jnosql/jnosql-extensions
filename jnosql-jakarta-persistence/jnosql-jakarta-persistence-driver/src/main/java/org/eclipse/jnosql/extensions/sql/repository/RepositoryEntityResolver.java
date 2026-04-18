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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
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
     * @param repositoryType the repository interface
     * @return the resolved entity class
     * @throws NullPointerException if {@code repositoryType} is null
     * @throws IllegalArgumentException if the entity type cannot be resolved
     */
    public Class<?> resolveEntityType(Class<?> repositoryType) {
        Objects.requireNonNull(repositoryType, "repositoryType is required");

        Optional<Class<?>> entityType = resolveFromClass(repositoryType);
        if(entityType.isPresent()) {
            return entityType.get();
        }
        return resolveFromCustomRepository(repositoryType);
    }

    private Class<?> resolveFromCustomRepository(Class<?> repositoryType) {
        for (Method method : repositoryType.getDeclaredMethods()) {

            Class<?> entity = extractEntityFromReturnType(method.getGenericReturnType());
            if (entity != null) {
                return entity;
            }
            if(method.getParameters().length > 0) {
                for (Type parameterType : method.getGenericParameterTypes()) {
                    entity = extractEntityFromReturnType(parameterType);
                    if (entity != null) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }

    private Class<?> extractEntityFromReturnType(Type type) {

        if (type instanceof Class<?> typeEntity) {
            if (typeEntity.isArray()) {
                return extractEntityFromReturnType(typeEntity.getComponentType());
            }
            return isEntity(typeEntity) ? typeEntity : null;
        }

        if (type instanceof GenericArrayType genericArrayType) {
            return extractEntityFromReturnType(genericArrayType.getGenericComponentType());
        }

        if (type instanceof ParameterizedType parameterizedType) {

            Type rawType = parameterizedType.getRawType();

            if (rawType instanceof Class<?>) {
                Class<?> rawClass = (Class<?>) rawType;

                if (Optional.class.isAssignableFrom(rawClass)) {
                    return extractEntityFromReturnType(
                            parameterizedType.getActualTypeArguments()[0]
                    );
                }

                if (Iterable.class.isAssignableFrom(rawClass) ||
                        Collection.class.isAssignableFrom(rawClass)) {
                    return extractEntityFromReturnType(
                            parameterizedType.getActualTypeArguments()[0]
                    );
                }
            }
        }

        return null;
    }

    private Optional<Class<?>> resolveFromClass(Class<?> clazz) {

        for (Type type : clazz.getGenericInterfaces()) {
            Optional<Class<?>> resolved = resolveFromType(type);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
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

    private boolean isEntity(Type type) {
        if (type instanceof Class<?> entityClass) {
            return entityClass.isAnnotationPresent(jakarta.persistence.Entity.class);
        }

        if (type instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            return isEntity(rawType);
        }

        if (type instanceof GenericArrayType genericArrayType) {
            return isEntity(genericArrayType.getGenericComponentType());
        }

        return false;
    }
}
