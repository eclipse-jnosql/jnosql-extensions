/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.lite.mapping.entities;

import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PersonRepositoryTest {
    @Mock
    private GraphTemplate template;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @Mock
    private LifecycleEventHandler lifecycleEventHandler;

    @InjectMocks
    private PersonRepositoryLiteGraph personRepository;

    @Nested
    @DisplayName("When entities are saved")
    class WhenTheEntitiesAreSaved {


        @Test
        @DisplayName("Should save the entity")
        void shouldSaveEntity() {
            Person person = new Person();
            when(template.insert(eq(person))).thenReturn(person);

            Person savedPerson = personRepository.save(person);

            assertThat(savedPerson).as("value of savedPerson").isNotNull();
            verify(template, times(1)).insert(eq(person));
        }


        @Test
        @DisplayName("Should save every entity")
        void shouldSaveAllEntities() {
            List<Person> persons = Arrays.asList(new Person(), new Person());
            Iterable<Person> savedPersons = personRepository.saveAll(persons);
            assertThat(savedPersons).as("value of savedPersons").isNotNull();
            verify(template, Mockito.times(2)).insert(new Person());
        }
    }

    @Nested
    @DisplayName("When entities are deleted")
    class WhenTheEntitiesAreDeleted {


        @Test
        @DisplayName("Should delete the entity by identifier")
        void shouldDeleteEntityById() {
            Long id = 123L;

            personRepository.deleteById(id);

            verify(template, times(1)).delete(eq(Person.class), eq(id));
        }


        @Test
        @DisplayName("Should delete the entity")
        void shouldDeleteEntity() {
            Person person = new Person();

            personRepository.delete(person);

            verify(template, times(1)).delete(eq(Person.class), eq(person.getId()));
        }


        @Test
        @DisplayName("Should delete all entities")
        void shouldDeleteAllEntities() {
            personRepository.deleteAll();

            verify(template, times(1)).deleteAll(eq(Person.class));
        }
    }

    @Nested
    @DisplayName("When entities are retrieved")
    class WhenTheEntitiesAreRetrieved {


        @Test
        @DisplayName("Should find the entity by identifier")
        void shouldFindEntityById() {
            Long id = 123L;
            Person person = new Person();
            when(template.find(eq(Person.class), eq(id))).thenReturn(Optional.of(person));

            Optional<Person> foundPerson = personRepository.findById(id);

            assertThat(foundPerson.isPresent()).as("value of foundPerson.isPresent()").isTrue();
            verify(template, times(1)).find(eq(Person.class), eq(id));
        }


        @Test
        @DisplayName("Should find every entity")
        void shouldFindAllEntities() {
            Stream<Object> personStream = Stream.of(new Person());
            when(template.select(any(SelectQuery.class))).thenReturn(personStream);

            Stream<Person> allPersons = personRepository.findAll();

            assertThat(allPersons).as("value of allPersons").isNotNull();
            verify(template, times(1)).select(any(SelectQuery.class));
        }


        @Test
        @DisplayName("Should find every entity with a requested identifier")
        void shouldFindAllEntitiesByIds() {
            List<Long> ids = Arrays.asList(123L, 456L);
            Person person1 = new Person();
            Person person2 = new Person();
            when(template.find(eq(Person.class), anyLong())).thenReturn(Optional.of(person1), Optional.of(person2));

            Stream<Person> foundPersons = personRepository.findByIdIn(ids);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(foundPersons).as("value of foundPersons").isNotNull();
                soft.assertThat(foundPersons.count()).as("value of foundPersons.count()").isEqualTo(2);
            });
            verify(template, times(ids.size())).find(eq(Person.class), anyLong());
        }


        @Test
        @DisplayName("Should find all entities with page request")
        void shouldFindAllEntitiesWithPageRequest() {
            PageRequest pageRequest = mock(PageRequest.class);
            when(template.select(any(SelectQuery.class))).thenReturn(Stream.of(new Person(), new Person()));

            Page<Person> page = personRepository.findAll(pageRequest, Order.by());

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(page).as("value of page").isNotNull();
                soft.assertThat(page.content()).as("value of page.content()").isEqualTo(List.of(new Person(), new Person()));
            });
            verify(template, times(1)).select(any(SelectQuery.class));
        }
    }

    @Nested
    @DisplayName("When a repository operation is executed")
    class WhenTheRepositoryOperationIsExecuted {


        @Test
        @DisplayName("Should update")
        void shouldUpdate() {
            personRepository.update(new Person());
            verify(template).update(any(Person.class));
        }


        @Test
        @DisplayName("Should insert")
        void shouldInsert() {
            personRepository.insert(new Person());
            verify(template).insert(any(Person.class));
        }


        @Test
        @DisplayName("Should update iterable")
        void shouldUpdateIterable() {
            personRepository.updateAll(List.of(new Person()));
            verify(template).update(any(List.class));
        }


        @Test
        @DisplayName("Should insert iterable")
        void shouldInsertIterable() {
            personRepository.insertAll(List.of(new Person()));
            verify(template).insert(any(List.class));
        }


        @Test
        @DisplayName("Should throw exception if page request is null")
        void shouldThrowExceptionIfPageRequestIsNull() {
            assertThatNullPointerException().as("null input rejection").isThrownBy(() -> personRepository.findAll(null, null));
        }
    }

    @Nested
    @DisplayName("When entities are counted")
    class WhenTheEntitiesAreCounted {


        @Test
        @DisplayName("Should count the entities")
        void shouldCountEntities() {
            long expectedCount = 5L;
            when(template.count(eq(Person.class))).thenReturn(expectedCount);

            long count = personRepository.countBy();

            assertThat(count).as("value of count").isEqualTo(expectedCount);
            verify(template, times(1)).count(eq(Person.class));
        }
    }

    @Nested
    @DisplayName("When entity existence is checked")
    class WhenTheEntityExistenceIsChecked {


        @Test
        @DisplayName("Should report an existing entity by identifier")
        void shouldCheckIfEntityExistsById() {
            Long id = 123L;
            when(template.find(eq(Person.class), eq(id))).thenReturn(Optional.of(new Person()));

            boolean exists = personRepository.existsById(id);

            assertThat(exists).as("value of exists").isTrue();
            verify(template, times(1)).find(eq(Person.class), eq(id));
        }


        @Test
        @DisplayName("Should report absence when the identifier is unknown")
        void shouldReturnFalseIfEntityDoesNotExistById() {
            Long id = 123L;
            when(template.find(eq(Person.class), eq(id))).thenReturn(Optional.empty());

            boolean exists = personRepository.existsById(id);

            assertThat(exists).as("value of exists").isFalse();
            verify(template, times(1)).find(eq(Person.class), eq(id));
        }
    }
}
