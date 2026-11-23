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
package org.eclipse.jnosql.rest.tests.sample;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.jnosql.rest.RestExclude;
import jakarta.ws.rs.Path;
import org.eclipse.jnosql.rest.RestRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RestRepository(path = "/products")
@Tag(name = "Products", description = "Product catalog operations")
public interface ProductRepository extends CrudRepository<Product, UUID> {

    @Find
    @Operation(summary = "Find products by category", description = "Returns products belonging to a category")
    @APIResponse(responseCode = "200", description = "Products retrieved successfully")
    List<Product> findByCategory(
            @Parameter(description = "Category name", in = ParameterIn.QUERY, required = true) String category);

    Optional<Product> findByName(String name);

    boolean existsByName(String name);

    long countByCategory(String category);

    @Query("WHERE price > :minimum")
    List<Product> expensive(BigDecimal minimum);

    @Path("/purge/{category}")
    void deleteByCategory(String category);

    @Query("DELETE FROM Product WHERE category = :category")
    void purgeQuery(String category);

    @RestExclude
    List<Product> findByNameLike(String name);
}
