/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import ee.omnifish.jnosql.jakartapersistence.EntityManagerProducer;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.jakartapersistence.mapping.cache.PersistenceUnitCacheProvider;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableWeld
class DefaultSqlTemplateTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    JakartaPersistenceExtension.class,
                    EntityManagerProducer.class,
                    PersistenceUnitCacheProvider.class
            )
            .build();

    @Inject
    private EntityManager entityManager;

    private SqlTemplate sqlTemplate;

    @BeforeEach
    void setUp() {
        this.sqlTemplate = DefaultSqlTemplate.of(entityManager);
        this.sqlTemplate.deleteAll(Computer.class);
    }


    @Nested
    @DisplayName("When inserting entities into the persistence context")
    class WhenInsert {

        @Test
        @DisplayName("Should persist a new entity and assign an identifier")
        void shouldInsertEntity() {
            Computer computer = Computer.of("MacBook", 2024);

            Computer inserted = sqlTemplate.insert(computer);

            assertThat(inserted.getId()).isNotZero();
        }

        @Test
        @DisplayName("Should persist all entities from a collection")
        void shouldInsertEntities() {
            List<Computer> computers = List.of(
                    Computer.of("MacBook", 2024),
                    Computer.of("ThinkPad", 2023)
            );

            Iterable<Computer> inserted = sqlTemplate.insert(computers);

            assertThat(inserted).hasSize(2);
        }

        @Test
        @DisplayName("Should throw NullPointerException when the entity is null")
        void shouldThrowExceptionWhenEntityIsNull() {
            assertThatThrownBy(() -> sqlTemplate.insert(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when the entities collection is null")
        void shouldThrowExceptionWhenEntitiesIsNull() {
            assertThatThrownBy(() -> sqlTemplate.insert((Iterable<Computer>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entities is null");
        }
    }

    @Nested
    @DisplayName("When updating entities in the persistence context")
    class WhenUpdate {

        @Test
        @DisplayName("Should merge the entity state into the persistence context")
        void shouldUpdateEntity() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            Computer updated = sqlTemplate.update(computer);

            assertThat(updated).isNotNull();
        }

        @Test
        @DisplayName("Should merge all entities from a collection")
        void shouldUpdateEntities() {
            List<Computer> computers = List.of(
                    sqlTemplate.insert(Computer.of("MacBook", 2024)),
                    sqlTemplate.insert(Computer.of("ThinkPad", 2023))
            );

            Iterable<Computer> updated = sqlTemplate.update(computers);

            assertThat(updated).hasSize(2);
        }

        @Test
        @DisplayName("Should throw NullPointerException when the entity is null")
        void shouldThrowExceptionWhenEntityIsNull() {
            assertThatThrownBy(() -> sqlTemplate.update((Object) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is null");
        }
    }

    @Nested
    @DisplayName("When deleting entities from the persistence context")
    class WhenDelete {

        @Test
        @DisplayName("Should remove a managed entity from the database")
        void shouldDeleteEntity() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            sqlTemplate.delete(computer);

            Optional<Computer> found =
                    sqlTemplate.find(Computer.class, computer.getId());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should remove all entities from the provided collection")
        void shouldDeleteEntities() {
            List<Computer> computers = List.of(
                    sqlTemplate.insert(Computer.of("MacBook", 2024)),
                    sqlTemplate.insert(Computer.of("ThinkPad", 2023))
            );

            sqlTemplate.delete(computers);

            List<Computer> remaining =
                    sqlTemplate.findAll(Computer.class).toList();

            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException when the entity is null")
        void shouldThrowExceptionWhenEntityIsNull() {
            assertThatThrownBy(() -> sqlTemplate.delete((Computer) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is null");
        }
    }

    @Nested
    @DisplayName("When retrieving entities by identifier")
    class WhenFind {

        @Test
        @DisplayName("Should return the entity when it exists in the database")
        void shouldFindEntityById() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            Optional<Computer> found =
                    sqlTemplate.find(Computer.class, computer.getId());

            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Should return an empty result when the entity does not exist")
        void shouldReturnEmptyWhenEntityNotFound() {
            Optional<Computer> found =
                    sqlTemplate.find(Computer.class, 999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("When retrieving all entities of a given type")
    class WhenFindAll {

        @Test
        @DisplayName("Should return all persisted entities of the specified type")
        void shouldReturnAllEntities() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            List<Computer> computers =
                    sqlTemplate.findAll(Computer.class).toList();

            assertThat(computers).hasSize(2);
        }
    }

    @Nested
    @DisplayName("When removing all entities of a given type")
    class WhenDeleteAll {

        @Test
        @DisplayName("Should delete all entities from the database for the given type")
        void shouldDeleteAllEntities() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            sqlTemplate.deleteAll(Computer.class);

            List<Computer> remaining =
                    sqlTemplate.findAll(Computer.class).toList();

            assertThat(remaining).isEmpty();
        }
    }

    @Nested
    @DisplayName("When counting entities stored in the database")
    class WhenCount {

        @Test
        @DisplayName("Should return the total number of persisted entities for the given type")
        void shouldCountEntitiesByClass() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            long count = sqlTemplate.count(Computer.class);

            assertThat(count).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should return the total number of persisted entities for the given String")
        void shouldCountEntitiesByString() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            long count = sqlTemplate.count("Computer");

            assertThat(count).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should throw NullPointerException when the entity name is null")
        void shouldThrowExceptionWhenEntityNameIsNull() {
            assertThatThrownBy(() -> sqlTemplate.count((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when the entity type is null")
        void shouldThrowExceptionWhenClassIsNull() {
            assertThatThrownBy(() -> sqlTemplate.count((Class<?>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("type is null");
        }

    }

    @Nested
    @DisplayName("When deleting an entity by identifier")
    class WhenDeleteById {

        @Test
        @DisplayName("Should delete the entity when it exists")
        void shouldDeleteEntityById() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            sqlTemplate.delete(Computer.class, computer.getId());

            Optional<Computer> found =
                    sqlTemplate.find(Computer.class, computer.getId());

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should not fail when deleting a non existing entity")
        void shouldNotFailWhenEntityDoesNotExist() {
            sqlTemplate.delete(Computer.class, 999L);

            List<Computer> computers =
                    sqlTemplate.findAll(Computer.class).toList();

            assertThat(computers).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            assertThatThrownBy(() -> sqlTemplate.delete(null, 1L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("type is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> sqlTemplate.delete(Computer.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("id is null");
        }
    }

}