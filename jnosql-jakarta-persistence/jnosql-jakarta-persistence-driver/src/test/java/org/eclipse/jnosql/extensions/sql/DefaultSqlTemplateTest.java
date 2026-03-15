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

import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.extensions.sql.model.Computer;
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
                    DefaultSqlTemplateTest.class
            )
            .build();

    @Produces
    @ApplicationScoped
    public EntityManager createEntityManager() {
        return Persistence.createEntityManagerFactory("testPersistenceUnit")
                .createEntityManager();
    }


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

    @Nested
    @DisplayName("When verifying the existence of an entity by identifier")
    class WhenExistsById {

        @Test
        @DisplayName("Should return true when the entity exists")
        void shouldReturnTrueWhenEntityExists() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            boolean exists = sqlTemplate.existsById(Computer.class, computer.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when the entity does not exist")
        void shouldReturnFalseWhenEntityDoesNotExist() {
            boolean exists = sqlTemplate.existsById(Computer.class, 999L);

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should throw NullPointerException when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            assertThatThrownBy(() -> sqlTemplate.existsById(null, 1L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("type is required");
        }

        @Test
        @DisplayName("Should throw NullPointerException when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> sqlTemplate.existsById(Computer.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("id is required");
        }
    }

    @Nested
    @DisplayName("When executing queries using SqlTemplate query method")
    class WhenQuery {

        @Test
        @DisplayName("Should return results when executing a JPQL select query")
        void shouldReturnResultsFromQuery() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            List<Computer> result = sqlTemplate
                    .query("SELECT c FROM Computer c")
                    .result();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return a stream when executing a JPQL select query")
        void shouldReturnStreamFromQuery() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            List<Computer> result = sqlTemplate
                    .query("SELECT c FROM Computer c")
                    .<Computer>stream()
                    .toList();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return single result when query matches exactly one entity")
        void shouldReturnSingleResult() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            Optional<Computer> result = sqlTemplate
                    .query("SELECT c FROM Computer c WHERE c.id = :id")
                    .bind("id", computer.getId())
                    .singleResult();

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(computer.getId());
        }

        @Test
        @DisplayName("Should return empty when single result query returns no entity")
        void shouldReturnEmptyWhenSingleResultDoesNotExist() {
            Optional<Computer> result = sqlTemplate
                    .query("SELECT c FROM Computer c WHERE c.id = :id")
                    .bind("id", 999L)
                    .singleResult();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when query string is null")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> sqlTemplate.query(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("query is null");
        }
    }

    @Nested
    @DisplayName("When executing typed queries using SqlTemplate")
    class WhenTypedQuery {

        @Test
        @DisplayName("Should return results when executing a typed JPQL select query")
        void shouldReturnResultsFromTypedQuery() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            List<Computer> result = sqlTemplate
                    .typedQuery("SELECT c FROM Computer c", Computer.class)
                    .result();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return a stream when executing a typed JPQL select query")
        void shouldReturnStreamFromTypedQuery() {
            sqlTemplate.insert(Computer.of("MacBook", 2024));
            sqlTemplate.insert(Computer.of("ThinkPad", 2023));

            List<Computer> result = sqlTemplate
                    .typedQuery("SELECT c FROM Computer c", Computer.class)
                    .stream()
                    .toList();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return a single result when exactly one entity matches")
        void shouldReturnSingleResultFromTypedQuery() {
            Computer computer = sqlTemplate.insert(Computer.of("MacBook", 2024));

            Optional<Computer> result = sqlTemplate
                    .typedQuery("SELECT c FROM Computer c WHERE c.id = :id", Computer.class)
                    .bind("id", computer.getId())
                    .singleResult();

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(computer.getId());
        }

        @Test
        @DisplayName("Should return empty when the typed query matches no entity")
        void shouldReturnEmptyWhenTypedQueryReturnsNoResult() {
            Optional<Computer> result = sqlTemplate
                    .typedQuery("SELECT c FROM Computer c WHERE c.id = :id", Computer.class)
                    .bind("id", 999L)
                    .singleResult();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException when query string is null")
        void shouldThrowExceptionWhenQueryIsNull() {
            assertThatThrownBy(() -> sqlTemplate.typedQuery(null, Computer.class))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("query is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            assertThatThrownBy(() -> sqlTemplate.typedQuery("SELECT c FROM Computer c", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("type is null");
        }
    }

    @Nested
    @DisplayName("When executing SelectQuery aggregate and existence operations")
    class WhenSelectQueryAggregateOperations {

        @BeforeEach
        void insertData() {
            sqlTemplate.insert(Computer.of("MacBook Pro", 2021));
            sqlTemplate.insert(Computer.of("ThinkPad", 2020));
            sqlTemplate.insert(Computer.of("XPS", 2019));
        }

        @Test
        @DisplayName("Should return the number of entities matching the query")
        void shouldCountResultsFromQuery() {

            var select = SelectQuery.select()
                    .from("Computer")
                    .where("release")
                    .gte(2020)
                    .build();

            long count = sqlTemplate.count(select);

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return true when at least one entity matches the query")
        void shouldReturnTrueWhenQueryMatchesEntities() {

            var select = SelectQuery.select()
                    .from("Computer")
                    .where("release")
                    .eq(2021)
                    .build();

            boolean exists = sqlTemplate.exists(select);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when no entity matches the query")
        void shouldReturnFalseWhenQueryMatchesNoEntities() {

            var select = SelectQuery.select()
                    .from("Computer")
                    .where("release")
                    .eq(1990)
                    .build();

            boolean exists = sqlTemplate.exists(select);

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return the single matching entity when exactly one result exists")
        void shouldReturnSingleResult() {

            var select = SelectQuery.select()
                    .from("Computer")
                    .where("model")
                    .eq("ThinkPad")
                    .build();

            Optional<Computer> result = sqlTemplate.singleResult(select);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(result).isPresent();
                soft.assertThat(result.get().getModel()).isEqualTo("ThinkPad");
            });
        }

        @Test
        @DisplayName("Should return empty when the query matches no entities")
        void shouldReturnEmptyWhenNoResultExists() {

            var select = SelectQuery.select()
                    .from("Computer")
                    .where("model")
                    .eq("NonExisting")
                    .build();

            Optional<Computer> result = sqlTemplate.singleResult(select);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw NonUniqueResultException when more than one entity matches the query")
        void shouldThrowWhenMultipleResultsExist() {

            var select = SelectQuery.select()
                    .from("Computer")
                    .where("release")
                    .gte(2020)
                    .build();

            assertThatThrownBy(() -> sqlTemplate.singleResult(select))
                    .isInstanceOf(NonUniqueResultException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException when query is null")
        void shouldThrowExceptionWhenQueryIsNull() {

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThatThrownBy(() -> sqlTemplate.count((SelectQuery) null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessage("query is null");

                soft.assertThatThrownBy(() -> sqlTemplate.exists(null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessage("query is null");

                soft.assertThatThrownBy(() -> sqlTemplate.singleResult(null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessage("query is null");
            });
        }
    }
}