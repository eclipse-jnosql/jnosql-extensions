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

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.core.ConverterResolver;
import org.eclipse.jnosql.mapping.metadata.FieldParameterMetadata;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ConverterResolver} implementation that bridges the Spring {@link ApplicationContext}
 * to JNoSQL's converter resolution pipeline.
 *
 * <p>When resolving a converter for a given {@link FieldParameterMetadata}, this implementation:
 * <ol>
 *   <li>Extracts the converter {@link Class} from the metadata via {@link FieldParameterMetadata#converter()}.</li>
 *   <li>Attempts to retrieve a Spring-managed bean of that type from the {@link ApplicationContext}.</li>
 *   <li>Falls back to reflective instantiation via {@code getDeclaredConstructor()} if no Spring bean exists.</li>
 * </ol>
 *
 * <p>Instances of this class are not CDI-managed; lifecycle is controlled by the Spring container.
 */
public class SpringConverterResolver implements ConverterResolver {

    private static final Logger LOGGER = Logger.getLogger(SpringConverterResolver.class.getName());

    private final ApplicationContext applicationContext;

    /**
     * Creates a new {@code SpringConverterResolver} backed by the given {@link ApplicationContext}.
     *
     * @param applicationContext the Spring application context; must not be {@code null}
     * @throws NullPointerException if {@code applicationContext} is {@code null}
     */
    public SpringConverterResolver(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext is required");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<AttributeConverter<?, ?>> resolve(FieldParameterMetadata metadata) {
        Objects.requireNonNull(metadata, "The metadata is required");
        Class<?> converterType = metadata.converter().orElse(null);
        if (converterType == null) {
            return Optional.empty();
        }
        try {
            AttributeConverter<?, ?> bean = (AttributeConverter<?, ?>) applicationContext.getBean(converterType);
            return Optional.of(bean);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.log(Level.FINE, "Converter type {0} not found in Spring context; falling back to reflective instantiation", converterType);
            try {
                AttributeConverter<?, ?> instance = (AttributeConverter<?, ?>) converterType.getDeclaredConstructor().newInstance();
                return Optional.of(instance);
            } catch (ReflectiveOperationException reflectiveException) {
                LOGGER.log(Level.WARNING, "Failed to instantiate converter type {0} reflectively", converterType);
                return Optional.empty();
            }
        }
    }
}
