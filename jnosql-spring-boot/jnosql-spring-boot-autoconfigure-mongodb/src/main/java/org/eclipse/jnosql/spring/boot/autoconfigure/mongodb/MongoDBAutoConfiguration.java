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
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfiguration;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentManager;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentManagerFactory;
import org.eclipse.jnosql.databases.mongodb.mapping.MongoDBTemplate;
import org.eclipse.jnosql.databases.mongodb.mapping.MongoDBTemplateBuilder;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLCoreAutoConfiguration;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.function.Predicate;

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.DOCUMENT_DATABASE;

/**
 * Spring Boot auto-configuration for Eclipse JNoSQL MongoDB integration.
 *
 * <p>Provides MongoDB-specific beans on top of the shared infrastructure declared
 * by {@link JNoSQLCoreAutoConfiguration}. The wiring chain for MongoDB-specific beans is:
 * <pre>
 * MongoDBDocumentManagerFactory → MongoDBDocumentManager → MongoDBTemplate
 * </pre>
 *
 * <p>Every bean is annotated with {@link ConditionalOnMissingBean} so that
 * any application-provided bean of the same type takes precedence.
 *
 * <p>The {@link MongoDBDocumentManagerFactory} bean accepts an optional
 * {@link MongoClient} via {@link ObjectProvider}: if an application-provided
 * {@link MongoClient} bean exists it is reused; otherwise a new client is
 * created from {@code jnosql.mongodb.*} properties.
 */
@AutoConfiguration
@AutoConfigureAfter(JNoSQLCoreAutoConfiguration.class)
public class MongoDBAutoConfiguration {

    /**
     * Creates the {@link MongoDBDocumentManagerFactory}.
     *
     * <p>If an application-provided {@link MongoClient} bean exists it is reused.
     * Otherwise a new {@link MongoClient} is created from the {@code jnosql.mongodb.*}
     * configuration properties via {@link MongoDBDocumentConfiguration}.
     *
     * <p>Supported properties when no user {@link MongoClient} is present:
     * <ul>
     *   <li>{@code jnosql.mongodb.host} — host:port (default {@code localhost:27017})</li>
     *   <li>{@code jnosql.mongodb.url} — connection string</li>
     *   <li>{@code jnosql.mongodb.user} / {@code jnosql.mongodb.password} — credentials</li>
     * </ul>
     *
     * @param mongoClientProvider optional application-provided {@link MongoClient}
     * @param settings            the JNoSQL {@link Settings} built from Spring properties
     * @return a configured {@link MongoDBDocumentManagerFactory}
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoDBDocumentManagerFactory mongoDBDocumentManagerFactory(
            ObjectProvider<MongoClient> mongoClientProvider,
            Settings settings) {
        MongoClient existingClient = mongoClientProvider.getIfAvailable();
        if (existingClient != null) {
            return new MongoDBDocumentConfiguration().get(existingClient);
        }
        return new MongoDBDocumentConfiguration().apply(settings);
    }

    /**
     * Creates the {@link MongoDBDocumentManager} for the database specified by
     * {@code jnosql.document.database}.
     *
     * @param factory     the document manager factory
     * @param settings    the JNoSQL {@link Settings} built from Spring properties for reading the document database name
     * @throws BeanCreationException if {@code jnosql.document.database} is not configured
     */
    @Bean
    @ConditionalOnMissingBean
    public MongoDBDocumentManager mongoDBDocumentManager(MongoDBDocumentManagerFactory factory,
                                                         Settings settings) {
        String database = settings
                .get(DOCUMENT_DATABASE, String.class)
                .filter(Predicate.not(String::isBlank))
                .orElseThrow(() ->
                        new BeanCreationException("mongoDBDocumentManager",
                                "Required configuration property '%s' is not set. ".formatted(DOCUMENT_DATABASE.get()) +
                                        "Please configure it to specify the MongoDB database name."));
        return factory.apply(database);
    }

    /**
     * Assembles the fully wired {@link MongoDBTemplate} from all required infrastructure beans.
     */
    @Bean
    @Database(DatabaseType.DOCUMENT)
    @ConditionalOnMissingBean
    public MongoDBTemplate mongoDBTemplate(Converters converters,
                                           EntitiesMetadata entitiesMetadata,
                                           MongoDBDocumentManager mongoDBDocumentManager,
                                           EntityConverter entityConverter,
                                           EventPersistManager eventPersistManager) {
        return MongoDBTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entitiesMetadata)
                .withManager(mongoDBDocumentManager)
                .withEntityConverter(entityConverter)
                .withEventPersistManager(eventPersistManager)
                .build();
    }
}
