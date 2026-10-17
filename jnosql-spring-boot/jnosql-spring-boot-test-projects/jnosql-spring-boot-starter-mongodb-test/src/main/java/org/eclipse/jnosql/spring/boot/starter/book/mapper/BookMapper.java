/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.spring.boot.starter.book.mapper;

import org.eclipse.jnosql.spring.boot.starter.book.domain.Book;
import org.eclipse.jnosql.spring.boot.starter.book.dto.BookRequest;
import org.eclipse.jnosql.spring.boot.starter.book.dto.BookResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookMapper {

    public Book toNewEntity(BookRequest request) {
        return new Book(UUID.randomUUID().toString(),
            request.title(), request.author(),
            request.isbn(), request.year());
    }

    public Book updateEntity(Book existing, BookRequest request) {
        return new Book(
            existing.id(),
            request.title() != null ? request.title() : existing.title(),
            request.author() != null ? request.author() : existing.author(),
            request.isbn() != null ? request.isbn() : existing.isbn(),
            request.year() != null ? request.year() : existing.year()
        );
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
            book.id(), book.title(), book.author(),
            book.isbn(), book.year()
        );
    }
}