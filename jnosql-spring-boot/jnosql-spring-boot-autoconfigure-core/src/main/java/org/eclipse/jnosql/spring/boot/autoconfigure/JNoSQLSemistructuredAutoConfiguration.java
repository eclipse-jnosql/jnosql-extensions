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

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.BuiltInMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.CustomRepositoryResolver;
import org.eclipse.jnosql.mapping.core.repository.DefaultMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;
import org.eclipse.jnosql.mapping.core.repository.ProviderQueryHandlerResolver;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDefaultMethodOperatorBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperationBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreInsertOperationBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreProviderOperationBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreSaveOperationBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreUpdateOperationBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultBuiltInMethodOperatorBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultCustomRepositoryMethodOperatorBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.DefaultObjectMethodOperatorBuilder;
import org.eclipse.jnosql.mapping.core.repository.operations.InfrastructureOperatorProviderBuilder;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
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
import org.eclipse.jnosql.mapping.reflection.repository.ReflectionRepositoriesMetadataBuilder;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.eclipse.jnosql.mapping.semistructured.repository.RepositoryOperationProviderBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCountAllOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCountByOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredCursorPaginationOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredDeleteByOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredExistsByOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredFindAllOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredFindByOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredParameterBasedOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredQueryBuilderBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredQueryOperationBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducer;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredRepositoryProducerBuilder;
import org.eclipse.jnosql.mapping.semistructured.repository.SemistructuredReturnTypeBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot autoconfiguration for Eclipse JNoSQL semistructured repository infrastructure.
 *
 * <p>
 * This configuration wires all 14 repository operation beans, the infrastructure operator provider,
 * and the repository producer using step builders to enable Spring Boot integration with CDI-backed
 * JNoSQL libraries without modifying the original CDI classes.
 *
 * <p>
 * The wiring chain is:
 * <pre>
 * Converters → SemistructuredQueryBuilder
 * EntitiesMetadata + ProjectorConverter → SemistructuredReturnType
 * SemiStructuredTemplate → SemistructuredCountAllOperation
 * (QueryBuilder + ReturnType) → SemistructuredFindByOperation
 * (QueryBuilder + ReturnType) → SemistructuredFindAllOperation
 * QueryBuilder → SemistructuredCountByOperation
 * QueryBuilder → SemistructuredExistsByOperation
 * QueryBuilder → SemistructuredDeleteByOperation
 * (QueryBuilder + ReturnType + EntitiesMetadata) → SemistructuredQueryOperation
 * (QueryBuilder + ReturnType + EntitiesMetadata + Converters) → SemistructuredParameterBasedOperation
 * (QueryBuilder + ReturnType + Converters) → SemistructuredCursorPaginationOperation
 * </pre>
 *
 * <p>
 * Every bean is annotated with {@link ConditionalOnMissingBean} so that any
 * application-provided bean of the same type takes precedence.
 */
@AutoConfiguration
@AutoConfigureAfter(JNoSQLCoreAutoConfiguration.class)
public class JNoSQLSemistructuredAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProviderQueryHandlerResolver providerQueryHandlerResolver() {
        return () -> null;
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomRepositoryResolver customRepositoryResolver() {
        return repoClass -> null;
    }

    @Bean
    @ConditionalOnMissingBean
    public BuiltInMethodOperator builtInMethodOperator() {
        return DefaultBuiltInMethodOperatorBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMethodOperator objectMethodOperator() {
        return DefaultObjectMethodOperatorBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultMethodOperator defaultMethodOperator() {
        return CoreDefaultMethodOperatorBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomRepositoryMethodOperator customRepositoryMethodOperator(CustomRepositoryResolver resolver) {
        return DefaultCustomRepositoryMethodOperatorBuilder.builder()
                .withResolver(resolver)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public InfrastructureOperatorProvider infrastructureOperatorProvider(
            BuiltInMethodOperator builtIn,
            ObjectMethodOperator objectOp,
            CustomRepositoryMethodOperator custom,
            DefaultMethodOperator defaultOp) {
        return InfrastructureOperatorProviderBuilder.builder()
                .withBuiltIn(builtIn)
                .withObject(objectOp)
                .withCustom(custom)
                .withDefault(defaultOp)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public InsertOperation insertOperation() {
        return CoreInsertOperationBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public UpdateOperation updateOperation() {
        return CoreUpdateOperationBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SaveOperation saveOperation() {
        return CoreSaveOperationBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DeleteOperation deleteOperation() {
        return CoreDeleteOperationBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CountAllOperation countAllOperation() {
        return SemistructuredCountAllOperationBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public FindByOperation findByOperation(
            Converters converters,
            EntitiesMetadata entities,
            ProjectorConverter projector) {
        return SemistructuredFindByOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .withReturnType(
                        SemistructuredReturnTypeBuilder.builder()
                                .withEntities(entities)
                                .withProjector(projector)
                                .build()
                )
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public FindAllOperation findAllOperation(
            Converters converters,
            EntitiesMetadata entities,
            ProjectorConverter projector) {
        return SemistructuredFindAllOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .withReturnType(
                        SemistructuredReturnTypeBuilder.builder()
                                .withEntities(entities)
                                .withProjector(projector)
                                .build()
                )
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CountByOperation countByOperation(Converters converters) {
        return SemistructuredCountByOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExistsByOperation existsByOperation(Converters converters) {
        return SemistructuredExistsByOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DeleteByOperation deleteByOperation(Converters converters) {
        return SemistructuredDeleteByOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryOperation queryOperation(Converters converters,
                                         EntitiesMetadata entitiesMetadata,
                                         ProjectorConverter projector) {
        return SemistructuredQueryOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .withReturnType(
                        SemistructuredReturnTypeBuilder.builder()
                                .withEntities(entitiesMetadata)
                                .withProjector(projector)
                                .build()
                )
                .withEntities(entitiesMetadata)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ParameterBasedOperation parameterBasedOperation(
            ProjectorConverter projector,
            EntitiesMetadata entitiesMetadata,
            Converters converters) {
        return SemistructuredParameterBasedOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .withReturnType(
                        SemistructuredReturnTypeBuilder.builder()
                                .withEntities(entitiesMetadata)
                                .withProjector(projector)
                                .build()
                )
                .withEntities(entitiesMetadata)
                .withConverters(converters)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CursorPaginationOperation cursorPaginationOperation(
            ProjectorConverter projector,
            EntitiesMetadata entitiesMetadata,
            Converters converters) {
        return SemistructuredCursorPaginationOperationBuilder.builder()
                .withQueryBuilder(SemistructuredQueryBuilderBuilder.builder()
                        .withConverters(converters)
                        .build())
                .withReturnType(
                        SemistructuredReturnTypeBuilder.builder()
                                .withEntities(entitiesMetadata)
                                .withProjector(projector)
                                .build()
                )
                .withConverters(converters)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProviderOperation providerOperation(ProviderQueryHandlerResolver resolver) {
        return CoreProviderOperationBuilder.builder()
                .withResolver(resolver)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RepositoryOperationProvider repositoryOperationProvider(
            InsertOperation insertOp,
            UpdateOperation updateOp,
            DeleteOperation deleteOp,
            SaveOperation saveOp,
            FindByOperation findByOp,
            FindAllOperation findAllOp,
            CountByOperation countByOp,
            CountAllOperation countAllOp,
            ExistsByOperation existsByOp,
            DeleteByOperation deleteByOp,
            QueryOperation queryOp,
            ParameterBasedOperation paramBasedOp,
            CursorPaginationOperation cursorPaginationOp,
            ProviderOperation providerOp) {
        return RepositoryOperationProviderBuilder.builder()
                .withInsert(insertOp)
                .withUpdate(updateOp)
                .withDelete(deleteOp)
                .withSave(saveOp)
                .withFindBy(findByOp)
                .withFindAll(findAllOp)
                .withCountBy(countByOp)
                .withCountAll(countAllOp)
                .withExistsBy(existsByOp)
                .withDeleteBy(deleteByOp)
                .withQuery(queryOp)
                .withParamBased(paramBasedOp)
                .withCursorPagination(cursorPaginationOp)
                .withProvider(providerOp)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SemistructuredRepositoryProducer semistructuredRepositoryProducer(
            EntitiesMetadata entities,
            RepositoriesMetadata repositories,
            InfrastructureOperatorProvider infrastructure,
            RepositoryOperationProvider operations) {
        return SemistructuredRepositoryProducerBuilder.builder()
                .withEntities(entities)
                .withRepositories(repositories)
                .withInfrastructure(infrastructure)
                .withOperations(operations)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RepositoriesMetadata repositoriesMetadata(ApplicationEventPublisher eventPublisher) {
        return ReflectionRepositoriesMetadataBuilder.builder()
                .withObserver(eventPublisher::publishEvent)
                .build();
    }

    @Bean
    public SemistructuredJakartaDataRepositoryRegistrar repositoryRegistrar(
            ApplicationContext applicationContext,
            SemistructuredRepositoryProducer semistructuredRepositoryProducer,
            ClassScanner classScanner) {
        return new SemistructuredJakartaDataRepositoryRegistrar(applicationContext, classScanner, semistructuredRepositoryProducer);
    }

}