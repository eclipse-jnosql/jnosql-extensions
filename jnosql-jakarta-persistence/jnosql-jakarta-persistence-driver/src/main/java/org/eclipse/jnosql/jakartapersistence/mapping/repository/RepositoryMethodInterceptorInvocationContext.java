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

import org.eclipse.jnosql.jakartapersistence.mapping.spi.RepositoryMethodInterceptor;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ondro Mihalyi
 */
class RepositoryMethodInterceptorInvocationContext implements InvocationContext {

    private final Object instance;
    private final Method method;
    private final Action action;
    private Object[] params;
    Map<String, Object> contextData = new HashMap<>();

    @FunctionalInterface
    interface Action {
        Object invoke(Object instance, Method method, Object[] params) throws Throwable;
    }

    public RepositoryMethodInterceptorInvocationContext(Object[] params, Object instance, Method method, final EntityManager entityManager, Action action) {
        this.params = params;
        this.instance = instance;
        this.method = method;
        this.contextData.put(EntityManager.class.getName(), entityManager);
        this.action = action;
    }

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
            return action.invoke(instance, method, params);
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    public Object execute() throws Exception {
        final Instance<RepositoryMethodInterceptor> selector = CDI.current().select(RepositoryMethodInterceptor.class);
        if (selector.isUnsatisfied()) {
            return this.proceed();
        }
        return selector.get().intercept(this);
    }

}
