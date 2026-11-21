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
package org.eclipse.jnosql.spring.boot.starter.book.controller;

import org.eclipse.jnosql.spring.boot.starter.book.domain.Book;
import org.eclipse.jnosql.spring.boot.starter.book.dto.BookRequest;
import org.eclipse.jnosql.spring.boot.starter.book.dto.BookResponse;
import org.eclipse.jnosql.spring.boot.starter.book.mapper.BookMapper;
import org.eclipse.jnosql.spring.boot.starter.book.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService service;
    private final BookMapper mapper;

    public BookController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book book = mapper.toNewEntity(request);
        Book saved = service.save(book);
        return ResponseEntity.status(201).body(mapper.toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> findAll() {
        List<BookResponse> books = service.findAll().stream()
            .map(mapper::toResponse)
            .toList();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable("id") String id) {
        return service.findById(id)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable("id") String id,
            @Valid @RequestBody BookRequest request) {
        return service.findById(id)
            .map(existing -> {
                Book updated = mapper.updateEntity(existing, request);
                BookResponse response = mapper.toResponse(service.save(updated));
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}