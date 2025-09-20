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
package org.eclipse.jnosql.jakartapersistence.mapping.cache;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
public class PersistenceUnitCacheProvider {

    // The keys are persistence unit names
    private ConcurrentHashMap<String, PersistenceUnitCache> caches;

    @PostConstruct
    public void init() {
       caches = new ConcurrentHashMap<>();
    }

    public PersistenceUnitCache getCacheFor(EntityManagerFactory entityManagerFactory) {
        return caches.computeIfAbsent(entityManagerFactory.getName(), this::createQueryCache);
    }

    private PersistenceUnitCache createQueryCache(String persistenceUnitName) {
        return new MapBasedPersistenceUnitCache();
    }

}
