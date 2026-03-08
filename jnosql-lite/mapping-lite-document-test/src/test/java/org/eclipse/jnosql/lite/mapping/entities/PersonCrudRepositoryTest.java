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

import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CountByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ExistsByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.ParameterBasedOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.QueryOperation;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PersonCrudRepositoryTest {

    @Mock
    private DocumentTemplate template;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @InjectMocks
    private PersonCrudRepositoryLiteDocument personRepository;

    @Test
    void shouldSaveEntity() {
        Person person = new Person();
        when(template.insert(eq(person))).thenReturn(person);

        Person savedPerson = personRepository.save(person);

        assertNotNull(savedPerson);
        verify(template, times(1)).insert(eq(person));
    }

    @Test
    void shouldDeleteEntityById() {
        Long id = 123L;

        personRepository.deleteById(id);

        verify(template, times(1)).delete(eq(Person.class), eq(id));
    }

    @Test
    void shouldFindEntityById() {
        Long id = 123L;
        Person person = new Person();
        when(template.find(eq(Person.class), eq(id))).thenReturn(Optional.of(person));

        Optional<Person> foundPerson = personRepository.findById(id);

        assertTrue(foundPerson.isPresent());
        verify(template, times(1)).find(eq(Person.class), eq(id));
    }

    @Test
    void shouldFindAllEntities() {
        Stream<Object> personStream = Stream.of(new Person());
        when(template.select(any(SelectQuery.class))).thenReturn(personStream);

        Stream<Person> allPersons = personRepository.findAll();

        assertNotNull(allPersons);
        verify(template, times(1)).select(any(SelectQuery.class));
    }

    @Test
    void shouldSaveAllEntities() {
        List<Person> persons = Arrays.asList(new Person(), new Person());
        Iterable<Person> savedPersons = personRepository.saveAll(persons);
        assertNotNull(savedPersons);
        verify(template, Mockito.times(2)).insert(new Person());
    }

    @Test
    void shouldDeleteEntity() {
        Person person = new Person();

        personRepository.delete(person);

        verify(template, times(1)).delete(eq(Person.class), eq(person.getId()));
    }

    @Test
    void shouldFindAllEntitiesByIds() {
        List<Long> ids = Arrays.asList(123L, 456L);
        Person person1 = new Person();
        Person person2 = new Person();
        when(template.find(eq(Person.class), anyLong())).thenReturn(Optional.of(person1), Optional.of(person2));

        Stream<Person> foundPersons = personRepository.findByIdIn(ids);

        assertNotNull(foundPersons);
        assertEquals(2, foundPersons.count());
        verify(template, times(ids.size())).find(eq(Person.class), anyLong());
    }

    @Test
    void shouldCountEntities() {
        long expectedCount = 5L;
        when(template.count(eq(Person.class))).thenReturn(expectedCount);

        long count = personRepository.countBy();

        assertEquals(expectedCount, count);
        verify(template, times(1)).count(eq(Person.class));
    }

    @Test
    void shouldCheckIfEntityExistsById() {
        Long id = 123L;
        when(template.find(eq(Person.class), eq(id))).thenReturn(Optional.of(new Person()));

        boolean exists = personRepository.existsById(id);

        assertTrue(exists);
        verify(template, times(1)).find(eq(Person.class), eq(id));
    }

    @Test
    void shouldReturnFalseIfEntityDoesNotExistById() {
        Long id = 123L;
        when(template.find(eq(Person.class), eq(id))).thenReturn(Optional.empty());

        boolean exists = personRepository.existsById(id);

        assertFalse(exists);
        verify(template, times(1)).find(eq(Person.class), eq(id));
    }

    @Test
    @DisplayName("When invoking a derived findBy query the repository must delegate execution to FindByOperation")
    void shouldFindByName() {

        FindByOperation operation = mock(FindByOperation.class);

        when(repositoryOperationProvider.findByOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(List.of(new Person()));

        personRepository.findByName("Ada");

        verify(repositoryOperationProvider).findByOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking a repository method annotated with @Query the repository must delegate to QueryOperation")
    void shouldQuery() {

        QueryOperation operation = mock(QueryOperation.class);

        when(repositoryOperationProvider.queryOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(List.of(new Person()));

        personRepository.query("Ada");

        verify(repositoryOperationProvider).queryOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking another explicit @Query method the repository must also delegate to QueryOperation")
    void shouldQuery2() {

        QueryOperation operation = mock(QueryOperation.class);

        when(repositoryOperationProvider.queryOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(List.of(new Person()));

        personRepository.query2("Ada");

        verify(repositoryOperationProvider).queryOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking existsBy derived query the repository must delegate to ExistsByOperation")
    void shouldExistsByName() {

        ExistsByOperation operation = mock(ExistsByOperation.class);

        when(repositoryOperationProvider.existsByOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(true);

        personRepository.existsByName("Ada");

        verify(repositoryOperationProvider).existsByOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking countBy derived projection the repository must delegate to CountByOperation")
    void shouldCountByName() {

        CountByOperation operation = mock(CountByOperation.class);

        when(repositoryOperationProvider.countByOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(2L);

        personRepository.countByName("Ada");

        verify(repositoryOperationProvider).countByOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking deleteBy derived query the repository must delegate to DeleteByOperation")
    void shouldDeleteByName() {

        DeleteByOperation operation = mock(DeleteByOperation.class);

        when(repositoryOperationProvider.deleteByOperation()).thenReturn(operation);

        personRepository.deleteByName("Ada");

        verify(repositoryOperationProvider).deleteByOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking parameter-based repository methods the repository must delegate to ParameterBasedOperation")
    void shouldParameterBasedOperation() {

        ParameterBasedOperation operation = mock(ParameterBasedOperation.class);

        when(repositoryOperationProvider.parameterBasedOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(List.of());

        personRepository.age(10);

        verify(repositoryOperationProvider).parameterBasedOperation();
        verify(operation).execute(any());
    }

    @Test
    @DisplayName("When invoking cursor pagination methods the repository must delegate to CursorPaginationOperation")
    void shouldCursorPagination() {

        CursorPaginationOperation operation = mock(CursorPaginationOperation.class);

        when(repositoryOperationProvider.cursorPaginationOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(mock(CursoredPage.class));

        personRepository.findByName("Ada", PageRequest.ofPage(1).size(2));

        verify(repositoryOperationProvider).cursorPaginationOperation();
        verify(operation).execute(any());
    }
}