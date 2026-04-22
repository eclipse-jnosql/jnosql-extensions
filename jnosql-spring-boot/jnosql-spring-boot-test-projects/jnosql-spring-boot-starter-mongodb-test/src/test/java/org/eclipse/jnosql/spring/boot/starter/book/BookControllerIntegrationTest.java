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
package org.eclipse.jnosql.spring.boot.starter.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;

import java.util.UUID;


@Testcontainers
@SpringBootTest
@AutoConfigureRestTestClient
@DisplayName("Book REST API")
class BookControllerIntegrationTest {

    @Container
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("jnosql.mongodb.url", MONGODB::getReplicaSetUrl);
    }

    @Autowired
    RestTestClient client;

    @Test
    @DisplayName("POST /books returns 201 with book response")
    void create_shouldReturn201WithBookResponse() {

        client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "title": "Clean Code",
                            "author": "Robert C. Martin",
                            "isbn": "978-0132350884",
                            "year": 2008
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.title").isEqualTo("Clean Code")
                .jsonPath("$.author").isEqualTo("Robert C. Martin")
                .jsonPath("$.isbn").isEqualTo("978-0132350884")
                .jsonPath("$.year").isEqualTo(2008);

    }

    @Test
    @DisplayName("POST /books returns 400 when title is missing")
    void create_shouldReturn400WhenTitleIsMissing() {

        client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"author": "Robert C. Martin"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();

    }

    @Test
    @DisplayName("GET /books/{id} returns 200 when book exists")
    void findById_shouldReturn200WhenBookExists() {

        var exchangeResult = client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title": "Clean Architecture", "author": "Robert C. Martin", "isbn": "978-0134494193", "year": 2017}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(JsonNode.class);

        String id = exchangeResult.getResponseBody().get("id").asString();

        client.get().uri("/books/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.title").isEqualTo("Clean Architecture");

    }

    @Test
    @DisplayName("GET /books/{id} returns 404 when book does not exist")
    void findById_shouldReturn404WhenBookNotExists() {
        client.get().uri("/books/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /books returns 200 with list of books")
    void findAll_shouldReturn200WithListOfBooks() {
        client.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Test
    @DisplayName("PUT /books/{id} returns 200 when book exists")
    void update_shouldReturn200WhenBookExists() {
        var exchangeResult = client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title": "Original Title", "author": "Original Author", "isbn": "ORIGINAL-ISBN", "year": 2000}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(JsonNode.class);

        String id = exchangeResult.getResponseBody().get("id").asString();

        client.put().uri("/books/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "title": "Updated Title",
                            "author": "Updated Author",
                            "isbn": "UPDATED-ISBN",
                            "year": 2024
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.title").isEqualTo("Updated Title")
                .jsonPath("$.author").isEqualTo("Updated Author")
                .jsonPath("$.isbn").isEqualTo("UPDATED-ISBN")
                .jsonPath("$.year").isEqualTo(2024);


        client.put().uri("/books/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "title": "Updated Title",
                            "author": "Updated Author",
                            "year": 2026
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.title").isEqualTo("Updated Title")
                .jsonPath("$.author").isEqualTo("Updated Author")
                .jsonPath("$.isbn").isEqualTo("UPDATED-ISBN")
                .jsonPath("$.year").isEqualTo(2026);


    }

    @Test
    @DisplayName("PUT /books/{id} returns 404 when book does not exist")
    void update_shouldReturn404WhenBookNotExists() {
        client.put().uri("/books/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title": "Updated Title", "author": "Updated Author"}
                        """)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /books/{id} returns 204 when book exists")
    void delete_shouldReturn204WhenBookExists() {

        var exchangeResult = client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title": "To Be Deleted", "author": "Author"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(JsonNode.class);

        String id = exchangeResult.getResponseBody().get("id").asString();

        client.delete().uri("/books/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        client.delete().uri("/books/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /books/{id} returns 404 when book does not exist")
    void delete_shouldReturn404WhenBookNotExists() {
        client.delete().uri("/books/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /books returns 400 when author is null")
    void create_shouldReturn400WhenAuthorIsNull() {
        client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title": "Valid Title", "author": null}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("POST /books returns 400 when title is empty")
    void create_shouldReturn400WhenTitleIsEmpty() {
        client.post().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title": "", "author": "Valid Author"}
                        """)
                .exchange()
                .expectStatus().is4xxClientError();
    }
}
