/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.core.util.AnnotationLiteralUtil;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;


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
public class CustomRepositoryPersistenceBean<T> extends AbstractBean<T> {

    private final Class<T> type;

    private final Set<Type> types;

    private final Set<Annotation> qualifiers;

    /**
     * @param type the bean class
     */
    @SuppressWarnings("unchecked")
    public CustomRepositoryPersistenceBean(Class<?> type) {
        this.type = (Class<T>) type;
        this.types = Collections.singleton(type);
        this.qualifiers = new HashSet<>();
        qualifiers.add(DatabaseQualifier.ofDocument());
        qualifiers.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
        qualifiers.add(AnnotationLiteralUtil.ANY_ANNOTATION);
    }

    @Override
    public Class<?> getBeanClass() {
        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(CreationalContext<T> context) {
        var entities = getInstance(EntitiesMetadata.class);
        var template = getInstance(PersistenceDocumentTemplate.class);

        var converters = getInstance(Converters.class);

        var handler = CustomRepositoryPersistenceHandler.builder()
                .entitiesMetadata(entities)
                .template(template)
                .customRepositoryType(type)
                .converters(converters)
                .build();

        return (T) Proxy.newProxyInstance(type.getClassLoader(),
                new Class[]{type},
                handler);
    }


    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String getId() {
        return type.getName() + "@JakartaPersistence";
    }

}