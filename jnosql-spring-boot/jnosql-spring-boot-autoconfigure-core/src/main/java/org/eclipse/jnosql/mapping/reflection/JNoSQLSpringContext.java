/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.reflection;

import org.springframework.context.ApplicationContext;

import java.util.concurrent.locks.ReentrantLock;

public class JNoSQLSpringContext {

    private static final ReentrantLock lock = new ReentrantLock();
    private static ApplicationContext context;

    private JNoSQLSpringContext(){}

    public static void setContext(ApplicationContext context) {
        lock();
        try {
            JNoSQLSpringContext.context = context;
        } finally {
            lock.unlock();
        }
    }

    private static void lock() {
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread was interrupted while trying to acquire the lock to set the application context", e);
        }
    }

    public static ApplicationContext getContext() {
        lock();
        try {
            return JNoSQLSpringContext.context;
        } finally {
            lock.unlock();
        }
    }

}
