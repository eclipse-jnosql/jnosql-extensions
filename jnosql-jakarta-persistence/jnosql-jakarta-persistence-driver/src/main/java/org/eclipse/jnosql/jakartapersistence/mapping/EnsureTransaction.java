/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Interceptor binding that makes a method run inside a Jakarta Persistence transaction.
 *
 * <p>The binding is handled by {@link EnsureTransactionInterceptor}. If the entity manager is already
 * joined to a transaction, the invocation participates in it. Otherwise a resource-local transaction is
 * started, committed when the method returns normally and rolled back when it throws.</p>
 *
 * <p>It can be placed on a type, in which case it applies to all its business methods, or on a single
 * method, and it is {@link java.lang.annotation.Inherited inherited} by subclasses. It requires the
 * interceptor to be enabled in the CDI container.</p>
 *
 * @author Ondro Mihalyi
 * @see EnsureTransactionInterceptor
 */
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface EnsureTransaction {
}
