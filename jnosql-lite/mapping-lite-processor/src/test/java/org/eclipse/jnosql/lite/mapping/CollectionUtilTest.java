/*
 *  Copyright (c) 2023 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping;

import org.eclipse.jnosql.lite.mapping.processing.CollectionUtil;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionUtilTest {


    private final CollectionUtil collectionUtil= CollectionUtil.INSTANCE;
    @Test
    void shouldReturnList(){
        assertThat(collectionUtil.apply(List.class.getName())).isEqualTo(CollectionUtil.NEW_LIST);
        assertThat(collectionUtil.apply(Iterable.class.getName())).isEqualTo(CollectionUtil.NEW_LIST);
        assertThat(collectionUtil.apply(Collection.class.getName())).isEqualTo(CollectionUtil.NEW_LIST);
    }

    @Test
    void shouldReturnSet(){
        assertThat(collectionUtil.apply(Set.class.getName())).isEqualTo(CollectionUtil.NEW_SET);
    }

    @Test
    void shouldReturnDeque(){
        assertThat(collectionUtil.apply(Deque.class.getName())).isEqualTo(CollectionUtil.NEW_DEQUE);
        assertThat(collectionUtil.apply(Queue.class.getName())).isEqualTo(CollectionUtil.NEW_DEQUE);
    }

    @Test
    void shouldReturnTreeSet(){
        assertThat(collectionUtil.apply(NavigableSet.class.getName())).isEqualTo(CollectionUtil.NEW_TREE_SET);
        assertThat(collectionUtil.apply(SortedSet.class.getName())).isEqualTo(CollectionUtil.NEW_TREE_SET);
    }

    @Test
    void shouldReturnDefault(){
        assertThat(collectionUtil.apply(String.class.getName())).isEqualTo(CollectionUtil.DEFAULT);
    }
}