/*
 *  Copyright (c) 2020 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.data.exceptions.MappingException;
import org.eclipse.jnosql.mapping.metadata.CollectionSupplier;

import java.util.ArrayList;
import java.util.List;

final class CollectionSupplierProvider {

    private static final List<CollectionSupplier<?>> SUPPLIER;

    static {
        SUPPLIER = new ArrayList<>();
        SUPPLIER.add(new DequeSupplier());
        SUPPLIER.add(new ListSupplier());
        SUPPLIER.add(new SetSupplier());
        SUPPLIER.add(new TreeSetSupplier());
    }

    private CollectionSupplierProvider() {
    }

    static CollectionSupplier<?> find(Class<?> collectionType) {
        return SUPPLIER.stream()
                .filter(c -> c.test(collectionType))
                .findFirst()
                .orElseThrow(() -> new MappingException("There is not support the collection type: " + collectionType));
    }
}
