/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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


import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceClassScanner;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.CustomRepositoryPersistenceBean;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.RepositoryPersistenceBean;

/**
 * This CDI extension, {@code JakartaPersistenceExtension}, observes the CDI
 * container lifecycle events to perform tasks related to Jakarta Persistence
 * repository beans.
 * <p>
 */
public class JakartaPersistenceExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceExtension.class.getName());

    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery) {

        ClassScanner scanner = new PersistenceClassScanner();

        Set<Class<?>> crudTypes = scanner.repositoriesStandard();
        Set<Class<?>> customRepositories = scanner.customRepositories();

        LOGGER.info(() -> "Processing Jakarta Persistence extension. Found "
                + crudTypes.size() + " standard repositories, "
                + customRepositories.size() + " custom repositories.");
        LOGGER.fine(() -> "Processing standard repositories as a Jakarta Persistence implementation: " + crudTypes);
        LOGGER.fine(() -> "Processing custom repositories as a Jakarta Persistence implementation: " + customRepositories);

        crudTypes.forEach(type -> {
            afterBeanDiscovery.addBean(new RepositoryPersistenceBean<>(type));
        });

        customRepositories.forEach(type -> {
            afterBeanDiscovery.addBean(new CustomRepositoryPersistenceBean<>(type));
        });

        /* What about custom repositories like MultipleEntityRepo in the Data TCK?
          The DocumentExtension in `jnosql-mapping-document` creates CustomRepositoryDocumentBean beans
        that create repositories backed by CustomRepositoryHandler. We need to suppress this and create our own
        repository handler, because CustomRepositoryHandler for documents is not compatible
        with PersistencePreparedStatement.

        We might need to remove the service file for DocumentExtension, or create our custom repositories as
        lternatives so that they supporess repositories created by the DocumentExtension.
         */
    }
}