/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.data.exceptions.MappingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class CollectionSupplierProviderTest {

    @Nested
    @DisplayName("When selecting a collection supplier")
    class WhenTheSupplierIsSelected {

        @ParameterizedTest(name = "Should select {1} for {0}")
        @MethodSource("collectionTypes")
        @DisplayName("Should select the supported supplier")
        void shouldSelectTheSupportedSupplier(Class<?> collectionType,
                                              Class<?> supplierType,
                                              Class<?> resultType) {
            var supplier = CollectionSupplierProvider.find(collectionType);

            var result = supplier.get();

            assertSoftly(softly -> {
                softly.assertThat(supplier)
                        .as("supplier for " + collectionType.getName())
                        .isInstanceOf(supplierType);
                softly.assertThat(result)
                        .as("collection created for " + collectionType.getName())
                        .isInstanceOf(resultType)
                        .isEmpty();
            });
        }

        static Stream<Arguments> collectionTypes() {
            return Stream.of(
                    Arguments.of(List.class, ListSupplier.class, ArrayList.class),
                    Arguments.of(Iterable.class, ListSupplier.class, ArrayList.class),
                    Arguments.of(Collection.class, ListSupplier.class, ArrayList.class),
                    Arguments.of(Deque.class, DequeSupplier.class, LinkedList.class),
                    Arguments.of(Queue.class, DequeSupplier.class, LinkedList.class),
                    Arguments.of(Set.class, SetSupplier.class, HashSet.class),
                    Arguments.of(NavigableSet.class, TreeSetSupplier.class, TreeSet.class),
                    Arguments.of(SortedSet.class, TreeSetSupplier.class, TreeSet.class)
            );
        }

        @Test
        @DisplayName("Should reject an unsupported collection type")
        void shouldRejectAnUnsupportedCollectionType() {
            assertThatExceptionOfType(MappingException.class)
                    .as("unsupported collection type")
                    .isThrownBy(() -> CollectionSupplierProvider.find(ArrayList.class))
                    .withMessageContaining(ArrayList.class.toString());
        }
    }
}
