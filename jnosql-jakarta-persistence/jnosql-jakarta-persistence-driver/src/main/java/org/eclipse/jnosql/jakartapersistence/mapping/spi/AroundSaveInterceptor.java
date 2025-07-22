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
package org.eclipse.jnosql.jakartapersistence.mapping.spi;

import jakarta.interceptor.InvocationContext;

/**
 * Extension point to intercept any a save action, which persists entities inside repository methods
 * that persist entities. An interceptor should be an alternative CDI bean
 * that implements this interface. It would replace any default interceptor.
 *
 * This interceptor is handy to validate entities in Jakarta EE container context or
 * if integration with Jakarta Validation is desired.
 *
 * @author Ondro Mihalyi
 */
@FunctionalInterface
public interface AroundSaveInterceptor {

    /**
     * Called to intercept saving action. A collection of entities can be retrieved from the context
     * by calling {@code (Collection<? extends Object>)context.getContextData(().get(jakarta.persistence.Entity.class.getName())}.
     * @param context Invocation context
     * @return value returned by the context invocation
     * @throws Exception
     */
    Object intercept(InvocationContext context) throws Exception;
}
