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
package org.eclipse.jnosql.spring.boot.autoconfigure.oracle;

import jakarta.nosql.Template;
import org.eclipse.jnosql.databases.oracle.mapping.OracleNoSQLTemplate;
import org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLCoreAutoConfiguration;
import org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLSemistructuredAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using Testcontainers Oracle NoSQL Community Edition.
 *
 * <p>Starts a real Oracle NoSQL CE container, boots the auto-configuration via
 * {@link ApplicationContextRunner}, and performs a basic {@link OracleNoSQLTemplate}
 * insert + find round-trip to verify end-to-end wiring.
 */
@Testcontainers
class OracleNoSQLIntegrationTest {

    @Container
    static final GenericContainer<?> ORACLE_NOSQL =
            new GenericContainer<>(DockerImageName.parse("ghcr.io/oracle/nosql:latest-ce"))
                    .withExposedPorts(8080);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JNoSQLCoreAutoConfiguration.class,
                    JNoSQLSemistructuredAutoConfiguration.class,
                    OracleNoSQLAutoConfiguration.class));

    @Test
    void shouldInsertAndFindDocument() {
        String host = "http://" + ORACLE_NOSQL.getHost() + ":" + ORACLE_NOSQL.getFirstMappedPort();

        contextRunner
                .withPropertyValues(
                        "jnosql.oracle.nosql.host=" + host,
                        "jnosql.document.database=integrationtest"
                )
                .run(context -> {
                    OracleNoSQLTemplate template = context.getBean(OracleNoSQLTemplate.class);

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
        String host = "http://" + ORACLE_NOSQL.getHost() + ":" + ORACLE_NOSQL.getFirstMappedPort();

        contextRunner
                .withPropertyValues(
                        "jnosql.oracle.nosql.host=" + host,
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

    @Test
    void shouldInsertAndFindDocumentRepository() {
        String host = "http://" + ORACLE_NOSQL.getHost() + ":" + ORACLE_NOSQL.getFirstMappedPort();

        contextRunner
                .withPropertyValues(
                        "jnosql.oracle.nosql.host=" + host,
                        "jnosql.document.database=integrationtest2"
                )
                .run(context -> {

                    Library library=context.getBean(Library.class);

                    String id = UUID.randomUUID().toString();
                    BookEntity book = new BookEntity(id, "Clean Code");

                    library.save(book);

                    Optional<BookEntity> found = library.findById(id);
                    assertThat(found).isPresent();
                    assertThat(found.get().getTitle()).isEqualTo("Clean Code");
                });
    }
}
