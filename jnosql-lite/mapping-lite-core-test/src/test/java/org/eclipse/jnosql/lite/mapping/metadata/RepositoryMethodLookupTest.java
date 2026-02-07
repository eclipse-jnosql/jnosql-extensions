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
import org.eclipse.jnosql.lite.mapping.entities.repository.Garage;
import org.eclipse.jnosql.lite.mapping.entities.repository.PersonRepository;
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

        @Test
        @DisplayName("should define FIND_BY where there predicate at method name")
        void shouldDefineFindByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.FIND_BY);
            });
        }

        @Test
        @DisplayName("should define COUNT_BY where there predicate at method name")
        void shouldDefineCountByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("countByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.COUNT_BY);
            });
        }

        @Test
        @DisplayName("should define EXISTS_BY where there predicate at method name")
        void shouldDefineExistByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("existsByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.EXISTS_BY);
            });
        }

        @Test
        @DisplayName("should define DELETE_BY where there predicate at method name")
        void shouldDefineDeleteByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("deleteByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.DELETE_BY);
            });
        }

        @Test
        @DisplayName("should define COUNT_ALL where there predicate at method name")
        void shouldDefineCountAllWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("countAll")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.COUNT_ALL);
            });
        }

        @Test
        @DisplayName("should define FIND_ALL where there predicate at method name")
        void shouldDefineFindAllWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findAll")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.FIND_ALL);
            });
        }

        @Test
        @DisplayName("should define DEFAULT_METHOD where the method is default")
        void shouldDefineDefaultMethodWhenTheMethodIsDefault() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("defaultMethod")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.DEFAULT_METHOD);
            });
        }

        @Test
        @DisplayName("should define CURSOR_PAGINATION where the return is Cursor")
        void shouldDefineCursorPaginationWhenTheReturnIsCursor() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("cursor")).orElseThrow();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(method).isNotNull();
                soft.assertThat(method.type()).isEqualTo(RepositoryMethodType.CURSOR_PAGINATION);
            });
        }
    }

}
