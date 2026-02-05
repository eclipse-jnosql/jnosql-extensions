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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.entities.repository.ActorRepository;
import org.eclipse.jnosql.lite.mapping.entities.repository.ComputerRepository;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RepositoryMethodLookupTest {

    private RepositoriesMetadata repositoriesMetadata;

    @BeforeEach
    void setUp() {
        this.repositoriesMetadata = new LiteRepositoriesMetadata();
    }


    @Nested
    class WhenLoadMethodType {

        @Test
        @DisplayName("should define insert type when there is insert annotation")
        void shouldDefineInsertTypeWhenThereIsInsertAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("insert")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.INSERT);
            });
        }

        @Test
        @DisplayName("should define update type when there is update annotation")
        void shouldDefineUpdateTypeWhenThereIsUpdateAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("update")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.UPDATE);
            });
        }


        @Test
        @DisplayName("should define delete type when there is delete annotation")
        void shouldDefineDeleteTypeWhenThereIsDeleteAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("delete")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.DELETE);
            });
        }

        @Test
        @DisplayName("should define save type when there is save annotation")
        void shouldDefineSaveTypeWhenThereIsSaveAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("save")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.SAVE);
            });
        }


        @Test
        @DisplayName("should define query type when there is query annotation")
        void shouldDefineInsertTypeWhenThereIsDeleteAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("query")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.QUERY);
            });
        }

        @Test
        @DisplayName("should define PARAMETER_BASED type when there is find annotation")
        void shouldDefineFindTypeWhenThereIsFindAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("find")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.PARAMETER_BASED);
            });
        }
    }

}
