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
 * Enables DTO generation for a Jakarta Data repository exposed through JNoSQL REST.
 *
 * <p>JNoSQL REST generates request and response DTOs as Java records: {@code ProductDTO} for responses,
 * {@code ProductCreateRequest} for creation and {@code ProductUpdateRequest} for updates. DTOs written by the
 * developer with those names, in the package of the repository, are reused and never overwritten. Entities and DTOs
 * are converted by a generated MapStruct mapper, so MapStruct and its annotation processor must be configured by the
 * application. All eligible attributes are mapped by default.</p>
 *
 * <p>Attributes can be left out of the generated DTOs with {@link #exclude()}, {@link #excludeFromRequest()} and
 * {@link #excludeFromResponse()}. Given a {@code Product} entity with the attributes {@code id}, {@code name},
 * {@code price}, {@code supplierCost} and {@code internalNotes}:</p>
 *
 * <pre>{@code
 * @Repository
 * @RestRepository(path = "/products")
 * @RestDTO(
 *     exclude = {"internalNotes"},
 *     excludeFromRequest = {"id"},
 *     excludeFromResponse = {"supplierCost"}
 * )
 * public interface ProductRepository
 *         extends CrudRepository<Product, String> {
 * }
 * }</pre>
 *
 * <p>The creation endpoint accepts:</p>
 *
 * <pre>{@code
 * {
 *   "name": "Pen",
 *   "price": 10,
 *   "supplierCost": 9
 * }
 * }</pre>
 *
 * <p>and returns a representation such as:</p>
 *
 * <pre>{@code
 * {
 *   "id": "product-123",
 *   "name": "Pen",
 *   "price": 10
 * }
 * }</pre>
 *
 * <p>{@code internalNotes} appears in neither representation, {@code id} appears only in the response, and
 * {@code supplierCost} is accepted in the request but omitted from the response. The entity and its persistence
 * mapping are unchanged.</p>
 *
 * <p>Names are Java attribute names, not database column names, and only direct attributes are supported. Unknown
 * names are compilation errors. The DTO configuration is not an authorization mechanism.</p>
 *
 * @see RestRepository
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RestDTO {

    /**
     * Excludes the specified entity attributes from both generated request and response DTOs.
     *
     * @return the attribute names to exclude from request and response DTOs
     */
    String[] exclude() default {};

    /**
     * Excludes the specified entity attributes from generated request DTOs while keeping them in response DTOs
     * unless otherwise excluded.
     *
     * @return the attribute names to exclude from request DTOs only
     */
    String[] excludeFromRequest() default {};

    /**
     * Excludes the specified entity attributes from generated response DTOs while keeping them in request DTOs
     * unless otherwise excluded.
     *
     * @return the attribute names to exclude from response DTOs only
     */
    String[] excludeFromResponse() default {};
}
