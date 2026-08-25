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

import org.eclipse.jnosql.mapping.metadata.CollectionSupplier;

import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An implementation of {@link CollectionSupplier} to {@link TreeSet}
 */
public class TreeSetSupplier implements CollectionSupplier<TreeSet<?>> {

    @Override
    public boolean test(Class<?> type) {
        return NavigableSet.class.equals(type)
                || SortedSet.class.equals(type);
    }

    @Override
    public TreeSet<?> get() {
        return new TreeSet<>();
    }
}
