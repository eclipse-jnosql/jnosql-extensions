/*
 *  Copyright (c) 2023 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.processing;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

public enum CollectionUtil implements Function<String, String> {
    INSTANCE;

    public static final String NEW_LIST = "new java.util.ArrayList<>()";
    public static final String NEW_SET = "new java.util.HashSet<>()";
    public static final String NEW_DEQUE = "new java.util.LinkedList<>()";
    public static final String NEW_TREE_SET = "new java.util.TreeSet<>()";

    public static final String DEFAULT = "null";

    @Override
    public String apply(String type) {
        if (isCollection(type)) {
            return NEW_LIST;
        } else if (isSet(type)) {
            return NEW_SET;
        } else if (isDeque(type)) {
            return NEW_DEQUE;
        } else if (isTreeSet(type)) {
            return NEW_TREE_SET;
        }
        return DEFAULT;
    }

    private boolean isCollection(String type) {
        return toString(List.class).equals(type) ||
                toString(Iterable.class).equals(type)
                || toString(Collection.class).equals(type);
    }

    private boolean isSet(String type) {
        return toString(Set.class).equals(type);
    }

    private boolean isDeque(String type) {
        return toString(Deque.class).equals(type) || toString(Queue.class).equals(type);
    }

    private boolean isTreeSet(String type) {
        return toString(NavigableSet.class).equals(type)
                || toString(SortedSet.class).equals(type);
    }

    private String toString(Class<?> type){
        return type.getName();
    }
}
