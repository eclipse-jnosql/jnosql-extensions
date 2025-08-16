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


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jnosql.jakartapersistence.mapping.repository.CustomRepositoryPersistenceBean;
import org.eclipse.jnosql.jakartapersistence.mapping.repository.RepositoryPersistenceBean;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;

/**
 * This CDI extension, {@code JakartaPersistenceExtension}, observes the CDI
 * container lifecycle events to perform tasks related to Jakarta Persistence
 * repository beans.
 * <p>
 */
public class JakartaPersistenceExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(JakartaPersistenceExtension.class.getName());

    private ClassScanner scanner;

    public void setScanner(ClassScanner scanner) {
        this.scanner = scanner;
    }

    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery) {

        ClassScanner scanner = this.scanner != null ? this.scanner : ClassScanner.load();

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

        afterBeanDiscovery.addBean()
            .types(AbstractBean.class)
            .scope(ApplicationScoped.class)
            .qualifiers(Default.Literal.INSTANCE)
            .createWith(ctx -> {
                // This is just to define beanManager for AbstractBean, it shouldn't be injected
                return null;
            });
    }
}