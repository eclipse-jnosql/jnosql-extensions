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

import org.eclipse.jnosql.mapping.PreparedStatement;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PersonCrudRepositoryTest {
    @Mock
    private ColumnTemplate template;

    @InjectMocks
    private PersonCrudRepositoryLiteColumn personRepository;


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

        verify(template, times(1)).delete(eq(Person.class),eq(person.getId()));
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
    void shouldFindByName(){
        when(template.select(any(SelectQuery.class))).thenReturn( Stream.of(new Person(), new Person()));
        List<Person> result = this.personRepository.findByName("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        assertThat(result).isNotEmpty().hasSize(2);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get(String.class)).isEqualTo("Ada");
        });

    }

    @Test
    void shouldQuery(){
        when(template.prepare(anyString(), Mockito.eq("Person"))).thenReturn(Mockito.mock(PreparedStatement.class));
        this.personRepository.query("Ada");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(template).prepare(captor.capture(), Mockito.eq("Person"));
        String value = captor.getValue();
        assertThat(value).isEqualTo("select * from Person where name = @name");
    }

    @Test
    void shouldExistByName(){
        when(template.select(any(SelectQuery.class))).thenReturn( Stream.of(new Person(), new Person()));
        boolean result = this.personRepository.existsByName("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        assertThat(result).isTrue();
        verify(template).select(captor.capture());
        var query = captor.getValue();
        CriteriaCondition condition = query.condition().orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get(String.class)).isEqualTo("Ada");
        });

    }

    @Test
    void shouldCountByName(){
        when(template.select(any(SelectQuery.class))).thenReturn( Stream.of(new Person(), new Person()));
        long result = this.personRepository.countByName("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        assertThat(result).isEqualTo(2L);
        verify(template).select(captor.capture());
        var query = captor.getValue();
        CriteriaCondition condition = query.condition().orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get(String.class)).isEqualTo("Ada");
        });

    }

    @Test
    void shouldDeleteByName(){
        this.personRepository.deleteByName("Ada");
        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
        verify(template).delete(captor.capture());
        DeleteQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get(String.class)).isEqualTo("Ada");
        });

    }

}