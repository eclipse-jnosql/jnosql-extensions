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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import jakarta.data.repository.Repository;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.persistence.EntityManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jnosql.jakartapersistence.CdiUtil;
import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;
import org.eclipse.jnosql.jakartapersistence.communication.PersistenceDatabaseManager;
import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.core.util.AnnotationLiteralUtil;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

/**
 *
 * @author Ondro Mihalyi
 */
public abstract class AbstractRepositoryPersistenceBean<T> extends AbstractBean<T> {

    protected final Class<T> type;

    private final Set<Type> types;

    private final Set<Annotation> qualifiersForBean;

    private final Set<Annotation> qualifiersOnRepository;

    /**
     * Constructor
     *
     * @param type The bean class
     */
    @SuppressWarnings("unchecked")
    public AbstractRepositoryPersistenceBean(Class<?> type) {
        this.type = (Class<T>) type;
        this.types = Collections.singleton(type);
        this.qualifiersOnRepository = initQualifiersOnRepository();
        this.qualifiersForBean = new HashSet<>(qualifiersOnRepository);
        qualifiersForBean.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
        qualifiersForBean.add(AnnotationLiteralUtil.ANY_ANNOTATION);
    }

    @Override
    public Class<?> getBeanClass() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(CreationalContext<T> context) {
        EntitiesMetadata entities = getInstance(EntitiesMetadata.class);

        EntityManager entityManager = findEntityManager();
        var template = new PersistenceDocumentTemplate(new PersistenceDatabaseManager(entityManager));

        Converters converters = getInstance(Converters.class);

        var handler = new JakartaPersistenceRepositoryProxy<>(template,
                entities, type, converters);
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
        return qualifiersForBean;
    }

    @Override
    public String getId() {
        return type.getName() + "@JakartaPersistence";
    }

    protected EntityManager findEntityManager() throws IllegalStateException {
        final Optional<EntityManager> entityManager = getInstance(EntityManagerProvider.class)
                .produceMatchingEntityManager(this::getPersistenceUnit, this::getEntityManagerQualifiers);
        if (entityManager.isEmpty()) {
            throw new IllegalStateException("Found no entity manager matching the " + type + " repository declaration");
        }
        final EntityManager em = entityManager.get();
        return em;
    }

    private String getPersistenceUnit() {
        final Repository annotation = type.getAnnotation(Repository.class);
        final String persistenceUnit = annotation.dataStore();
        return Repository.DEFAULT_DATA_STORE.equals(persistenceUnit)
                ? null
                : persistenceUnit;
    }

    private Annotation[] getEntityManagerQualifiers() {
        return initQualifiersOnRepository().toArray(Annotation[]::new);
    }

    private Set<Annotation> initQualifiersOnRepository() {
        return CdiUtil.getAllQualifiersRecursively(type.getDeclaredAnnotations());
    }

}
