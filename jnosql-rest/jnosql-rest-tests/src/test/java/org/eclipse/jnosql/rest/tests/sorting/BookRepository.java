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
package org.eclipse.jnosql.rest.tests.sorting;

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.rest.RestDTO;
import org.eclipse.jnosql.rest.RestRepository;

import java.util.List;

@Repository
@RestRepository(path = "/books")
@RestDTO(exclude = {"internalCode"})
public interface BookRepository extends BasicRepository<Book, Long> {

    @Find
    List<Book> byAuthorLimited(String author, Limit limit);

    @Find
    List<Book> byAuthorSorted(String author, Sort<Book> sort);

    @Find
    List<Book> byAuthorOrdered(String author, Order<Book> order);

    @Find
    CursoredPage<Book> byAuthorCursor(String author, PageRequest pageRequest, Order<Book> order);
}
