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
package org.eclipse.jnosql.spring.boot.autoconfigure.mongodb;

import jakarta.nosql.Template;
import org.eclipse.jnosql.databases.mongodb.mapping.MongoDBTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using Testcontainers MongoDB.
 *
 * <p>Starts a real MongoDB container, boots the auto-configuration via
 * {@link ApplicationContextRunner}, and performs a basic {@link MongoDBTemplate}
 * insert + find round-trip to verify end-to-end wiring.
 */
@Testcontainers
class MongoDBIntegrationTest {

    @Container
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:7.0");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLCoreAutoConfiguration.class,
                    MongoDBAutoConfiguration.class));

    @Test
    void shouldInsertAndFindDocument() {
        contextRunner
                .withPropertyValues(
                        "jnosql.mongodb.url=" + MONGODB.getReplicaSetUrl(),
                        "jnosql.document.database=integrationtest"
                )
                .run(context -> {
                    MongoDBTemplate template = context.getBean(MongoDBTemplate.class);

                    String id = UUID.randomUUID().toString();
                    BookEntity book = new BookEntity(id, "Clean Code");

                    template.insert(book);

                    Optional<BookEntity> found = template.find(BookEntity.class, id);
                    assertThat(found).isPresent();
                    assertThat(found.get().getTitle()).isEqualTo("Clean Code");
                });
    }

    @Test
    void shouldInsertAndFindDocumentByUsingTemplate() {
        contextRunner
                .withPropertyValues(
                        "jnosql.mongodb.url=" + MONGODB.getReplicaSetUrl(),
                        "jnosql.document.database=integrationtest2"
                )
                .run(context -> {
                    Template template = context.getBean(Template.class);

                    String id = UUID.randomUUID().toString();
                    BookEntity book = new BookEntity(id, "Clean Code");

                    template.insert(book);

                    Optional<BookEntity> found = template.find(BookEntity.class, id);
                    assertThat(found).isPresent();
                    assertThat(found.get().getTitle()).isEqualTo("Clean Code");
                });
    }
}
