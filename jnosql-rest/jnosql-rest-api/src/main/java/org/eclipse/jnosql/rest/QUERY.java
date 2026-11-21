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

import jakarta.ws.rs.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP method designator for the {@code QUERY} method: a safe, idempotent request that carries its search
 * criteria in the request body.
 *
 * <p>Jakarta REST does not define a {@code QUERY} annotation, so the generated resources use this standard
 * {@link HttpMethod} extension for the dynamic search endpoints produced for repository methods that accept a
 * {@code jakarta.data.restrict.Restriction}. It is only ever generated for read-only operations.</p>
 *
 * <p>Compatibility: the runtime, the HTTP server, reverse proxies and the HTTP client must all accept the
 * {@code QUERY} method. There is no silent fallback to {@code POST}.</p>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc10008.html">HTTP QUERY</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@HttpMethod("QUERY")
public @interface QUERY {
}
