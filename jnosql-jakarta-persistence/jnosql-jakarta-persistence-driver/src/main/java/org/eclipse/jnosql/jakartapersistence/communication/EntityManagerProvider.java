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
package org.eclipse.jnosql.jakartapersistence.communication;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.EntityManager;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author Ondro Mihalyi
 */
@ApplicationScoped
public class EntityManagerProvider {

    public Optional<EntityManager> produceMatchingEntityManager(Supplier<String> persistenceUnitSupplier, Supplier<Annotation[]> qualifiersSupplier) {
        Optional<EntityManager> result = Optional.empty();
        boolean qualifiersPresent = false;
        if (result.isEmpty()) {
            var qualifiers = qualifiersSupplier.get();
            if (qualifiers != null && qualifiers.length > 0) {
                qualifiersPresent = true;
                result = produceEntityManagerForQualifiers(qualifiers);
            }
        }
        if (result.isEmpty()) {
            var persistenceUnit = persistenceUnitSupplier.get();
            if (persistenceUnit != null && !persistenceUnit.isBlank()) {
                result = produceEntityManagerForPersistenceUnit(persistenceUnit);
            }
        }
        if (result.isEmpty() && !qualifiersPresent) {
            result = this.produceDefaultEntityManager();
        }
        return result;
    }

    protected Optional<EntityManager> produceEntityManagerForQualifiers(Annotation... qualifiers) {
        final Instance<EntityManager> emSelector = CDI.current().select(EntityManager.class, qualifiers);
        return emSelector.isResolvable() ? Optional.of(emSelector.get()) : Optional.empty();
    }

    protected Optional<EntityManager> produceEntityManagerForPersistenceUnit(String persistenceUnit) {
        for (EntityManager em : CDI.current().select(EntityManager.class, Any.Literal.INSTANCE)) {
            if (em.getEntityManagerFactory().getName().equals(persistenceUnit)) {
                return Optional.of(em);
            }
        }
        return Optional.empty();
    }

    protected Optional<EntityManager> produceDefaultEntityManager() {
        return produceEntityManagerForQualifiers();
    }

}
