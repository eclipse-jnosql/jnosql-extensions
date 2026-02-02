/*
 *  Copyright (c) 2025 Otávio Santana and others
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

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.lite.mapping.entities.repository.PersonRepository;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class LiteRepositoriesMetadataTest {

    private RepositoriesMetadata repositoriesMetadata;

    @BeforeEach
    void setUp() {
        this.repositoriesMetadata = new LiteRepositoriesMetadata();
    }

    @Test
    @DisplayName("Should return not null instance")
    void shouldBeNotNullInstance() {
        Assertions.assertThat(repositoriesMetadata).isNotNull();
    }

    @Test
    @DisplayName("Should return empty when repository not found")
    void shouldReturnOptionalEmptyWhenRepositoryNotFound() {
        var repositoryMetadata = repositoriesMetadata.get(String.class);
        Assertions.assertThat(repositoryMetadata).isEmpty();
    }

    @Test
    @DisplayName("Should find when repository found")
    void shouldFindWhenRepositoryFound() {
        var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class);
        Assertions.assertThat(repositoryMetadata).isNotEmpty();
    }

}
