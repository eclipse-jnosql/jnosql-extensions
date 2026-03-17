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

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.databases.oracle.communication.OracleDocumentConfiguration;
import org.eclipse.jnosql.databases.oracle.communication.OracleDocumentManagerFactory;
import org.eclipse.jnosql.databases.oracle.communication.OracleNoSQLConfigurations;
import org.eclipse.jnosql.databases.oracle.communication.OracleNoSQLDocumentManager;
import org.eclipse.jnosql.databases.oracle.mapping.OracleNoSQLTemplate;
import org.eclipse.jnosql.databases.oracle.mapping.OracleNoSQLTemplateBuilder;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.spring.boot.autoconfigure.JNoSQLCoreAutoConfiguration;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.function.Predicate;

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.DOCUMENT_DATABASE;

/**
 * Spring Boot auto-configuration for Eclipse JNoSQL Oracle NoSQL integration.
 *
 * <p>Provides Oracle NoSQL-specific beans on top of the shared infrastructure declared
 * by {@link JNoSQLCoreAutoConfiguration}. The wiring chain for Oracle NoSQL-specific beans is:
 * <pre>
 * OracleDocumentManagerFactory → OracleNoSQLDocumentManager → OracleNoSQLTemplate
 * </pre>
 *
 * <p>Every bean is annotated with {@link ConditionalOnMissingBean} so that
 * any application-provided bean of the same type takes precedence.
 *
 * <p>Supported configuration properties (all under {@code jnosql.oracle.nosql.*}):
 * <ul>
 *   <li>{@code jnosql.oracle.nosql.host} — host URL (default: {@code http://localhost:8080})</li>
 *   <li>{@code jnosql.oracle.nosql.deployment} — deployment type ({@code ON_PREMISES} or {@code CLOUD})</li>
 *   <li>{@code jnosql.oracle.nosql.user} / {@code jnosql.oracle.nosql.password} — credentials</li>
 *   <li>And other {@link OracleNoSQLConfigurations} properties</li>
 * </ul>
 *
 * <p>The property {@code jnosql.document.database} is required; startup fails with a
 * {@link BeanCreationException} if it is absent.
 */
@AutoConfiguration
@AutoConfigureAfter(JNoSQLCoreAutoConfiguration.class)
public class OracleNoSQLAutoConfiguration {

    /**
     * Creates the {@link OracleDocumentManagerFactory} from the {@code jnosql.oracle.nosql.*}
     * properties available in the Spring {@link Environment}.
     *
     * @param settings the JNoSQL {@link Settings} built from Spring properties
     * @return a configured {@link OracleDocumentManagerFactory}
     */
    @Bean
    @ConditionalOnMissingBean
    public OracleDocumentManagerFactory oracleDocumentManagerFactory(Settings settings) {
        return new OracleDocumentConfiguration().apply(settings);
    }

    /**
     * Creates the {@link OracleNoSQLDocumentManager} for the database/table namespace
     * specified by {@code jnosql.document.database}.
     *
     * @param factory     the document manager factory
     * @param settings    the JNoSQL {@link Settings} built from Spring properties for reading the {@code jnosql.document.database} property
     * @throws BeanCreationException if {@code jnosql.document.database} is not configured
     */
    @Bean
    @ConditionalOnMissingBean
    public OracleNoSQLDocumentManager oracleNoSQLDocumentManager(OracleDocumentManagerFactory factory,
                                                                   Settings settings) {
        String database = settings
                .get(DOCUMENT_DATABASE, String.class)
                .filter(Predicate.not(String::isBlank))
                .orElseThrow(() ->
                        new BeanCreationException("oracleNoSQLDocumentManager",
                                "Required configuration property '%s' is not set. ".formatted(DOCUMENT_DATABASE.get()) +
                                        "Please configure it to specify the Oracle NoSQL table namespace."));
        return factory.apply(database);
    }

    /**
     * Assembles the fully wired {@link OracleNoSQLTemplate} from all required infrastructure beans.
     */
    @Bean
    @Database(DatabaseType.DOCUMENT)
    @ConditionalOnMissingBean
    public OracleNoSQLTemplate oracleNoSQLTemplate(Converters converters,
                                                    EntitiesMetadata entitiesMetadata,
                                                    OracleNoSQLDocumentManager oracleNoSQLDocumentManager,
                                                    EntityConverter entityConverter,
                                                    EventPersistManager eventPersistManager) {
        return OracleNoSQLTemplateBuilder.builder()
                .withConverters(converters)
                .withEntities(entitiesMetadata)
                .withManager(oracleNoSQLDocumentManager)
                .withEntityConverter(entityConverter)
                .withEventPersistManager(eventPersistManager)
                .build();
    }
}
