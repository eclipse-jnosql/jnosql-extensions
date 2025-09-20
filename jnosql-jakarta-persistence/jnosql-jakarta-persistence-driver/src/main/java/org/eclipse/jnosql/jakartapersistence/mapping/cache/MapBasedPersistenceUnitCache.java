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

import jakarta.persistence.metamodel.EntityType;

import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Ondro Mihalyi
 */
public class MapBasedPersistenceUnitCache implements PersistenceUnitCache {

    private volatile Map<String, EntityType<?>> entityTypesByName = null;
    private Supplier<Map<String, EntityType<?>>> entityTypesByNameSupplier;

    @Override
    public Map<String, EntityType<?>> getEntityTypesByName() {
        Map<String, EntityType<?>> entityTypesByNameLocal = this.entityTypesByName;
        if (entityTypesByNameLocal == null) {
            synchronized (this) {
                entityTypesByNameLocal = this.entityTypesByName;
                if (entityTypesByNameLocal == null) {
                    this.entityTypesByName = entityTypesByNameLocal = entityTypesByNameSupplier.get();
                }
            }
        }
        return entityTypesByNameLocal;
    }

    @Override
    public void setEntityTypesByNameSupplier(Supplier<Map<String, EntityType<?>>> supplier) {
        this.entityTypesByNameSupplier = supplier;
    }

}
