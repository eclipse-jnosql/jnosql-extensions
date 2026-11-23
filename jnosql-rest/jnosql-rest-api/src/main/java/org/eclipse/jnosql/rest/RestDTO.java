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
 * with {@code MappingConstants.ComponentModel.JAKARTA_CDI} and injected into the resource. MapStruct and its annotation
 * processor must be configured by the application; otherwise compilation fails with an explanatory message.</p>
 *
 * <p>Attributes are removed from the request DTOs with {@link #exclude()} and {@link #excludeFromRequest()}, and
 * from the response DTO with {@link #exclude()} and {@link #excludeFromResponse()}.</p>
 *
 * <p>The DTO is not an authorization mechanism. Prefer developer-defined records for sensitive or independently
 * versioned contracts.</p>
 *
 * <p>The examples below use this entity, whose attributes are all public and readable:</p>
 *
 * <pre>{@code
 * @Entity
 * public class Product {
 *     @Id private String id;
 *     @Column private String name;
 *     @Column private BigDecimal price;
 *     @Column private BigDecimal supplierCost;
 *     @Column private String internalNotes;
 *     // public getters and setters
 * }
 * }</pre>
 *
 * <p>With all three attributes combined:</p>
 *
 * <pre>{@code
 * @Repository
 * @RestRepository(path = "/products")
 * @RestDTO(
 *     exclude = {"internalNotes"},
 *     excludeFromRequest = {"id"},
 *     excludeFromResponse = {"supplierCost"})
 * public interface ProductRepository extends CrudRepository<Product, String> {
 * }
 * }</pre>
 *
 * <p>the processor generates:</p>
 *
 * <pre>{@code
 * public record ProductDTO(String id, String name, BigDecimal price) { }
 * public record ProductCreateRequest(String name, BigDecimal price, BigDecimal supplierCost) { }
 * public record ProductUpdateRequest(String name, BigDecimal price, BigDecimal supplierCost) { }
 * }</pre>
 *
 * <table>
 *   <caption>Effective attributes of the combined example</caption>
 *   <tr><th>Attribute</th><th>Create/update request</th><th>Response</th></tr>
 *   <tr><td>{@code id}</td><td>excluded</td><td>included</td></tr>
 *   <tr><td>{@code name}, {@code price}</td><td>included</td><td>included</td></tr>
 *   <tr><td>{@code supplierCost}</td><td>included</td><td>excluded</td></tr>
 *   <tr><td>{@code internalNotes}</td><td>excluded</td><td>excluded</td></tr>
 * </table>
 *
 * <p>An update request never contains the identifier, which is bound from the path.</p>
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
     * <p>The attributes are removed from {@code ProductDTO}, {@code ProductCreateRequest} and
     * {@code ProductUpdateRequest}, and therefore from their MapStruct mappings and from the OpenAPI schemas derived
     * from them. All other eligible attributes are mapped by default. These settings control the generated REST DTOs,
     * not database attributes or persistence behavior.</p>
     *
     * <p>Names are Java attribute names, not database column names, and only direct attributes are supported.
     * Unknown names are compilation errors and repeated names are ignored. If a developer-defined response DTO that
     * contains an excluded attribute exists, compilation fails.</p>
     *
     * <pre>{@code
     * @RestDTO(exclude = {"internalNotes"})
     *
     * public record ProductDTO(String id, String name, BigDecimal price, BigDecimal supplierCost) { }
     * public record ProductCreateRequest(String id, String name, BigDecimal price, BigDecimal supplierCost) { }
     * public record ProductUpdateRequest(String name, BigDecimal price, BigDecimal supplierCost) { }
     * }</pre>
     *
     * @return the attribute names to exclude from request and response DTOs
     */
    String[] exclude() default {};

    /**
     * Excludes the specified entity attributes from generated request DTOs while preserving them in response DTOs
     * unless otherwise excluded.
     *
     * <p>Typical for server-managed attributes such as an identifier. The attribute is absent from the create and
     * update request DTOs, but the entity identifier remains available to repository operations and to path
     * parameters. This does not change the database model.</p>
     *
     * <pre>{@code
     * @RestDTO(excludeFromRequest = {"id"})
     *
     * public record ProductDTO(String id, String name, BigDecimal price, BigDecimal supplierCost,
     *                          String internalNotes) { }
     * public record ProductCreateRequest(String name, BigDecimal price, BigDecimal supplierCost,
     *                                    String internalNotes) { }
     * public record ProductUpdateRequest(String name, BigDecimal price, BigDecimal supplierCost,
     *                                    String internalNotes) { }
     * }</pre>
     *
     * @return the attribute names to exclude from request DTOs only
     */
    String[] excludeFromRequest() default {};

    /**
     * Excludes the specified entity attributes from generated response DTOs while preserving them in request DTOs
     * unless otherwise excluded.
     *
     * <p>Clients can still send these attributes, which MapStruct maps into the entity and the repository persists,
     * but responses, sorting and searching never expose them. This does not change the database model.</p>
     *
     * <pre>{@code
     * @RestDTO(excludeFromResponse = {"supplierCost"})
     *
     * public record ProductDTO(String id, String name, BigDecimal price, String internalNotes) { }
     * public record ProductCreateRequest(String id, String name, BigDecimal price, BigDecimal supplierCost,
     *                                    String internalNotes) { }
     * public record ProductUpdateRequest(String name, BigDecimal price, BigDecimal supplierCost,
     *                                    String internalNotes) { }
     * }</pre>
     *
     * @return the attribute names to exclude from response DTOs only
     */
    String[] excludeFromResponse() default {};
}
