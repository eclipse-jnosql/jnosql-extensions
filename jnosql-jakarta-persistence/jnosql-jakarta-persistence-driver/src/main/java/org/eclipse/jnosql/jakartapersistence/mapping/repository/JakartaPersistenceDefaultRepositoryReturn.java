/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;

/**
 * Copied from DefaultRepositoryReturn in JNoSQL and modified for Jakarta Persistence
 */
class JakartaPersistenceDefaultRepositoryReturn implements RepositoryReturn {

    @Override
    public boolean isCompatible(Class<?> entity, Class<?> returnType) {
        return true;
    }

    @Override
    public <T> Object convert(DynamicReturn<T> dynamicReturn) {
        final Class<?> returnType = dynamicReturn.getMethod().getReturnType();
        if (isNumberType(returnType)) {
            return dynamicReturn.singleResult().get();
        }
        return dynamicReturn.result();
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn) {
        return dynamicReturn.streamPagination();
    }

    public static boolean isNumberType(Class<?> clazz) {
        // Check if it's a primitive numeric type
        if (clazz.isPrimitive()) {
            return clazz == byte.class
                    || clazz == short.class
                    || clazz == int.class
                    || clazz == long.class
                    || clazz == float.class
                    || clazz == double.class;
        }

        // Check if it's a Number subclass
        return Number.class.isAssignableFrom(clazz);
    }
}
