/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
 *   Otavio Santana
 * Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.Objects;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;


/**
 * Builder class for constructing instances of {@link CustomRepositoryHandler}.
 * This builder facilitates the configuration of various components required
 * to instantiate a custom repository handler.
 */
public class CustomRepositoryPersistenceHandlerBuilder {

    private EntitiesMetadata entitiesMetadata;

    private PersistenceDocumentTemplate template;

    private Class<?> customRepositoryType;

    private Converters converters;

    /**
     * Sets the entities metadata for the custom repository handler.
     *
     * @param entitiesMetadata the {@link EntitiesMetadata} instance
     * @return the current instance of {@link CustomRepositoryPersistenceHandlerBuilder}
     * @throws NullPointerException if the entitiesMetadata is null
     */
    public CustomRepositoryPersistenceHandlerBuilder entitiesMetadata(EntitiesMetadata entitiesMetadata) {
        this.entitiesMetadata = Objects.requireNonNull(entitiesMetadata, "entitiesMetadata is required");
        return this;
    }

    /**
     * Sets the template for the custom repository handler.
     *
     * @param template the {@link SemiStructuredTemplate} instance
     * @return the current instance of {@link CustomRepositoryPersistenceHandlerBuilder}
     * @throws NullPointerException if the template is null
     */
    public CustomRepositoryPersistenceHandlerBuilder template(PersistenceDocumentTemplate template) {
        this.template = Objects.requireNonNull(template, "template is required");
        return this;
    }

    /**
     * Sets the custom repository type for the custom repository handler.
     *
     * @param customRepositoryType the {@link Class} type of the custom repository
     * @return the current instance of {@link CustomRepositoryPersistenceHandlerBuilder}
     * @throws NullPointerException if the customRepositoryType is null
     */
    public CustomRepositoryPersistenceHandlerBuilder customRepositoryType(Class<?> customRepositoryType) {
        this.customRepositoryType = Objects.requireNonNull(customRepositoryType, "customRepositoryType is required");
        return this;
    }

    /**
     * Sets the converters for the custom repository handler.
     *
     * @param converters the {@link Converters} instance
     * @return the current instance of {@link CustomRepositoryPersistenceHandlerBuilder}
     */
    public CustomRepositoryPersistenceHandlerBuilder converters(Converters converters) {
        this.converters = Objects.requireNonNull(converters, "converters is required");
        return this;
    }

    /**
     * Builds and returns a {@link CustomRepositoryHandler} instance configured
     * with the provided components.
     *
     * @return a new instance of {@link CustomRepositoryHandler}
     * @throws IllegalStateException if the entitiesMetadata, template, customRepositoryType, or converters are null
     */
    public CustomRepositoryPersistenceHandler build() {
        return new CustomRepositoryPersistenceHandler(entitiesMetadata, template, customRepositoryType, converters);
    }
}