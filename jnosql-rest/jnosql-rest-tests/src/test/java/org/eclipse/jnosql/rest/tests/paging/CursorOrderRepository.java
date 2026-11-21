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
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.rest.RestDTO;
import org.eclipse.jnosql.rest.RestRepository;
import org.eclipse.jnosql.rest.tests.custom.Order;

import java.util.UUID;

@Repository
@RestRepository(path = "/paged-orders")
@RestDTO(exclude = {"customerId"})
public interface CursorOrderRepository {

    @Find
    @OrderBy("status")
    @OrderBy("id")
    CursoredPage<Order> findByCustomerId(UUID customerId, PageRequest pageRequest);

    @Find
    Page<Order> findByStatus(String status, PageRequest pageRequest);
}
