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
package org.eclipse.jnosql.jakartapersistence.mapping.spi;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import jakarta.interceptor.InvocationContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Extension point to intercept repository method calls. An interceptor should
 * be an alternative CDI bean that implements this interface and is marked by
 * one of the appropriate qualifiers.
 *
 * @author Ondro Mihalyi
 */
@FunctionalInterface
public interface MethodInterceptor {

    /**
     * Interceptors marked with this qualifier intercept repository method calls
     * as they are called from outside of the repository. Enabling an
     * alternative with this qualifier replaces the default interceptor that
     * ensures that a repository methods run within a transaction. The current
     * EntityManager can be retrieved from the context by calling
     * {@code (Collection<? extends Object>)context.getContextData(().get(EntityManager.class.getName())}.
     * The invocation context holds information about the repository method,
     * instance, and the parameters.
     */
    @Documented
    @Qualifier
    @Retention(RUNTIME)
    @Target({TYPE, METHOD, FIELD, PARAMETER})
    public @interface Repository {

        public static final RepositoryLiteral INSTANCE = new RepositoryLiteral();

        public static class RepositoryLiteral extends AnnotationLiteral<MethodInterceptor.Repository> implements MethodInterceptor.Repository {
        }
    }

    /**
     * Called to intercept any methods.
     *
     * @param context Invocation context
     * @return value returned by the context invocation
     * @throws Exception
     */
    Object intercept(InvocationContext context) throws Exception;

    /**
     * Allows chaining multiple interceptors. A single interceptor can chain other interceptors together from
     * the {@link MethodInterceptor#intercept(jakarta.interceptor.InvocationContext)} method
     * with
     * <pre>{@code Object intercept(InvocationContext context) throws Exception {
     *   return secondInterceptor.intercept(new ChainedInvocationContext(thirdInterceptor, context));
     * }}</pre>
     */
    public static class ChainedInvocationContext implements InvocationContext {

        InvocationContext originalContext;
        MethodInterceptor interceptor;

        public ChainedInvocationContext(MethodInterceptor interceptor, InvocationContext originalContext) {
            this.interceptor = interceptor;
            this.originalContext = originalContext;
        }

        @Override
        public Object getTarget() {
            return originalContext.getTarget();
        }

        @Override
        public Object getTimer() {
            return originalContext.getTimer();
        }

        @Override
        public Method getMethod() {
            return originalContext.getMethod();
        }

        @Override
        public Constructor<?> getConstructor() {
            return originalContext.getConstructor();
        }

        @Override
        public Object[] getParameters() {
            return originalContext.getParameters();
        }

        @Override
        public void setParameters(Object[] params) {
            originalContext.setParameters(params);
        }

        @Override
        public Map<String, Object> getContextData() {
            return originalContext.getContextData();
        }

        @Override
        public Object proceed() throws Exception {
            return interceptor.intercept(originalContext);
        }

        @Override
        public Set<Annotation> getInterceptorBindings() {
            return originalContext.getInterceptorBindings();
        }

        @Override
        public <T extends Annotation> T getInterceptorBinding(Class<T> annotationType) {
            return originalContext.getInterceptorBinding(annotationType);
        }

        @Override
        public <T extends Annotation> Set<T> getInterceptorBindings(Class<T> annotationType) {
            return originalContext.getInterceptorBindings(annotationType);
        }

    }
}
