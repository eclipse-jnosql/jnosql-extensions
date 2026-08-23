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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.lite.mapping.entities;

import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PersonCrudRepositoryTest {
    @Mock
    private ColumnTemplate template;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @Mock
    private LifecycleEventHandler lifecycleEventHandler;

    @InjectMocks
    private PersonCrudRepositoryLiteColumn personRepository;

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
    @DisplayName("When lifecycle events are handled")
    class WhenLifecycleEventsAreHandled {

        @Test
        @DisplayName("Should handle lifecycle events when an entity is saved")
        void shouldHandleLifecycleEventsWhenEntityIsSaved() {
            Person person = new Person();
            Person savedPerson = new Person();
            when(template.insert(person)).thenReturn(savedPerson);

            personRepository.save(person);

            verify(lifecycleEventHandler).preUpsert(person);
            verify(lifecycleEventHandler).postUpsert(savedPerson);
        }

        @Test
        @DisplayName("Should handle lifecycle events when an entity is inserted")
        void shouldHandleLifecycleEventsWhenEntityIsInserted() {
            Person person = new Person();
            Person insertedPerson = new Person();
            when(template.insert(person)).thenReturn(insertedPerson);

            personRepository.insert(person);

            verify(lifecycleEventHandler).preInsert(person);
            verify(lifecycleEventHandler).postInsert(insertedPerson);
        }

        @Test
        @DisplayName("Should handle lifecycle events when an entity is updated")
        void shouldHandleLifecycleEventsWhenEntityIsUpdated() {
            Person person = new Person();
            Person updatedPerson = new Person();
            when(template.update(person)).thenReturn(updatedPerson);

            personRepository.update(person);

            verify(lifecycleEventHandler).preUpdate(person);
            verify(lifecycleEventHandler).postUpdate(updatedPerson);
        }

        @Test
        @DisplayName("Should handle lifecycle events when an entity is deleted")
        void shouldHandleLifecycleEventsWhenEntityIsDeleted() {
            Person person = new Person();
            person.setId(1L);

            personRepository.delete(person);

            verify(lifecycleEventHandler).preDelete(person);
            verify(lifecycleEventHandler).postDelete(person);
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
