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

import jakarta.persistence.EntityManager;


/**
 *
 * @author Ondro Mihalyi
 */
public class StatementInterceptionEvent {

    @FunctionalInterface
    public interface CallableWithThrowable<T> {
        T call() throws Throwable;
    }

    private EntityManager entityManager;
    private CallableWithThrowable<Object> action;

    public StatementInterceptionEvent(EntityManager entityManager, CallableWithThrowable<Object> action) {
        this.entityManager = entityManager;
        this.action = action;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public CallableWithThrowable<Object> getAction() {
        return action;
    }

    public void setAction(CallableWithThrowable<Object> action) {
        this.action = action;
    }

}
