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
 * Exposes a Jakarta Data repository as a Jakarta REST resource.
 *
 * <p>The annotation processor generates, at compile time, a class named
 * {@code <RepositoryInterfaceName>Resource} in the package of the repository, for example
 * {@code ProductRepositoryResource}. The class is a CDI request-scoped Jakarta REST resource that
 * delegates every request to the CDI-managed repository.</p>
 *
 * <pre>{@code
 * @Repository
 * @RestRepository(path = "/products")
 * public interface ProductRepository extends BasicRepository<Product, UUID> {
 *
 *     @Find
 *     List<Product> findByCategory(String category);
 * }
 * }</pre>
 *
 * <p>The annotation alone is enough to obtain resource-oriented endpoints: inserts are served by
 * {@code POST /products}, a lookup by identifier by {@code GET /products/{id}}, and other finders by
 * {@code GET /products/{methodName}}. Standard Jakarta REST annotations customize the result:
 * {@code @Path} on a repository method selects its route and, under {@link RestExposure#EXPLICIT}, exposes it;
 * {@code @PathParam} and {@code @QueryParam} on a parameter select how it is bound. {@link RestExclude} removes
 * a method.</p>
 *
 * <p>Repositories without this annotation are never exposed. The annotation must be placed on an interface
 * that extends {@code jakarta.data.repository.DataRepository}. Contradictory configuration, unsupported
 * method signatures that were explicitly requested, invalid parameter bindings and route collisions are
 * reported as compilation errors.</p>
 *
 * <p>The default {@link RestExposure#ALL} policy can publish mutating operations. The generated resource does not
 * add authorization; use the security mechanisms of the hosting application.</p>
 *
 * @see RestExposure
 * @see RestOperation
 * @see RestExclude
 * @see RestDTO
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RestRepository {

    /**
     * The base path of the generated resource, used as the value of {@code @jakarta.ws.rs.Path}.
     *
     * @return the resource path, such as {@code "/products"}
     */
    String path();

    /**
     * The policy that selects which operations are exposed. The default is {@link RestExposure#ALL}.
     *
     * @return the exposure policy
     */
    RestExposure exposure() default RestExposure.ALL;

    /**
     * Operation categories that are never exposed, including inherited operations that are not redeclared.
     * Excluding a category that contains a method annotated with a Jakarta REST {@code @Path} is a compilation
     * error.
     *
     * @return the excluded operation categories
     */
    RestOperation[] exclude() default {};
}
