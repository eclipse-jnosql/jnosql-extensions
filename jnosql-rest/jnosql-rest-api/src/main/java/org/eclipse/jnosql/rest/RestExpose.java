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
 * Explicitly exposes a repository method through the generated REST resource.
 *
 * <p>It is required for every method under {@link RestExposure#EXPLICIT}, and for destructive query-based
 * operations such as {@code deleteByCategory} or {@code @Delete} methods without an entity parameter, even under
 * {@link RestExposure#ALL}. To select an inherited operation, redeclare it in the repository interface and
 * annotate the declaration.</p>
 *
 * <p>Annotating a method that cannot be mapped to an endpoint, or whose operation category is excluded by
 * {@link RestRepository#exclude()}, is a compilation error. Using both this annotation and {@link RestExclude}
 * on the same method is also an error.</p>
 *
 * @see RestRepository#exposure()
 * @see RestExclude
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface RestExpose {

    /**
     * Overrides the sub-path of the endpoint, relative to {@link RestRepository#path()}.
     * When empty, the path is derived from the operation, normally {@code "/" + methodName} for declared methods.
     * Two endpoints with the same HTTP method and path are reported as a compilation error.
     *
     * @return the sub-path, such as {@code "/by-category"}, or an empty string to keep the derived path
     */
    String path() default "";
}
