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
package org.eclipse.jnosql.spring.boot.autoconfigure;

import org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryResolver;
import org.eclipse.jnosql.mapping.core.repository.DefaultMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.ProviderQueryHandlerResolver;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ExistsByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindAllOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.InsertOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.SaveOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.UpdateOperation;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JNoSQLSemistructuredAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JNoSQLCoreAutoConfiguration.class,
                    JNoSQLSemistructuredAutoConfiguration.class));

    @Test
    @DisplayName("Should create all semistructured repository infrastructure beans")
    void shouldCreateAllBeans() {
        contextRunner
                .withUserConfiguration(MockDependenciesConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ProviderQueryHandlerResolver.class);
                    assertThat(context).hasSingleBean(CustomRepositoryResolver.class);
                    assertThat(context).hasSingleBean(BuiltInMethodOperator.class);
                    assertThat(context).hasSingleBean(ObjectMethodOperator.class);
                    assertThat(context).hasSingleBean(DefaultMethodOperator.class);
                    assertThat(context).hasSingleBean(CustomRepositoryMethodOperator.class);
                    assertThat(context).hasSingleBean(InfrastructureOperatorProvider.class);
                    assertThat(context).hasSingleBean(InsertOperation.class);
                    assertThat(context).hasSingleBean(UpdateOperation.class);
                    assertThat(context).hasSingleBean(SaveOperation.class);
                    assertThat(context).hasSingleBean(DeleteOperation.class);
                    assertThat(context).hasSingleBean(CountAllOperation.class);
                    assertThat(context).hasSingleBean(FindByOperation.class);
                    assertThat(context).hasSingleBean(FindAllOperation.class);
                    assertThat(context).hasSingleBean(CountByOperation.class);
                    assertThat(context).hasSingleBean(ExistsByOperation.class);
                    assertThat(context).hasSingleBean(DeleteByOperation.class);
                    assertThat(context).hasSingleBean(QueryOperation.class);
                    assertThat(context).hasSingleBean(ParameterBasedOperation.class);
                    assertThat(context).hasSingleBean(CursorPaginationOperation.class);
                    assertThat(context).hasSingleBean(ProviderOperation.class);
                    assertThat(context).hasSingleBean(SemistructuredRepositoryProducer.class);
                });
    }

    @Test
    @DisplayName("Should not override user-provided beans")
    void shouldNotOverrideUserBeans() {
        contextRunner
                .withUserConfiguration(MockDependenciesConfig.class, CustomOperationConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(FindByOperation.class);
                    assertThat(context.getBean(FindByOperation.class))
                            .isSameAs(context.getBean(CustomOperationConfig.CUSTOM_FIND_BY));
                });
    }

    @Configuration
    static class MockDependenciesConfig {
        @Bean
        EntitiesMetadata entitiesMetadata() {
            return mock(EntitiesMetadata.class);
        }

        @Bean
        RepositoriesMetadata repositoriesMetadata() {
            return mock(RepositoriesMetadata.class);
        }

        @Bean
        ProjectorConverter projectorConverter() {
            return mock(ProjectorConverter.class);
        }

        @Bean
        SemiStructuredTemplate semiStructuredTemplate() {
            return mock(SemiStructuredTemplate.class);
        }
    }

    @Configuration
    static class CustomOperationConfig {
        static final String CUSTOM_FIND_BY = "customFindByOperation";

        @Bean(CUSTOM_FIND_BY)
        FindByOperation customFindByOperation() {
            return mock(FindByOperation.class);
        }
    }
}