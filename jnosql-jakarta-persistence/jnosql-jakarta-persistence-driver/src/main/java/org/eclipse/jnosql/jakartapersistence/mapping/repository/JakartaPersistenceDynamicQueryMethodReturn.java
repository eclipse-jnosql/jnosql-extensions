/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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


import jakarta.data.page.PageRequest;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistencePreparedStatement;
import org.eclipse.jnosql.mapping.core.repository.MethodDynamicExecutable;

/**
 * Copied from DynamicQueryMethodReturn in JNoSQL and modified for Jakarta Persistence
 */
public final class JakartaPersistenceDynamicQueryMethodReturn<T> implements MethodDynamicExecutable {


    private final Method method;
    private final Object[] args;
    private final Class<?> typeClass;
    private final Function<String, PersistencePreparedStatement> prepareConverter;
    private final PageRequest pageRequest;

    private JakartaPersistenceDynamicQueryMethodReturn(Method method, Object[] args, Class<?> typeClass,
                                     Function<String, PersistencePreparedStatement> prepareConverter,
                                     PageRequest pageRequest) {
        this.method = method;
        this.args = args;
        this.typeClass = typeClass;
        this.prepareConverter = prepareConverter;
        this.pageRequest = pageRequest;
    }

    Method method() {
        return method;
    }

    Object[] args() {
        return args;
    }

    Class<?> typeClass() {
        return typeClass;
    }

    Function<String, PersistencePreparedStatement> prepareConverter() {
        return prepareConverter;
    }

    PageRequest pageRequest() {
        return pageRequest;
    }

    boolean hasPagination() {
        return pageRequest != null;
    }

    public static <T> DynamicQueryMethodReturnBuilder<T> builder() {
        return new DynamicQueryMethodReturnBuilder<>();
    }

    @Override
    public Object execute() {
        return JakartaPersistenceDynamicReturnConverter.INSTANCE.convert(this);
    }

    public static final class DynamicQueryMethodReturnBuilder<T> {

        private Method method;
        private Object[] args;
        private Class<?> typeClass;
        private Function<String, PersistencePreparedStatement> prepareConverter;
        private PageRequest pageRequest;

        private DynamicQueryMethodReturnBuilder() {
        }

        public DynamicQueryMethodReturnBuilder<T> method(Method method) {
            this.method = method;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> args(Object[] args) {
            if(args != null) {
                this.args = args.clone();
            }
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> typeClass(Class<?> typeClass) {
            this.typeClass = typeClass;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> prepareConverter(Function<String, PersistencePreparedStatement> prepareConverter) {
            this.prepareConverter = prepareConverter;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> pageRequest(PageRequest pageRequest) {
            this.pageRequest = pageRequest;
            return this;
        }

        public JakartaPersistenceDynamicQueryMethodReturn<T> build() {
            Objects.requireNonNull(method, "method is required");
            Objects.requireNonNull(typeClass, "typeClass is required");
            Objects.requireNonNull(prepareConverter, "prepareConverter is required");
            return new JakartaPersistenceDynamicQueryMethodReturn<>(method, args, typeClass, prepareConverter, pageRequest);
        }
    }


}