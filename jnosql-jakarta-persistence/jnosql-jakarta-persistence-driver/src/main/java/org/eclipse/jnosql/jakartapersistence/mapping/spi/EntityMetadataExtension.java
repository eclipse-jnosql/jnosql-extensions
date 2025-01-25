/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.jakartapersistence.mapping.spi;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.persistence.Entity;

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class is a CDI extension to load all classes that has {@link Entity} annotation.
 * This extension will load all classes and put them in a map,
 * where the key is {@link Class#getName()} and the value is {@link EntityMetadata}
 *
 * This is a copy of EntityMetadataExtension for NoSQL entities.
 *
 * TODO: Modify for Persistence entities.
 */
@ApplicationScoped
public class EntityMetadataExtension implements Extension, GroupEntityMetadata {

    private static final Logger LOGGER = Logger.getLogger(EntityMetadataExtension.class.getName());
    private final Map<String, EntityMetadata> mappings = new ConcurrentHashMap<>();

    private final Map<Class<?>, EntityMetadata> classes = new ConcurrentHashMap<>();

    private final ClassConverter converter;

    {
        converter = ClassConverter.load();
    }

    public void afterBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        LOGGER.fine("Starting the scanning process for Entity and Embeddable annotations: ");

        ClassScanner scanner = ClassScanner.load();

        processEntities(scanner);
        processEmbeddables(scanner);

        LOGGER.fine("Finishing the scanning with total of " + classes.size() + " scanned.");
    }

    private void processEntities(ClassScanner scanner) {
        for (Class<?> entity : scanner.entities()) {
            EntityMetadata entityMetadata = convertToEntityMetadata(entity);
            if (entityMetadata.hasEntityName()) {
                mappings.put(entityMetadata.name(), entityMetadata);
            }
            classes.put(entity, entityMetadata);
        }
    }

    private void processEmbeddables(ClassScanner scanner) {
        for (Class<?> embeddable : scanner.embeddables()) {
            EntityMetadata entityMetadata = convertToEntityMetadata(embeddable);
            classes.put(embeddable, entityMetadata);
        }
    }

    private EntityMetadata convertToEntityMetadata(Class<?> entityClass) {
        return converter.apply(entityClass);
    }

    @Override
    public Map<String, EntityMetadata> mappings() {
        return mappings;
    }

    @Override
    public Map<Class<?>, EntityMetadata> classes() {
        return classes;
    }

    @Override
    public String toString() {
        return "EntityMetadataExtension{" + "classConverter=" + converter +
                ", mappings-size=" + mappings.size() +
                ", classes=" + classes +
                '}';
    }
}
