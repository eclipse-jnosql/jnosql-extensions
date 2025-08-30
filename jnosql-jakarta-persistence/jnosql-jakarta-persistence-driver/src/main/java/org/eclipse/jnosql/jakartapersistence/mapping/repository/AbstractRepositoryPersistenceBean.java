/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.persistence.EntityManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final Set<Annotation> interceptorBindingsForBean;

    private final BeanManager beanManager;

    /**
     * Constructor
     *
     * @param type The bean class
     */
    @SuppressWarnings("unchecked")
    public AbstractRepositoryPersistenceBean(Class<?> type, BeanManager beanManager) {
        this.type = (Class<T>) type;
        this.types = Collections.singleton(type);
        this.beanManager = beanManager;
        this.qualifiersForBean = initializeQualifiers();
        this.interceptorBindingsForBean = initializeInterceptorBindings();
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

    protected Set<Annotation> getInterceptorBindings() {
        return interceptorBindingsForBean;
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
        // TODO Check if we can externalize reflection, e.g. using ClassGraph
        final Repository annotation = type.getAnnotation(Repository.class);
        final String persistenceUnit = annotation.dataStore();
        return Repository.DEFAULT_DATA_STORE.equals(persistenceUnit)
                ? null
                : persistenceUnit;
    }

    private Annotation[] getEntityManagerQualifiers() {
        // TODO Check if we can externalize reflection, e.g. using ClassGraph
        Set<Annotation> qualifiers = null;
        List<Method> matchingMethods = new ArrayList();
        for (Method method : type.getMethods()) {
            if (returnsEntityManager(method)) {
                if (qualifiers == null) {
                    qualifiers = CdiUtil.getAllQualifiersRecursively(beanManager, method.getAnnotations());
                }
                matchingMethods.add(method);
            }
        }
        if (matchingMethods.size() > 1) {
            throw new AmbiguousResolutionException("Expected at most one method on " + type + " that returns EntityManager, found multiple: "
                    + matchingMethods.stream().map(Method::toString).collect(Collectors.joining(", ")));

        }
        return qualifiers != null ? qualifiers.toArray(Annotation[]::new) : new Annotation[0];
    }

    protected static boolean returnsEntityManager(Method method) {
        return EntityManager.class.isAssignableFrom(method.getReturnType());
    }

    private Set<Annotation> getQualifiersOnRepositoryInterface() {
        // TODO Check if we can externalize reflection, e.g. using ClassGraph
        return CdiUtil.getAllQualifiersRecursively(beanManager, type.getDeclaredAnnotations());
    }

    private Set<Annotation> initializeInterceptorBindings() {
        // TODO Check if we can externalize reflection, e.g. using ClassGraph
        return CdiUtil.getAllInterceptorBindingsRecursively(beanManager, type.getDeclaredAnnotations());
    }

    private Set<Annotation> initializeQualifiers() {
        // TODO Check if we can externalize reflection, e.g. using ClassGraph
        Set<Annotation> qualifiersOnRepository = getQualifiersOnRepositoryInterface();
        if (qualifiersOnRepository.isEmpty()) {
            qualifiersOnRepository.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
            // TODO Port this to JNoSQL core for NoSQL repositories, in BaseRepositoryBean.initializeQualifiers
        }
        qualifiersOnRepository.add(AnnotationLiteralUtil.ANY_ANNOTATION);
        return qualifiersOnRepository;
    }

}
