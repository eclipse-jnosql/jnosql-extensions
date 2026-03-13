/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.extensions.sql.infrastructure;

import ee.omnifish.jnosql.jakartapersistence.EntityManagerProducer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCacheProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.PersistenceRepositoryProducer;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;

/**
 *
 * @author Ondro Mihalyi
 */
public class RepositorySupport {

    private RepositorySupport() {}

    @SuppressWarnings("unchecked")
    public static SeContainerInitializer cdiInitializerWithDefaultEmProducer() {
        try {
            Class.forName("org.eclipse.jnosql.mapping.reflection.repository.ReflectionRepositoriesMetadata");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addExtensions(JakartaPersistenceExtension.class)
                .addPackages(true, PersistenceRepositoryProducer.class)
                .addPackages(true, CoreDeleteOperation.class)
                .addPackages(ClassLoader.getSystemClassLoader()
                        .getDefinedPackage("org.eclipse.jnosql.mapping.reflection.repository"))
                .addPackages(PersistenceDocumentTemplate.class, PersistenceDatabaseManager.class)
                .addBeanClasses(EntityManagerProducer.class, PersistenceUnitCacheProvider.class);
    }


}
