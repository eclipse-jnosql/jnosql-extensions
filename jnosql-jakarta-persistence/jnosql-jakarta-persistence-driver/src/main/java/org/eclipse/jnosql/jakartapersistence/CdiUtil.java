/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.jakartapersistence;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;

import org.eclipse.jnosql.jakartapersistence.mapping.repository.MethodInterceptorProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Ondro Mihalyi
 */
public class CdiUtil {

    private CdiUtil() {
    }

    public static <T> Event<T> getEvent(Class<T> eventClass) {
        return CDI.current().select(new TypeLiteral<Event<T>>() {}).get();
    }

    /**
     * Find all qualifiers in the list of annotations, including qualifiers nested in stereotypes
     * @param annotations
     * @return
     */
    public static Set<Annotation> getAllQualifiersRecursively(BeanManager beanManager, Annotation... annotations) {
        Set<Annotation> qualifiers = new HashSet<>();
        Set<Class<? extends Annotation>> visitedStereotypes = new HashSet<>();

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();

            if (beanManager.isQualifier(type)) {
                qualifiers.add(annotation);
            } else if (beanManager.isStereotype(type)) {
                resolveStereotypeQualifiers(type, beanManager, qualifiers, visitedStereotypes);
            }
        }

        return qualifiers;
    }

    private static void resolveStereotypeQualifiers(Class<? extends Annotation> stereotype, BeanManager beanManager,
            Set<Annotation> qualifiers, Set<Class<? extends Annotation>> visitedStereotypes) {
        if (!visitedStereotypes.add(stereotype)) {
            return; // avoid infinite loop
        }

        for (Annotation inner : stereotype.getAnnotations()) {
            Class<? extends Annotation> innerType = inner.annotationType();

            if (beanManager.isQualifier(innerType)) {
                qualifiers.add(inner);
            } else if (beanManager.isStereotype(innerType)) {
                resolveStereotypeQualifiers(innerType, beanManager, qualifiers, visitedStereotypes);
            }
        }
    }

    /**
     * Find all interceptor bindings in the list of annotations, including interceptor bindings nested in stereotypes
     * @param annotations
     * @return
     */
    public static Set<Annotation> getAllInterceptorBindingsRecursively(BeanManager beanManager, Annotation... annotations) {
        Set<Annotation> interceptorBindings = new HashSet<>();
        Set<Class<? extends Annotation>> visitedStereotypes = new HashSet<>();

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();

            if (beanManager.isInterceptorBinding(type)) {
                interceptorBindings.add(annotation);
            } else if (beanManager.isStereotype(type)) {
                resolveStereotypeInterceptorBindings(type, beanManager, interceptorBindings, visitedStereotypes);
            }
        }

        return interceptorBindings;
    }

    private static void resolveStereotypeInterceptorBindings(Class<? extends Annotation> stereotype, BeanManager beanManager,
            Set<Annotation> interceptorBindings, Set<Class<? extends Annotation>> visitedStereotypes) {
        if (!visitedStereotypes.add(stereotype)) {
            return; // avoid infinite loop
        }

        for (Annotation inner : stereotype.getAnnotations()) {
            Class<? extends Annotation> innerType = inner.annotationType();

            if (beanManager.isInterceptorBinding(innerType)) {
                interceptorBindings.add(inner);
            } else if (beanManager.isStereotype(innerType)) {
                resolveStereotypeInterceptorBindings(innerType, beanManager, interceptorBindings, visitedStereotypes);
            }
        }
    }

    /**
     * Copy interceptors applied on the reference type to a non-intercepted instance. Copies class-level and method-level interceptor bindings.
     * @param interceptedInstance the instance to apply interceptors to
     * @param referenceType the reference type, from which to copy interceptor bindings
     * @param context the creational context for the bean
     * @param beanManager the CDI bean manager
     * @return an intercepted instance with interceptors applied
     */
    @SuppressWarnings("unchecked")
    public static <T> T copyInterceptors(T interceptedInstance, Class<T> referenceType, CreationalContext<T> context, BeanManager beanManager) {
        T result = interceptedInstance;
        Set<Annotation> classLevelBindings = CdiUtil.getAllInterceptorBindingsRecursively(beanManager, referenceType.getDeclaredAnnotations());

        // Apply class-level interceptor bindings using InterceptionFactory
        if (!classLevelBindings.isEmpty()) {
            var factory = beanManager.createInterceptionFactory(context, referenceType);
            var configurator = factory.configure();
            classLevelBindings.forEach(configurator::add);
            result = (T) factory.createInterceptedInstance(result);
        }

        // Apply method-level interceptor bindings using custom proxy
        if (hasMethodLevelInterceptorBindings(beanManager, referenceType)) {
            result = MethodInterceptorProxy.create(result, beanManager, context, referenceType);
        }

        return result;
    }

    private static boolean hasMethodLevelInterceptorBindings(BeanManager beanManager, Class<?> type) {
        for (Method method : type.getMethods()) {
            Set<Annotation> methodBindings = CdiUtil.getAllInterceptorBindingsRecursively(beanManager, method.getDeclaredAnnotations());
            if (!methodBindings.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
