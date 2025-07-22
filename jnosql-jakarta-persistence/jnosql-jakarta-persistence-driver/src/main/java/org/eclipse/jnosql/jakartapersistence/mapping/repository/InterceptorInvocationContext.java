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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;


import jakarta.enterprise.inject.Instance;
import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jnosql.jakartapersistence.mapping.spi.MethodInterceptor;

/**
 *
 * @author Ondro Mihalyi
 */
abstract class InterceptorInvocationContext implements InvocationContext {

    private final Object instance;
    private final Method method;
    private Object[] params;
    Map<String, Object> contextData = new HashMap<>();

    public InterceptorInvocationContext(Object[] params, Object instance, Method method, Map<? extends String, ? extends Object> contextData) {
        this.params = params;
        this.instance = instance;
        this.method = method;
        if (contextData != null) {
            this.contextData.putAll(contextData);
        }
    }

    /**
     * Selector for interceptors. Should select a single bean. If multiple beans selected, an exception is thrown.
     */
    abstract protected Instance<MethodInterceptor> selectInterceptor();

    /**
      Action that is intercepted.
    */
    abstract protected Object invoke(Object instance, Method method, Object[] params) throws Throwable;

    @Override
    public Object getTarget() {
        return instance;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return params;
    }

    @Override
    public void setParameters(Object[] params) {
        this.params = params;
    }

    @Override
    public Map<String, Object> getContextData() {
        return contextData;
    }

    @Override
    public Object proceed() throws Exception {
        try {
            return invoke(instance, method, params);
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    public Object execute() throws Exception {
        final Instance<MethodInterceptor> selector = selectInterceptor();
        if (selector == null || selector.isUnsatisfied()) {
            return this.proceed();
        }
        return selector.get().intercept(this);
    }

}
