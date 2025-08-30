/*
 *  Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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
 *   Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;

import jakarta.data.repository.DataRepository;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;


/**
 * This class serves as a JNoSQL discovery bean for CDI extension, responsible for registering Repository instances for Jakarta Persistence entities.
 * It extends {@link AbstractBean} and is parameterized with type {@code T} representing the repository type.
 * <p>
 * Upon instantiation, it initializes with the provided repository type and qualifiers.
 * </p>
 *
 * @param <T> the type of the repository
 * @see AbstractBean
 */
public class RepositoryPersistenceBean<T extends DataRepository<T, ?>> extends AbstractRepositoryPersistenceBean<T> {

    /**
     * Constructor
     *
     * @param type The bean class
     * @param beanManager
     */
    public RepositoryPersistenceBean(Class<?> type, BeanManager beanManager) {
        super(type, beanManager);
    }

    @Override
    public T create(CreationalContext<T> context) {
        EntitiesMetadata entities = getInstance(EntitiesMetadata.class);
        EntityManager entityManager = findEntityManager();
        var template = new PersistenceDocumentTemplate(new PersistenceDatabaseManager(entityManager));
        Converters converters = getInstance(Converters.class);

        var handler = new JakartaPersistenceRepositoryProxy<>(template, entities, type, converters);
        T proxy = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);

        // Apply class-level interceptor bindings using InterceptionFactory
        Set<Annotation> classLevelBindings = getClassLevelInterceptorBindings();
        if (!classLevelBindings.isEmpty()) {
            var interceptionFactory = CDI.current().getBeanManager().createInterceptionFactory(context, type);
            var configurator = interceptionFactory.configure();
            for (Annotation binding : classLevelBindings) {
                configurator.add(binding);
            }
            proxy = (T)interceptionFactory.createInterceptedInstance(proxy);
        }

        // Apply method-level interceptor bindings using custom proxy
        if (hasMethodLevelInterceptorBindings()) {
            proxy = MethodInterceptorProxy.create(proxy, CDI.current().getBeanManager(), context, type);
        }

        return proxy;
    }

}