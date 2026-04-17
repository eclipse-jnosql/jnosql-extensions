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

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.SettingsBuilder;
import org.eclipse.jnosql.mapping.core.ConverterResolver;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionEntitiesMetadataBuilder;
import org.eclipse.jnosql.mapping.reflection.ReflectionGroupEntityMetadataBuilder;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactoryBuilder;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManagerBuilder;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import java.util.List;
import java.util.Optional;

/**
 * Spring Boot auto-configuration for Eclipse JNoSQL core infrastructure beans.
 *
 * <p>Provides all database-agnostic infrastructure beans required by JNoSQL database-specific
 * auto-configurations. The wiring chain is:
 * <pre>
 * ClassScanner → ClassConverter → GroupEntityMetadata → EntitiesMetadata →
 * ConverterResolver → Converters → EntityConverterFactory → EntityConverter →
 * EventPersistManager
 * </pre>
 *
 * <p>Every bean is annotated with {@link ConditionalOnMissingBean} so that
 * any application-provided bean of the same type takes precedence.
 *
 * <p>Database-specific auto-configurations (e.g. {@code MongoDBAutoConfiguration},
 * {@code OracleNoSQLAutoConfiguration}) should declare
 * {@code @AutoConfigureAfter(JNoSQLCoreAutoConfiguration.class)} to ensure these
 * shared beans are available when they run.
 */
@AutoConfiguration
public class JNoSQLCoreAutoConfiguration {

    private static final String DEFAULT_ID_FIELD = "_id";

    @Bean
    @ConditionalOnMissingBean
    public SettingsBuilder settingsBuilder(List<SettingsBuilderCustomizer> customizers) {
        SettingsBuilder builder = Settings.builder();
        customizers.forEach(c -> c.customize(builder));
        return builder;
    }

    @Bean
    @ConditionalOnMissingBean
    public Settings settings(SettingsBuilder builder, Environment environment) {
        loadAllProperties(builder, environment);
        return builder.build();
    }

    private void loadAllProperties(SettingsBuilder builder, Environment env) {
        if (env instanceof ConfigurableEnvironment environment) {
            environment.getPropertySources().forEach(ps -> {
                if (ps instanceof MapPropertySource mapPropertySource) {
                    mapPropertySource.getSource().forEach((key, value) -> builder.put(key, value.toString()));
                }
            });
        }
    }

    // -------------------------------------------------------------------------
    // Classpath scanning and entity metadata
    // -------------------------------------------------------------------------

    /**
     * Provides the {@link ClassScanner} for entity/embeddable/repository discovery.
     * Uses the ServiceLoader-based {@link ClassScanner#load()} implementation.
     */
    @Bean
    @ConditionalOnMissingBean
    public ClassScanner classScanner() {
        return ClassScanner.load();
    }

    /**
     * Provides the {@link ClassConverter} that converts a {@link Class} to
     * {@link org.eclipse.jnosql.mapping.metadata.EntityMetadata}.
     * Uses the ServiceLoader-based {@link ClassConverter#load()} implementation.
     */
    @Bean
    @ConditionalOnMissingBean
    public ClassConverter classConverter() {
        return ClassConverter.load();
    }

    /**
     * Builds {@link GroupEntityMetadata} by scanning all entities and embeddables on the classpath.
     */
    @Bean
    @ConditionalOnMissingBean
    public GroupEntityMetadata groupEntityMetadata(ClassScanner classScanner, ClassConverter classConverter) {
        return ReflectionGroupEntityMetadataBuilder.builder()
                .withScanner(classScanner)
                .withConverter(classConverter)
                .build();
    }

    /**
     * Builds {@link EntitiesMetadata} from the scanned {@link GroupEntityMetadata}.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntitiesMetadata entitiesMetadata(GroupEntityMetadata groupEntityMetadata) {
        return ReflectionEntitiesMetadataBuilder.builder()
                .withGroup(groupEntityMetadata)
                .build();
    }

    // -------------------------------------------------------------------------
    // Converter resolution
    // -------------------------------------------------------------------------

    /**
     * Provides a {@link ConverterResolver} that resolves {@link jakarta.nosql.AttributeConverter}
     * instances from the Spring {@link ApplicationContext}, with reflective fallback.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConverterResolver converterResolver(ApplicationContext applicationContext) {
        return new SpringConverterResolver(applicationContext);
    }

    /**
     * Provides the {@link Converters} registry backed by the {@link ConverterResolver}.
     */
    @Bean
    @ConditionalOnMissingBean
    public Converters converters(ConverterResolver converterResolver) {
        return Converters.withResolver(converterResolver);
    }

    // -------------------------------------------------------------------------
    // Entity converter and event manager
    // -------------------------------------------------------------------------

    /**
     * Creates the {@link EntityConverterFactory} from entities metadata and converters.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityConverterFactory entityConverterFactory(EntitiesMetadata entitiesMetadata,
                                                         Converters converters) {
        return EntityConverterFactoryBuilder.builder()
                .withEntities(entitiesMetadata)
                .withConverters(converters)
                .build();
    }

    /**
     * Creates the {@link EntityConverter}, using {@code "_id"} as the default ID field name.
     * Database-specific auto-configurations that require a different ID field name should
     * declare their own {@code @ConditionalOnMissingBean EntityConverter} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityConverter entityConverter(EntityConverterFactory entityConverterFactory) {
        return entityConverterFactory.create(() -> Optional.of(DEFAULT_ID_FIELD));
    }

    /**
     * Creates a no-op {@link EventPersistManager}.
     * In the Spring context there is no CDI event bus; consumers can override this bean
     * to add custom pre/post-persist behaviour.
     */
    @Bean
    @ConditionalOnMissingBean
    public EventPersistManager eventPersistManager(ApplicationEventPublisher eventPublisher) {
        return EventPersistManagerBuilder.builder()
                .withPrePersist(eventPublisher::publishEvent)
                .withPostPersist(eventPublisher::publishEvent)
                .build();
    }

    /**
     * Creates the {@link ProjectorConverter} for mapping entities to projections, using the provided metadata.
     */
    @Bean
    @ConditionalOnMissingBean
    public ProjectorConverter projectorConverter(EntitiesMetadata entitiesMetadata) {
        return new ProjectorConverter(entitiesMetadata);
    }
}
