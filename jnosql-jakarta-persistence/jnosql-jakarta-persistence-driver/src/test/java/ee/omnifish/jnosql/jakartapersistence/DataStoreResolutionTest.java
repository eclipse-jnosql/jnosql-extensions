/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.CDI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataStoreResolutionTest {

    private SeContainer cdiContainer;

    @BeforeEach
    void init() {
        TestJakartaPersistenceClassScanner.standardRepositories =
                Set.of(MissingDataStoreRepository.class, ExistingDataStoreRepository.class);
        cdiContainer = TestSupport.cdiInitializerWithDefaultEmProducer().initialize();
    }

    @AfterEach
    void cleanup() {
        cdiContainer.close();
    }

    @Nested
    @DisplayName("When selecting the persistence unit of a repository")
    class WhenTheDataStoreIsSelected {

        @Test
        @DisplayName("Should use the persistence unit named by the data store")
        void shouldUseExistingDataStore() {
            var repository = CDI.current().select(ExistingDataStoreRepository.class).get();

            assertThat(repository.findAll().toList()).isNotNull();
        }

        @Test
        @DisplayName("Should fail instead of falling back to the default persistence unit when the data store does not exist")
        void shouldThrowExceptionWhenDataStoreDoesNotExist() {
            assertThatThrownBy(() -> CDI.current().select(MissingDataStoreRepository.class).get().findAll().toList())
                    .hasMessageContaining("Found no entity manager matching");
        }
    }
}
