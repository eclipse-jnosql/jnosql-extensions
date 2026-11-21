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
 * Enables DTO-based representations for a {@link RestRepository}.
 *
 * <p>No class is passed to the annotation. For an entity {@code Product} the processor looks, in the package
 * of the repository, for the following types and reuses them when they exist. It never overwrites a type written
 * by the developer, and it generates the missing ones as Java records:</p>
 *
 * <table>
 *   <caption>DTO naming conventions</caption>
 *   <tr><th>Type</th><th>Role</th></tr>
 *   <tr><td>{@code ProductDTO}</td><td>response representation</td></tr>
 *   <tr><td>{@code ProductCreateRequest}</td><td>body of insert and save operations</td></tr>
 *   <tr><td>{@code ProductUpdateRequest}</td><td>body of update operations</td></tr>
 * </table>
 *
 * <p>Objects are converted by a generated MapStruct mapper interface named {@code ProductDTOMapper}, declared
 * with {@code componentModel = "jakarta-cdi"} and injected into the resource. MapStruct and its annotation
 * processor must be configured by the application; otherwise compilation fails with an explanatory message.</p>
 *
 * <p>The DTO is not an authorization mechanism. Prefer developer-defined records for sensitive or independently
 * versioned contracts.</p>
 *
 * <pre>{@code
 * @Repository
 * @RestRepository(path = "/customers")
 * @RestDTO(exclude = {"passwordHash", "internalNotes"})
 * public interface CustomerRepository extends BasicRepository<Customer, UUID> {
 * }
 * }</pre>
 *
 * @see RestRepository
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RestDTO {

    /**
     * Entity attribute names removed from the generated response DTO, and therefore from its mapping and from
     * the OpenAPI response schema derived from it.
     *
     * <p>Names are Java attribute names, not database column names, and only direct attributes are supported.
     * Unknown or duplicated names are compilation errors. The exclusion applies only to a generated response DTO:
     * if a developer-defined DTO that contains an excluded attribute exists, compilation fails. Create and update
     * requests are not affected, so an excluded attribute can still be written; update requests never contain the
     * identifier.</p>
     *
     * @return the attribute names to exclude from the response DTO
     */
    String[] exclude() default {};
}
