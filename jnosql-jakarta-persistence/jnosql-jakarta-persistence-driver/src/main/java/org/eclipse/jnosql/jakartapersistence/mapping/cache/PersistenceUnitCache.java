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

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.metamodel.EntityType;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Ondro Mihalyi
 */
public interface PersistenceUnitCache {
    Map<String, EntityType<?>> getEntityTypesByName();
    void setEntityTypesByNameSupplier(Supplier<Map<String, EntityType<?>>> supplier);
    <T> CriteriaQuery<T> getOrCreateSelectQuery(Object key, Function<Object, CriteriaQuery<T>> supplier);
    String getOrCreateStringQuery(Object key, Function<Object, String> supplier);
}
