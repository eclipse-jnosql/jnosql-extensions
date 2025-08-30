/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

import java.lang.reflect.Proxy;

import org.eclipse.jnosql.jakartapersistence.CdiUtil;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;


/**
 * This class serves as a JNoSQL discovery bean for CDI extension, responsible for registering Custom Repository nstances for the Jakarta Persistence extension.
 * It extends {@link AbstractBean} and is parameterized with type {@code T} representing the repository type.
 * <p>
 * Upon instantiation, it initializes with the provided repository type and qualifiers.
 * </p>
 *
 * @param <T> the type of the repository
 * @see AbstractBean
 */
public class CustomRepositoryPersistenceBean<T> extends AbstractRepositoryPersistenceBean<T> {

    /**
     * @param type the bean class
     * @param beanManager
     */
    public CustomRepositoryPersistenceBean(Class<?> type, BeanManager beanManager) {
        super(type, beanManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(CreationalContext<T> context) {
        var entities = getInstance(EntitiesMetadata.class);
        var template = createTemplate();
        var converters = getInstance(Converters.class);

        var handler = CustomRepositoryPersistenceHandler.builder()
                .entitiesMetadata(entities)
                .template(template)
                .customRepositoryType(type)
                .converters(converters)
                .build();

        T proxy = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);

        return CdiUtil.copyInterceptors(proxy, type, context, beanManager);
    }

}
