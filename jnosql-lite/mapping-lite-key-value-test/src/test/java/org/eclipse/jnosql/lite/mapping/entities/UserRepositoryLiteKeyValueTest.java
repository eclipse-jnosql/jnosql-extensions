/*
 *   Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *    and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.entities;

import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryLiteKeyValueTest {

    @Mock
    private KeyValueTemplate template;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @Mock
    private LifecycleEventHandler lifecycleEventHandler;

    @InjectMocks
    private UserRepositoryLiteKeyValue userRepository;

    @Nested
    @DisplayName("When entities are saved")
    class WhenTheEntitiesAreSaved {


        @Test
        @DisplayName("Should save the entity")
        void shouldSaveEntity() {
            User user = new User();
            userRepository.save(user);
            verify(template, times(1)).put(eq(user));
        }


        @Test
        @DisplayName("Should save every entity")
        void shouldSaveAllEntities() {
            User user1 = new User("ada", "Ada", 36);
            User user2 = new User("grace", "Grace", 85);
            List<User> entities = Arrays.asList(user1, user2);

            userRepository.saveAll(entities);

            verify(template).put(user1);
            verify(template).put(user2);
        }
    }

    @Nested
    @DisplayName("When lifecycle events are handled")
    class WhenLifecycleEventsAreHandled {

        @Test
        @DisplayName("Should handle lifecycle events when an entity is saved")
        void shouldHandleLifecycleEventsWhenEntityIsSaved() {
            User user = new User("ada", "Ada", 36);
            User savedUser = new User("ada", "Ada Lovelace", 36);
            when(template.put(user)).thenReturn(savedUser);

            userRepository.save(user);

            verify(lifecycleEventHandler).preUpsert(user);
            verify(lifecycleEventHandler).postUpsert(savedUser);
        }

        @Test
        @DisplayName("Should handle lifecycle events when an entity is inserted")
        void shouldHandleLifecycleEventsWhenEntityIsInserted() {
            User user = new User("ada", "Ada", 36);
            User insertedUser = new User("ada", "Ada Lovelace", 36);
            when(template.put(user)).thenReturn(insertedUser);

            userRepository.insert(user);

            verify(lifecycleEventHandler).preInsert(user);
            verify(lifecycleEventHandler).postInsert(insertedUser);
        }

        @Test
        @DisplayName("Should handle lifecycle events when an entity is updated")
        void shouldHandleLifecycleEventsWhenEntityIsUpdated() {
            User user = new User("ada", "Ada", 36);
            User updatedUser = new User("ada", "Ada Lovelace", 36);
            when(template.put(user)).thenReturn(updatedUser);

            userRepository.update(user);

            verify(lifecycleEventHandler).preUpdate(user);
            verify(lifecycleEventHandler).postUpdate(updatedUser);
        }

        @Test
        @DisplayName("Should handle lifecycle events when an entity is deleted")
        void shouldHandleLifecycleEventsWhenEntityIsDeleted() {
            User user = new User("ada", "Ada", 36);

            userRepository.delete(user);

            verify(lifecycleEventHandler).preDelete(user);
            verify(lifecycleEventHandler).postDelete(user);
        }
    }

    @Nested
    @DisplayName("When entities are deleted")
    class WhenTheEntitiesAreDeleted {


        @Test
        @DisplayName("Should delete by identifier")
        void shouldDeleteById() {
            String id = "123";

            userRepository.deleteById(id);

            verify(template, times(1)).delete(eq(id));
        }
    }

    @Nested
    @DisplayName("When entities are retrieved")
    class WhenTheEntitiesAreRetrieved {


        @Test
        @DisplayName("Should find by identifier")
        void shouldFindById() {
            String id = "123";
            when(template.get(eq(id), eq(User.class))).thenReturn(Optional.of(new User()));

            userRepository.findById(id);

            verify(template, times(1)).get(eq(id), eq(User.class));
        }


        @Test
        @DisplayName("Should find all by identifiers")
        void shouldFindAllByIds() {
            String id1 = "123";
            String id2 = "456";
            Iterable<String> ids = Arrays.asList(id1, id2);
            when(template.get(eq(id1), eq(User.class))).thenReturn(Optional.of(new User()));
            when(template.get(eq(id2), eq(User.class))).thenReturn(Optional.of(new User()));

            List<User> users = userRepository.findByIdIn(ids).toList();

            verify(template, times(2)).get(anyString(), eq(User.class));

            assertThat(users).as("value of users").isNotNull().isNotEmpty().hasSize(2);
        }
    }

    @Nested
    @DisplayName("When entity existence is checked")
    class WhenTheEntityExistenceIsChecked {


        @Test
        @DisplayName("Should report an existing entity by identifier")
        void shouldCheckIfEntityExistsById() {
            String id = "123";
            when(template.get(eq(id), eq(User.class))).thenReturn(Optional.of(new User()));

            boolean exists = userRepository.existsById(id);

            assertThat(exists).as("value of exists").isTrue();
        }


        @Test
        @DisplayName("Should report absence when the identifier is unknown")
        void shouldReturnFalseIfEntityDoesNotExistById() {
            String id = "123";
            when(template.get(eq(id), eq(User.class))).thenReturn(Optional.empty());

            boolean exists = userRepository.existsById(id);

            assertThat(exists).as("value of exists").isFalse();
        }
    }

    @Nested
    @DisplayName("When a repository operation is executed")
    class WhenTheRepositoryOperationIsExecuted {


        @Test
        @DisplayName("Should insert")
        void shouldInsert() {
            User user = new User("Ada", "Lovelace", 10);
            userRepository.insert(user);
            verify(template, times(1)).put(eq(user));
        }


        @Test
        @DisplayName("Should update")
        void shouldUpdate() {
            User user = new User("Ada", "Lovelace", 10);
            userRepository.update(user);
            verify(template, times(1)).put(eq(user));
        }


        @Test
        @DisplayName("Should insert iterable")
        void shouldInsertIterable() {
            User user = new User("Ada", "Lovelace", 10);
            userRepository.insertAll(List.of(user));
            verify(template, times(1)).put(eq(List.of(user)));
        }


        @Test
        @DisplayName("Should update iterable")
        void shouldUpdateIterable() {
            User user = new User("Ada", "Lovelace", 10);
            userRepository.updateAll(List.of(user));
            verify(template, times(1)).put(eq(List.of(user)));
        }


        @Test
        @DisplayName("Should reject counting when the store does not support it")
        void shouldThrowUnsupportedOperationExceptionOnCount() {
            assertThatExceptionOfType(UnsupportedOperationException.class).as("expected exception").isThrownBy(() -> userRepository.countBy());
        }


        @Test
        @DisplayName("Should reject paged retrieval when the store does not support it")
        void shouldThrowUnsupportedOperationExceptionOnFindAllWithPageable() {
            assertThatExceptionOfType(UnsupportedOperationException.class).as("expected exception").isThrownBy(() -> userRepository.findAll(null, null));
        }


        @Test
        @DisplayName("Should reject unbounded retrieval when the store does not support it")
        void shouldThrowUnsupportedOperationExceptionOnFindAll() {
            assertThatExceptionOfType(UnsupportedOperationException.class).as("expected exception").isThrownBy(() -> userRepository.findAll());
        }
    }
}
