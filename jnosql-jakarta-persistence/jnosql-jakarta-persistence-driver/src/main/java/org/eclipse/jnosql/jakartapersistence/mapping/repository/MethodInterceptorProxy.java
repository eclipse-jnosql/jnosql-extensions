/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import org.eclipse.jnosql.jakartapersistence.CdiUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MethodInterceptorProxy<T> implements InvocationHandler {
    
    private final T delegate;
    private final Map<Method, Object> interceptedMethods = new HashMap<>();
    private final BeanManager beanManager;
    private final CreationalContext<T> context;
    private final Class<T> type;
    
    public MethodInterceptorProxy(T delegate, BeanManager beanManager, CreationalContext<T> context, Class<T> type) {
        this.delegate = delegate;
        this.beanManager = beanManager;
        this.context = context;
        this.type = type;
        initializeInterceptedMethods();
    }
    
    private void initializeInterceptedMethods() {
        for (Method method : type.getMethods()) {
            Set<Annotation> methodBindings = CdiUtil.getAllInterceptorBindingsRecursively(beanManager, method.getDeclaredAnnotations());
            if (!methodBindings.isEmpty()) {
                var factory = beanManager.createInterceptionFactory(context, type);
                var configurator = factory.configure();
                methodBindings.forEach(configurator::add);
                interceptedMethods.put(method, factory.createInterceptedInstance(delegate));
            }
        }
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object target = interceptedMethods.get(method);
        if (target != null) {
            return method.invoke(target, args);
        }
        return method.invoke(delegate, args);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T create(T delegate, BeanManager beanManager, CreationalContext<T> context, Class<T> type) {
        MethodInterceptorProxy<T> handler = new MethodInterceptorProxy<>(delegate, beanManager, context, type);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }
}
