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
package org.eclipse.jnosql.rest.tests.paging;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Repository;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.rest.RestExpose;
import org.eclipse.jnosql.rest.RestRepository;
import org.eclipse.jnosql.rest.tests.sample.Product;

import java.util.UUID;

@Repository
@RestRepository(path = "/paged-products")
public interface PagedProductRepository extends BasicRepository<Product, UUID> {

    @Find
    Page<Product> findByCategory(String category, PageRequest pageRequest);

    @Find
    @OrderBy("name")
    @OrderBy("id")
    CursoredPage<Product> findByName(String name, PageRequest pageRequest);

    @Find
    @RestExpose(path = "/search")
    @OrderBy("name")
    @OrderBy("id")
    CursoredPage<Product> search(Restriction<Product> restriction, PageRequest pageRequest);
}
