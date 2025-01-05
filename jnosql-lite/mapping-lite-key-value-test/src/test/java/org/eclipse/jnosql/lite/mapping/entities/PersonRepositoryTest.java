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

import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonRepositoryTest {

    @Mock
    private KeyValueTemplate template;

    @InjectMocks
    private PersonRepositoryLiteKeyValue personRepository;


    @Test
    void shouldInsertPerson(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        Person result = this.personRepository.insertPerson(person);
        assertThat(result).isNotNull().isEqualTo(person);
        Mockito.verify(template).insert(eq(person));
    }

    @Test
    void shouldInsertVoid(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        this.personRepository.insertPersonVoid(person);
        Mockito.verify(template).insert(eq(person));
    }

    @Test
    void shouldInsertInt(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        var result = this.personRepository.insertPersonInt(person);
        Mockito.verify(template).insert(eq(person));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldInsertLong(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        var result = this.personRepository.insertPersonLong(person);
        Mockito.verify(template).insert(eq(person));
        Assertions.assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldInsertPersonIterable(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        Iterable<Person> result = this.personRepository.insertIterable(people);
        assertThat(result).isNotNull().contains(person);
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldInsertPersonIterableVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        this.personRepository.insertIterableVoid(people);
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldInsertPersonIterableInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.insertIterableInt(people);
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldInsertPersonIterableLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.insertIterableLong(people);
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }


    @Test
    void shouldInsertPersonArray(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        Person[] result = this.personRepository.insertArray(new Person[]{person});
        assertThat(result).isNotNull().contains(person);
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldInsertPersonArrayVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        this.personRepository.insertArrayVoid(new Person[]{person});
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldInsertPersonArrayInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.insertArrayInt(new Person[]{person});
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldInsertPersonArrayLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.insertArrayLong(new Person[]{person});
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldUpdatePerson(){
        Person person = ada();
        when(template.update(eq(person))).thenReturn(person);
        Person result = this.personRepository.updatePerson(person);
        assertThat(result).isNotNull().isEqualTo(person);
        Mockito.verify(template).update(eq(person));
    }

    @Test
    void shouldUpdateVoid(){
        Person person = ada();
        when(template.update(eq(person))).thenReturn(person);
        this.personRepository.updatePersonVoid(person);
        Mockito.verify(template).update(eq(person));
    }

    @Test
    void shouldUpdateInt(){
        Person person = ada();
        when(template.update(eq(person))).thenReturn(person);
        int result = this.personRepository.updatePersonInt(person);
        Mockito.verify(template).update(eq(person));
        Assertions.assertThat(result).isEqualTo(1);
    }


    @Test
    void shouldUpdateLong(){
        Person person = ada();
        when(template.update(eq(person))).thenReturn(person);
        var result = this.personRepository.updatePersonLong(person);
        Mockito.verify(template).update(eq(person));
        Assertions.assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldUpdatePersonIterable(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        Iterable<Person> result = this.personRepository.updateIterable(people);
        assertThat(result).isNotNull().contains(person);
        Mockito.verify(template).update(eq(people));
    }

    @Test
    void shouldUpdatePersonIterableVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        this.personRepository.updateIterableVoid(people);
        Mockito.verify(template).update(eq(people));
    }

    @Test
    void shouldUpdatePersonIterableInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        int result = this.personRepository.updateIterableInt(people);
        Mockito.verify(template).update(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldUpdatePersonIterableLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        var result = this.personRepository.updateIterableLong(people);
        Mockito.verify(template).update(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }


    @Test
    void shouldUpdatePersonArray(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        Person[] result = this.personRepository.updateArray(new Person[]{person});
        assertThat(result).isNotNull().contains(person);
        Mockito.verify(template).update(eq(people));
    }

    @Test
    void shouldUpdatePersonArrayVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        this.personRepository.updateArrayVoid(new Person[]{person});
        Mockito.verify(template).update(eq(people));
    }

    @Test
    void shouldUpdatePersonArrayInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        int result = this.personRepository.updateArrayInt(new Person[]{person});
        Mockito.verify(template).update(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldUpdatePersonArrayLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.update(eq(people))).thenReturn(people);
        var result = this.personRepository.updateArrayLong(new Person[]{person});
        Mockito.verify(template).update(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldSavePerson(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        Person result = this.personRepository.savePerson(person);
        assertThat(result).isNotNull().isEqualTo(person);
        Mockito.verify(template).insert(eq(person));
    }

    @Test
    void shouldSaveVoid(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        this.personRepository.savePersonVoid(person);
        Mockito.verify(template).insert(eq(person));
    }

    @Test
    void shouldSaveInt(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        var result = this.personRepository.savePersonInt(person);
        Mockito.verify(template).insert(eq(person));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldSaveLong(){
        Person person = ada();
        when(template.insert(eq(person))).thenReturn(person);
        var result = this.personRepository.savePersonLong(person);
        Mockito.verify(template).insert(eq(person));
        Assertions.assertThat(result).isEqualTo(1);
    }


    @Test
    void shouldSavePersonIterable(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        Iterable<Person> result = this.personRepository.saveIterable(people);
        assertThat(result).isNotNull().contains(person);
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldSavePersonIterableVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        this.personRepository.saveIterableVoid(people);
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldSavePersonIterableInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.saveIterableInt(people);
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldSavePersonIterableLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.saveIterableLong(people);
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldSavePersonArray(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        Person[] result = this.personRepository.saveArray(new Person[]{person});
        assertThat(result).isNotNull().contains(person);
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldSavePersonArrayVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        this.personRepository.saveArrayVoid(new Person[]{person});
        Mockito.verify(template).insert(eq(people));
    }

    @Test
    void shouldSavePersonArrayInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.saveArrayInt(new Person[]{person});
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldSavePersonArrayLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        when(template.insert(eq(people))).thenReturn(people);
        var result = this.personRepository.saveArrayLong(new Person[]{person});
        Mockito.verify(template).insert(eq(people));
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldDeletePerson(){
        Person person = ada();
        var result = this.personRepository.deletePerson(person);
        assertThat(result).isTrue();
        Mockito.verify(template).delete(person.getId());
    }

    @Test
    void shouldDeleteVoid(){
        Person person = ada();
        this.personRepository.deletePersonVoid(person);
        Mockito.verify(template).delete(person.getId());
    }

    @Test
    void shouldDeleteInt(){
        Person person = ada();
        var result = this.personRepository.deletePersonInt(person);
        Mockito.verify(template).delete(person.getId());
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldDeleteLong(){
        Person person = ada();
        var result = this.personRepository.deletePersonLong(person);
        Mockito.verify(template).delete(person.getId());
        Assertions.assertThat(result).isEqualTo(1);
    }


    @Test
    void shouldDeletePersonIterable(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        var result = this.personRepository.deleteIterable(people);
        assertThat(result).isTrue();
        Mockito.verify(template).delete(person.getId());
    }

    @Test
    void shouldDeletePersonIterableVoid(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        this.personRepository.deleteIterableVoid(people);
        Mockito.verify(template).delete(person.getId());
    }

    @Test
    void shouldDeletePersonIterableInt(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        var result = this.personRepository.deleteIterableInt(people);
        Mockito.verify(template).delete(person.getId());
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldDeletePersonIterableLong(){
        Person person = ada();
        List<Person> people = Collections.singletonList(person);
        var result = this.personRepository.deleteIterableLong(people);
        Mockito.verify(template).delete(person.getId());
        Assertions.assertThat(result).isEqualTo(1);
    }


    @Test
    void shouldDeletePersonArray(){
        Person person = ada();
        var result = this.personRepository.deleteArray(new Person[]{person});
        assertThat(result).isTrue();
        Mockito.verify(template).delete(person.getId());
    }

    @Test
    void shouldDeletePersonArrayVoid(){
        Person person = ada();
        this.personRepository.deleteArrayVoid(new Person[]{person});
        Mockito.verify(template).delete(person.getId());
    }

    @Test
    void shouldDeletePersonArrayInt(){
        Person person = ada();
        var result = this.personRepository.deleteArrayInt(new Person[]{person});
        Mockito.verify(template).delete(person.getId());
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    void shouldDeletePersonArrayLong(){
        Person person = ada();
        var result = this.personRepository.deleteArrayLong(new Person[]{person});
        Mockito.verify(template).delete(person.getId());
        Assertions.assertThat(result).isEqualTo(1L);
    }

    private static Person ada() {
        Person person = new Person();
        person.setId(10L);
        person.setAge(12);
        person.setName("Ada");
        return person;
    }

}