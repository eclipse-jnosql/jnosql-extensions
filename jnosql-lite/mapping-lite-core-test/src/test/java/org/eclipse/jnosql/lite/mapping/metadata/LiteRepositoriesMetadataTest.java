/*
 *  Copyright (c) 2025 Otávio Santana and others
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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.entities.Actor;
import org.eclipse.jnosql.lite.mapping.entities.Computer;
import org.eclipse.jnosql.lite.mapping.entities.Person;
import org.eclipse.jnosql.lite.mapping.entities.repository.ActorRepository;
import org.eclipse.jnosql.lite.mapping.entities.repository.ComputerRepository;
import org.eclipse.jnosql.lite.mapping.entities.repository.Garage;
import org.eclipse.jnosql.lite.mapping.entities.repository.PersonRepository;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

public class LiteRepositoriesMetadataTest {

    private RepositoriesMetadata repositoriesMetadata;

    @BeforeEach
    void setUp() {
        this.repositoriesMetadata = new LiteRepositoriesMetadata();
    }

    @Nested
    @DisplayName("When the repository registry is queried")
    class WhenTheRepositoryRegistryIsQueried {


        @Test
        @DisplayName("Should create the repository registry")
        void shouldCreateRepositoryRegistry() {
            assertThat(repositoriesMetadata).as("value of repositoriesMetadata").isNotNull();
        }


        @Test
        @DisplayName("Should return empty when the repository is unknown")
        void shouldReturnEmptyWhenRepositoryIsUnknown() {
            var repositoryMetadata = repositoriesMetadata.get(String.class);
            assertThat(repositoryMetadata).as("value of repositoryMetadata").isEmpty();
        }


        @Test
        @DisplayName("Should return metadata when the repository is known")
        void shouldReturnMetadataWhenRepositoryIsKnown() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class);
            assertThat(repositoryMetadata).as("value of repositoryMetadata").isNotEmpty();
        }


        @Test
        @DisplayName("Should load person repository")
        void shouldLoadPersonRepository() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryMetadata).as("value of repositoryMetadata").isNotNull();
                soft.assertThat(repositoryMetadata.type()).as("value of repositoryMetadata.type()").isEqualTo(PersonRepository.class);
                soft.assertThat(repositoryMetadata.entity().orElseThrow()).as("value of repositoryMetadata.entity().orElseThrow()").isEqualTo(Person.class);
            });
        }


        @Test
        @DisplayName("Should load computer repository")
        void shouldLoadComputerRepository() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryMetadata).as("value of repositoryMetadata").isNotNull();
                soft.assertThat(repositoryMetadata.type()).as("value of repositoryMetadata.type()").isEqualTo(ComputerRepository.class);
                soft.assertThat(repositoryMetadata.entity().orElseThrow()).as("value of repositoryMetadata.entity().orElseThrow()").isEqualTo(Computer.class);
            });
        }


        @Test
        @DisplayName("Should load actor repository")
        void shouldLoadActorRepository() {
            var repositoryMetadata = repositoriesMetadata.get(ActorRepository.class).orElseThrow();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryMetadata).as("value of repositoryMetadata").isNotNull();
                soft.assertThat(repositoryMetadata.type()).as("value of repositoryMetadata.type()").isEqualTo(ActorRepository.class);
                soft.assertThat(repositoryMetadata.entity().orElseThrow()).as("value of repositoryMetadata.entity().orElseThrow()").isEqualTo(Actor.class);
            });
        }


        @Test
        @DisplayName("Should load repository metadata without an entity type")
        void shouldLoadRepositoryWithoutEntityType() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryMetadata).as("value of repositoryMetadata").isNotNull();
                soft.assertThat(repositoryMetadata.type()).as("value of repositoryMetadata.type()").isEqualTo(Garage.class);
                soft.assertThat(repositoryMetadata.entity()).as("value of repositoryMetadata.entity()").isEmpty();
            });
        }
    }
}
