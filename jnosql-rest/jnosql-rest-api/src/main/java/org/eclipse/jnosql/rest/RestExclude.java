/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Prevents a repository method from being exposed as an HTTP endpoint.
 *
 * <p>The exclusion wins over {@link RestExposure#ALL}. To exclude an inherited operation, redeclare it in the
 * repository interface and annotate the declaration, or exclude the whole category with
 * {@link RestRepository#exclude()}. Combining it with {@link RestExpose} on the same method is a compilation
 * error.</p>
 *
 * <pre>{@code
 * @RestExclude
 * @Delete
 * void delete(Product product);
 * }</pre>
 *
 * @see RestRepository#exclude()
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface RestExclude {
}
