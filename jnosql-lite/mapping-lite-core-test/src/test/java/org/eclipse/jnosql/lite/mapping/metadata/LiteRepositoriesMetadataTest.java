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

    @Test
    @DisplayName("Should load person repository")
    void shouldLoadPersonRepository() {
        var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repositoryMetadata).isNotNull();
            soft.assertThat(repositoryMetadata.type()).isEqualTo(PersonRepository.class);
            soft.assertThat(repositoryMetadata.entity().orElseThrow()).isEqualTo(Person.class);
        });
    }

    @Test
    @DisplayName("Should load Computer repository")
    void shouldLoadComputerRepository() {
        var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repositoryMetadata).isNotNull();
            soft.assertThat(repositoryMetadata.type()).isEqualTo(ComputerRepository.class);
            soft.assertThat(repositoryMetadata.entity().orElseThrow()).isEqualTo(Computer.class);
        });
    }

    @Test
    @DisplayName("Should load actor repository")
    void shouldLoadActorRepository() {
        var repositoryMetadata = repositoriesMetadata.get(ActorRepository.class).orElseThrow();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repositoryMetadata).isNotNull();
            soft.assertThat(repositoryMetadata.type()).isEqualTo(ActorRepository.class);
            soft.assertThat(repositoryMetadata.entity().orElseThrow()).isEqualTo(Actor.class);
        });
    }


    @Test
    @DisplayName("Should load garage repository")
    void shouldLoadGarage() {
        var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(repositoryMetadata).isNotNull();
            soft.assertThat(repositoryMetadata.type()).isEqualTo(Garage.class);
            soft.assertThat(repositoryMetadata.entity()).isEmpty();
        });
    }

}
