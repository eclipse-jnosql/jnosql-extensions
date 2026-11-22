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

/**
 * Defines which repository operations a {@link RestRepository} publishes by default.
 *
 * <p>The policy applies to inherited operations ({@code BasicRepository}, {@code CrudRepository}) and to
 * methods declared on the repository interface. {@link RestExclude} and
 * {@link RestRepository#exclude()} are evaluated after the policy.</p>
 *
 * <p>{@link #ALL} is the default and can expose mutating operations such as insert, update and delete.
 * Combine it with the authorization mechanisms of the hosting application, or choose {@link #EXPLICIT}.</p>
 *
 * @see RestRepository#exposure()
 * <table>
 * <caption>Effect of the policy on a method</caption>
 * <tr><th>Policy</th><th>Ordinary method</th><th>Method with {@code @Path}</th><th>Method with
 * {@link RestExclude}</th></tr>
 * <tr><td>{@link #ALL}</td><td>exposed</td><td>exposed at the given path</td><td>excluded</td></tr>
 * <tr><td>{@link #EXPLICIT}</td><td>excluded</td><td>exposed at the given path</td><td>excluded</td></tr>
 * <tr><td>{@link #NONE}</td><td>excluded</td><td>excluded</td><td>excluded</td></tr>
 * </table>
 *
 * <p>{@code @Path} is the Jakarta REST annotation {@code jakarta.ws.rs.Path}.</p>
 *
 * @see RestRepository#exposure()
 */
public enum RestExposure {

    /**
     * Exposes every eligible operation. Destructive query-based operations, such as {@code deleteByCategory},
     * and dynamic restriction searches still require a method-level {@code @Path}.
     */
    ALL,

    /**
     * Exposes only the methods annotated with a Jakarta REST {@code @Path}; an empty path selects the collection
     * root. An inherited operation is selected by redeclaring it in the repository interface and annotating the
     * declaration.
     */
    EXPLICIT,

    /**
     * Generates no endpoints, not even for methods annotated with {@code @Path}. Useful to disable a repository
     * temporarily without removing the annotation.
     */
    NONE
}
