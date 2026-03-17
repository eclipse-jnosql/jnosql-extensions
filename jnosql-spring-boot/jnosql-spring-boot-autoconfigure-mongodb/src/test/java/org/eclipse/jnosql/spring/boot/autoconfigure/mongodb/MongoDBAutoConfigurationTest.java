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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.eclipse.jnosql.databases.mongodb.mapping.MongoDBTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MongoDBAutoConfiguration} using {@link ApplicationContextRunner}.
 *
 * <p>These tests do NOT require a running MongoDB — they use a mock/in-process MongoClient
 * provided via a user configuration bean so the factory and manager can be created.
 */
class MongoDBAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLCoreAutoConfiguration.class,
                    MongoDBAutoConfiguration.class));

    // ------------------------------------------------------------------
    // Scenario 1 — MongoDBTemplate is created when jnosql.document.database is set
    // ------------------------------------------------------------------

    /**
     * Verifies that {@link MongoDBTemplate} bean is present when a user-provided
     * {@link MongoClient} bean exists and {@code jnosql.document.database} is configured.
     */
    @Test
    void mongoDBTemplateShouldBeCreatedWhenDatabaseIsConfigured() {
        contextRunner
                .withUserConfiguration(MongoClientConfig.class)
                .withPropertyValues("jnosql.document.database=testdb")
                .run(context -> assertThat(context).hasSingleBean(MongoDBTemplate.class));
    }

    // ------------------------------------------------------------------
    // Scenario 2 — MongoDBTemplate is NOT created when user bean exists
    // ------------------------------------------------------------------

    /**
     * Verifies that when a user defines their own {@link MongoDBTemplate} bean,
     * the auto-configured bean is NOT created (ConditionalOnMissingBean).
     */
    @Test
    void mongoDBTemplateShouldNotBeCreatedWhenUserDefinesOwnBean() {
        contextRunner
                .withUserConfiguration(MongoClientConfig.class, UserDefinedMongoDBTemplateConfig.class)
                .withPropertyValues("jnosql.document.database=testdb")
                .run(context -> {
                    assertThat(context).hasSingleBean(MongoDBTemplate.class);
                    assertThat(context.getBean(MongoDBTemplate.class))
                            .isSameAs(context.getBean(UserDefinedMongoDBTemplateConfig.BEAN_ID, MongoDBTemplate.class));
                });
    }

    // ------------------------------------------------------------------
    // Scenario 3 — Startup fails with BeanCreationException when database is absent
    // ------------------------------------------------------------------

    /**
     * Verifies that startup fails with {@link BeanCreationException} when
     * {@code jnosql.document.database} is not configured.
     */
    @Test
    void contextShouldFailWhenDatabasePropertyIsAbsent() {
        contextRunner
                .withUserConfiguration(MongoClientConfig.class)
                // no jnosql.document.database property
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .isInstanceOf(Exception.class)
                        .hasMessageContaining("jnosql.document.database"));
    }

    // ------------------------------------------------------------------
    // Supporting test configurations
    // ------------------------------------------------------------------

    /**
     * Provides a real {@link MongoClient} pointed at a local MongoDB that is NOT
     * expected to actually accept connections in unit tests. The MongoDBDocumentManager
     * is only exercised when tests perform actual database calls (integration tests).
     *
     * <p>For pure unit tests with {@link ApplicationContextRunner} the client is supplied
     * so that {@link MongoDBAutoConfiguration} can wire the factory without reading
     * {@code jnosql.mongodb.*} properties from the environment.
     */
    @Configuration
    static class MongoClientConfig {
        @Bean
        MongoClient mongoClient() {
            return MongoClients.create("mongodb://localhost:27017");
        }
    }

    @Configuration
    static class UserDefinedMongoDBTemplateConfig {

        static final String BEAN_ID = "userMongoDBTemplate";

        @Bean(BEAN_ID)
        MongoDBTemplate userMongoDBTemplate() {
            return org.mockito.Mockito.mock(MongoDBTemplate.class);
        }
    }
}
