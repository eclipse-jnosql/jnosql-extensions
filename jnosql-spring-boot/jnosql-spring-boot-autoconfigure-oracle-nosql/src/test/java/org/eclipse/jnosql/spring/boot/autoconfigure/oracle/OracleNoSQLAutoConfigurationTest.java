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

import org.eclipse.jnosql.databases.oracle.communication.OracleDocumentManagerFactory;
import org.eclipse.jnosql.databases.oracle.communication.OracleNoSQLDocumentManager;
import org.eclipse.jnosql.databases.oracle.mapping.OracleNoSQLTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OracleNoSQLAutoConfiguration} using {@link ApplicationContextRunner}.
 *
 * <p>These tests do NOT require a running Oracle NoSQL instance — they use a mock
 * {@link OracleDocumentManagerFactory} provided via a user configuration bean so that
 * the auto-configured beans can be wired without a real database connection.
 */
class OracleNoSQLAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLCoreAutoConfiguration.class,
                    OracleNoSQLAutoConfiguration.class));

    // ------------------------------------------------------------------
    // Scenario 1 — OracleNoSQLTemplate is created when all required props are present
    // ------------------------------------------------------------------

    /**
     * Verifies that {@link OracleNoSQLTemplate} bean is present when a mock
     * {@link OracleDocumentManagerFactory} is supplied and {@code jnosql.document.database}
     * is configured.
     */
    @Test
    void oracleNoSQLTemplateShouldBeCreatedWhenDatabaseIsConfigured() {
        contextRunner
                .withUserConfiguration(MockOracleFactoryConfig.class)
                .withPropertyValues("jnosql.document.database=testdb")
                .run(context -> assertThat(context).hasSingleBean(OracleNoSQLTemplate.class));
    }

    // ------------------------------------------------------------------
    // Scenario 2 — @ConditionalOnMissingBean: user-provided bean wins
    // ------------------------------------------------------------------

    /**
     * Verifies that when a user defines their own {@link OracleNoSQLTemplate} bean,
     * the auto-configured bean is NOT created.
     */
    @Test
    void oracleNoSQLTemplateShouldNotBeCreatedWhenUserDefinesOwnBean() {
        contextRunner
                .withUserConfiguration(MockOracleFactoryConfig.class, UserDefinedOracleTemplateConfig.class)
                .withPropertyValues("jnosql.document.database=testdb")
                .run(context -> {
                    assertThat(context).hasSingleBean(OracleNoSQLTemplate.class);
                    assertThat(context.getBean(OracleNoSQLTemplate.class))
                            .isSameAs(context.getBean(
                                    UserDefinedOracleTemplateConfig.BEAN_ID, OracleNoSQLTemplate.class));
                });
    }

    // ------------------------------------------------------------------
    // Scenario 3 — BeanCreationException when jnosql.document.database is missing
    // ------------------------------------------------------------------

    /**
     * Verifies that startup fails with {@link BeanCreationException} when
     * {@code jnosql.document.database} is not configured.
     */
    @Test
    void contextShouldFailWhenDatabasePropertyIsAbsent() {
        contextRunner
                .withUserConfiguration(MockOracleFactoryConfig.class)
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
     * Provides a mock {@link OracleDocumentManagerFactory} so the auto-configuration
     * can wire the manager bean without attempting to connect to a real Oracle NoSQL instance.
     */
    @Configuration
    static class MockOracleFactoryConfig {
        @Bean
        OracleDocumentManagerFactory oracleDocumentManagerFactory() {
            OracleDocumentManagerFactory factory = mock(OracleDocumentManagerFactory.class);
            when(factory.apply(anyString())).thenReturn(mock(OracleNoSQLDocumentManager.class));
            return factory;
        }
    }

    @Configuration
    static class UserDefinedOracleTemplateConfig {

        static final String BEAN_ID = "userOracleNoSQLTemplate";

        @Bean(BEAN_ID)
        OracleNoSQLTemplate userOracleNoSQLTemplate() {
            return mock(OracleNoSQLTemplate.class);
        }
    }
}
