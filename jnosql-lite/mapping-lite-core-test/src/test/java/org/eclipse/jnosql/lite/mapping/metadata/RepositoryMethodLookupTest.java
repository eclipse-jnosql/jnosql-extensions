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

import jakarta.data.Sort;
import jakarta.data.constraint.Constraint;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.LessThan;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.entities.Person;
import org.eclipse.jnosql.lite.mapping.entities.repository.ComputerRepository;
import org.eclipse.jnosql.lite.mapping.entities.repository.ExampleQuery;
import org.eclipse.jnosql.lite.mapping.entities.repository.Garage;
import org.eclipse.jnosql.lite.mapping.entities.repository.PersonRepository;
import org.eclipse.jnosql.mapping.metadata.repository.MethodSignatureKey;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryAnnotation;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryMethodLookupTest {

    private RepositoriesMetadata repositoriesMetadata;

    @BeforeEach
    void setUp() {
        this.repositoriesMetadata = new LiteRepositoriesMetadata();
    }


    @Nested
    class WhenTheRepositoryMethodTypeIsResolved {

        @Test
        @DisplayName("Should define insert type when there is insert annotation")
        void shouldDefineInsertTypeWhenThereIsInsertAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("insert")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.INSERT);
            });
        }

        @Test
        @DisplayName("Should define update type when there is update annotation")
        void shouldDefineUpdateTypeWhenThereIsUpdateAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("update")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.UPDATE);
            });
        }


        @Test
        @DisplayName("Should define delete type when there is delete annotation")
        void shouldDefineDeleteTypeWhenThereIsDeleteAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("delete")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.DELETE);
            });
        }

        @Test
        @DisplayName("Should define save type when there is save annotation")
        void shouldDefineSaveTypeWhenThereIsSaveAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("save")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.SAVE);
            });
        }


        @Test
        @DisplayName("Should define query type when there is query annotation")
        void shouldDefineInsertTypeWhenThereIsDeleteAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("query")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.QUERY);
            });
        }

        @Test
        @DisplayName("Should define PARAMETER_BASED type when there is find annotation")
        void shouldDefineFindTypeWhenThereIsFindAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            RepositoryMethod method = repositoryMetadata.find(new NameKey("find")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.PARAMETER_BASED);
            });
        }

        @Test
        @DisplayName("Should define FIND_BY where there predicate at method name")
        void shouldDefineFindByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.FIND_BY);
            });
        }

        @Test
        @DisplayName("Should define COUNT_BY where there predicate at method name")
        void shouldDefineCountByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("countByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.COUNT_BY);
            });
        }

        @Test
        @DisplayName("Should define EXISTS_BY where there predicate at method name")
        void shouldDefineExistByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("existsByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.EXISTS_BY);
            });
        }

        @Test
        @DisplayName("Should define DELETE_BY where there predicate at method name")
        void shouldDefineDeleteByWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("deleteByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.DELETE_BY);
            });
        }

        @Test
        @DisplayName("Should define COUNT_ALL where there predicate at method name")
        void shouldDefineCountAllWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("countAll")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.COUNT_ALL);
            });
        }

        @Test
        @DisplayName("Should define FIND_ALL where there predicate at method name")
        void shouldDefineFindAllWhenTheMethodNameHasFindPredicate() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findAll")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.FIND_ALL);
            });
        }

        @Test
        @DisplayName("Should define DEFAULT_METHOD where the method is default")
        void shouldDefineDefaultMethodWhenTheMethodIsDefault() {
            var repositoryMetadata = repositoriesMetadata.get(Garage.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("defaultMethod")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.DEFAULT_METHOD);
            });
        }

        @Test
        @DisplayName("Should define CURSOR_PAGINATION where the return is cursor")
        void shouldDefineCursorPaginationWhenTheReturnIsCursor() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("cursor")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method).as("value of method").isNotNull();
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.CURSOR_PAGINATION);
            });
        }
    }

    @Nested
    class WhenTheRepositoryMethodAnnotationsAreInspected {

        @Test
        @DisplayName("Should read query annotation")
        void shouldReadQueryAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("query")).orElseThrow();
            Optional<String> query = method.query();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(query).as("value of query").isNotEmpty();
                soft.assertThat(query).as("value of query").get().isEqualTo("FROM Computer");
            });
        }

        @Test
        @DisplayName("Should be empty when the query annotation does not have query annotation")
        void shouldReturnEmptyWhenThereIsNotQueryAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(ComputerRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("insert")).orElseThrow();
            Optional<String> query = method.query();
            assertThat(query).as("value of query").isEmpty();
        }

        @Test
        @DisplayName("Should read first annotation")
        void shouldReadFirstAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopTen")).orElseThrow();
            OptionalInt first = method.first();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(first).as("value of first").isNotEmpty();
                soft.assertThat(first.orElseThrow()).as("value of first.orElseThrow()").isEqualTo(10);
            });
        }

        @Test
        @DisplayName("Should read first annotation default value")
        void shouldReadFirstDefaultAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopOne")).orElseThrow();
            OptionalInt first = method.first();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(first).as("value of first").isNotEmpty();
                soft.assertThat(first.orElseThrow()).as("value of first.orElseThrow()").isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Should be empty when the first annotation does not have query annotation")
        void shouldReturnEmptyWhenThereIsNotFirstAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("name")).orElseThrow();
            OptionalInt first = method.first();
            assertThat(first).as("value of first").isEmpty();
        }

        @Test
        @DisplayName("Should read value when the find annotation")
        void shouldReadFindAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopTen")).orElseThrow();
            OptionalInt first = method.first();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(first).as("value of first").isNotEmpty();
                soft.assertThat(first.orElseThrow()).as("value of first.orElseThrow()").isEqualTo(10);
            });
        }

        @Test
        @DisplayName("Should return find default value")
        void shouldReadFindDefaultAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("name")).orElseThrow();
            Optional<Class<?>> find = method.find();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(find).as("value of find").isNotEmpty();
                soft.assertThat(find).as("value of find").get().isEqualTo(void.class);
            });
        }

        @Test
        @DisplayName("Should be empty when there is no find annotation ")
        void shouldReturnEmptyWhenThereIsNotFindAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("countByUsername")).orElseThrow();
            Optional<Class<?>> find = method.find();
            assertThat(find).as("value of find").isEmpty();
        }

        @Test
        @DisplayName("Should return sort value")
        void shouldReadSortAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopOne")).orElseThrow();
            List<Sort<?>> sorts = method.sorts();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(sorts).as("value of sorts").hasSize(1);
                soft.assertThat(sorts).as("value of sorts").contains(Sort.asc("email"));
            });
        }

        @Test
        @DisplayName("Should return sort values")
        void shouldReadSortAnnotations() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopTen")).orElseThrow();
            List<Sort<?>> sorts = method.sorts();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(sorts).as("value of sorts").hasSize(2);
                soft.assertThat(sorts).as("value of sorts").contains(Sort.desc("username"), Sort.asc("email"));
            });
        }

        @Test
        @DisplayName("Should return empty when sort annotation does not exist")
        void shouldReturnEmptyWhenSortDoesNotHaveFindAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("name")).orElseThrow();
            List<Sort<?>> sorts = method.sorts();
            assertThat(sorts).as("value of sorts").isEmpty();
        }

        @Test
        @DisplayName("Should return sort value")
        void shouldReadSelectAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopOne")).orElseThrow();
            var selected = method.select();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(selected).as("value of selected").hasSize(1);
                soft.assertThat(selected).as("value of selected").contains("email");
            });
        }

        @Test
        @DisplayName("Should return sort values")
        void shouldReadSelectAnnotations() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("findTopTen")).orElseThrow();
            var selected = method.select();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(selected).as("value of selected").hasSize(2);
                soft.assertThat(selected).as("value of selected").contains("email", "username");
            });
        }

        @Test
        @DisplayName("Should return empty when sort annotation does not exist")
        void shouldReturnEmptyWhenThereIsNotSelectAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("name")).orElseThrow();
            var selected = method.select();
            assertThat(selected).as("value of selected").isEmpty();
        }

        @Test
        @DisplayName("Should define provider as false")
        void shouldDefineProviderAsFalse() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("name")).orElseThrow();
            List<RepositoryAnnotation> annotations = method.annotations();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(annotations).as("value of annotations").hasSize(1);
                RepositoryAnnotation first = annotations.getFirst();
                soft.assertThat(first).as("value of first").isNotNull();
                soft.assertThat(first.isProviderAnnotation()).as("value of first.isProviderAnnotation()").isFalse();
                soft.assertThat(first.annotation()).as("value of first.annotation()").isEqualTo(Find.class);
                soft.assertThat(first.provider()).as("value of first.provider()").isEmpty();
            });
        }

        @Test
        @DisplayName("Should define provider as true")
        void shouldDefineProviderAsTrue() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("providerSampleQuery")).orElseThrow();
            List<RepositoryAnnotation> annotations = method.annotations();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(annotations).as("value of annotations").hasSize(1);
                RepositoryAnnotation first = annotations.getFirst();
                soft.assertThat(first).as("value of first").isNotNull();
                soft.assertThat(first.isProviderAnnotation()).as("value of first.isProviderAnnotation()").isTrue();
                soft.assertThat(first.annotation()).as("value of first.annotation()").isEqualTo(ExampleQuery.class);
                soft.assertThat(first.provider()).as("value of first.provider()").isNotEmpty().get().isEqualTo("example");
                soft.assertThat(method.type()).as("value of method.type()").isEqualTo(RepositoryMethodType.PROVIDER_OPERATION);
            });
        }

        @Test
        @DisplayName("Should extract query attributes from provider annotation")
        void shouldGetQueryFromProvider() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("providerSampleQuery")).orElseThrow();
            List<RepositoryAnnotation> annotations = method.annotations();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(annotations).as("value of annotations").hasSize(1);
                RepositoryAnnotation first = annotations.getFirst();
                Map<String, Object> attributes = first.attributes();
                soft.assertThat(attributes).as("value of attributes").isNotNull();
                soft.assertThat(attributes).as("value of attributes").containsEntry("value", "sampleQuery");
            });
        }
    }


    @Nested
    class WhenTheRepositoryReturnTypeIsResolved {

        @Test
        @DisplayName("Should return void on return type")
        void shouldReturnVoidOnReturnType() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("deleteByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method.returnType()).as("value of method.returnType()").isPresent().get().isEqualTo(void.class);
                soft.assertThat(method.elementType()).as("value of method.elementType()").isEmpty();
            });
        }

        @Test
        @DisplayName("Should return boolean primitive on return type")
        void shouldReturnBooleanPrimitiveOnReturnType() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("existsByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method.returnType()).as("value of method.returnType()").isPresent().get().isEqualTo(boolean.class);
                soft.assertThat(method.elementType()).as("value of method.elementType()").isEmpty();
            });
        }

        @Test
        @DisplayName("Should return number primitive on return type")
        void shouldReturnNumberPrimitiveOnReturnType() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("countByUsername")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method.returnType()).as("value of method.returnType()").isPresent().get().isEqualTo(long.class);
                soft.assertThat(method.elementType()).as("value of method.elementType()").isEmpty();
            });
        }

        @Test
        @DisplayName("Should return only entity on the return type")
        void shouldReturnOnlyEntityOnReturnType() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("id")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method.returnType()).as("value of method.returnType()").isPresent().get().isEqualTo(Person.class);
                soft.assertThat(method.elementType()).as("value of method.elementType()").isEmpty();
            });
        }

        @Test
        @DisplayName("Should return the structure and element type")
        void shouldReturnTheStructureAndElementType() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("optional")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method.returnType()).as("value of method.returnType()").isPresent().get().isEqualTo(Optional.class);
                soft.assertThat(method.elementType()).as("value of method.elementType()").isPresent().get().isEqualTo(Person.class);
            });
        }

        @Test
        @DisplayName("Should return the structure and element type as iterable")
        void shouldReturnTheStructureAndElementTypeAsIterable() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("array")).orElseThrow();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(method.returnType()).as("value of method.returnType()").isPresent().get().isEqualTo(Person[].class);
                soft.assertThat(method.elementType()).as("value of method.elementType()").isPresent().get().isEqualTo(Person.class);
            });
        }
    }

    @DisplayName("should find by class")
    @Nested
    class WhenTheRepositoryClassIsResolved {

        @Test
        @DisplayName("Should delegate the name query")
        void shouldFindByName() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata.find(new NameKey("optional")).orElseThrow();
            assertThat(method.find()).as("value of method.find()").isPresent();
        }

        @Test
        @DisplayName("Should find by param signature")
        void shouldFindByParamSignature() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("optional", List.of(Long.class)))
                    .orElseThrow();
            assertThat(method.find()).as("value of method.find()").isPresent();
        }
    }

    @DisplayName("should load parameters")
    @Nested
    class WhenTheRepositoryParametersAreResolved {

        @DisplayName("Should describe an entity parameter")
        @Test
        void shouldDescribeEntityParameter() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("findByUsername", List.of(String.class)))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryParam.name()).as("value of repositoryParam.name()").isEqualTo("username");
                soft.assertThat(repositoryParam.param()).as("value of repositoryParam.param()").isEqualTo("username");
                soft.assertThat(repositoryParam.is()).as("value of repositoryParam.is()").isEmpty();
                soft.assertThat(repositoryParam.type()).as("value of repositoryParam.type()").isEqualTo(String.class);
                soft.assertThat(repositoryParam.elementType()).as("value of repositoryParam.elementType()").isEmpty();
            });
        }

        @DisplayName("Should describe a named parameter")
        @Test
        void shouldDescribeNamedParameter() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("name", List.of(String.class)))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryParam.name()).as("value of repositoryParam.name()").isEqualTo("name");
                soft.assertThat(repositoryParam.param()).as("value of repositoryParam.param()").isEqualTo("paramAnnotation");
            });
        }

        @Test
        @DisplayName("Should load by annotation")
        void shouldLoadByAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("list", List.of(String.class)))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryParam.name()).as("value of repositoryParam.name()").isEqualTo("name");
                soft.assertThat(repositoryParam.by()).as("value of repositoryParam.by()").isEqualTo("isAnnotation");
            });
        }

        @Test
        @DisplayName("Should load when method as list or structure")
        void shouldLoadStructureAsList() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("saveList", List.of(List.class)))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryParam.type()).as("value of repositoryParam.type()").isEqualTo(List.class);
                soft.assertThat(repositoryParam.elementType()).as("value of repositoryParam.elementType()").get().isEqualTo(Person.class);
            });
        }

        @Test
        @DisplayName("Should load when method as list or structure")
        void shouldLoadStructureAsArray() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("saveArray", List.of(Person[].class)))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(repositoryParam.type()).as("value of repositoryParam.type()").isEqualTo(Person[].class);
                soft.assertThat(repositoryParam.elementType()).as("value of repositoryParam.elementType()").get().isEqualTo(Person.class);
            });
        }

        @Test
        @DisplayName("Should load default is")
        void shouldLoadDefaultIs() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new MethodSignatureKey("array", List.of(String.class)))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            assertThat(repositoryParam.is()).as("value of repositoryParam.is()").get().isEqualTo(EqualTo.class);
        }

        @Test
        @DisplayName("Should load from is annotation")
        void shouldLoadFromIsAnnotation() {
            var repositoryMetadata = repositoriesMetadata.get(PersonRepository.class).orElseThrow();
            var method = repositoryMetadata
                    .find(new NameKey("findLesserThan"))
                    .orElseThrow();
            List<RepositoryParam> params = method.params();
            RepositoryParam repositoryParam = params.getFirst();
            assertThat(repositoryParam.is()).as("value of repositoryParam.is()").get().isEqualTo(LessThan.class);
        }

    }

}
