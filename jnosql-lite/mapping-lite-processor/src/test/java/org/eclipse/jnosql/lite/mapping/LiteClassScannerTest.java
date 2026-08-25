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

import jakarta.data.repository.DataRepository;
import org.eclipse.jnosql.lite.mapping.metadata.LiteClassScanner;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class LiteClassScannerTest {

    @Test
    void testEntitiesIsEmpty() {
        LiteClassScanner scanner = new LiteClassScanner();
        Set<Class<?>> entities = scanner.entities();

        assertTrue(entities.isEmpty());
    }

    @Test
    void testRepositoriesIsEmpty() {
        LiteClassScanner scanner = new LiteClassScanner();
        Set<Class<?>> repositories = scanner.repositories();

        assertTrue(repositories.isEmpty());
    }

    @Test
    void testEmbeddablesIsEmpty() {
        LiteClassScanner scanner = new LiteClassScanner();
        Set<Class<?>> embeddables = scanner.embeddables();

        assertTrue(embeddables.isEmpty());
    }

    @Test
    void testRepositoriesWithFilterIsEmpty() {
        LiteClassScanner scanner = new LiteClassScanner();
        Set<Class<?>> repositoriesWithFilter = scanner.repositories(SomeDataRepository.class);

        assertTrue(repositoriesWithFilter.isEmpty());
    }

    @Test
    void testRepositoriesStandardIsEmpty() {
        LiteClassScanner scanner = new LiteClassScanner();
        Set<Class<?>> repositoriesStandard = scanner.repositoriesStandard();

        assertTrue(repositoriesStandard.isEmpty());
    }

    interface SomeDataRepository<ID, E> extends DataRepository<ID, E> {
        // Define a simple data repository class for testing purposes
    }


}

