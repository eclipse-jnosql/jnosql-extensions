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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableWeld
class NoSQLRepositorySqlAdapterTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    SqlTemplateFactory.class,
                    NoSQLRepositorySqlAdapterTest.class
            )
            .build();

    @Inject
    private SqlTemplate template;

    private NoSQLRepository<Computer, Long> repository;

    @Produces
    @ApplicationScoped
    public SqlTemplate createEntityManager() {
        EntityManagerFactory persistenceUnit = Persistence.createEntityManagerFactory("testPersistenceUnit");
        var entityManager = persistenceUnit.createEntityManager();
        var sqlTemplateFactory = new SqlTemplateFactory();
        return sqlTemplateFactory.create(entityManager);
    }

    @BeforeEach
    void setUp() {
        repository = new NoSQLRepositorySqlAdapter<> (Computer.class, template);
    }

    @Nested
    @DisplayName("WhenFindAllOperations")
    class WhenFindAllOperations {

        @BeforeEach
        void clean() {
            repository.deleteAll();
        }

        @Test
        @DisplayName("Should return all persisted entities")
        void shouldReturnAllPersistedEntities() {

            // given
            var computer1 = Computer.of("MacBook Pro", 2023);
            var computer2 = Computer.of("ThinkPad", 2022);

            repository.insert(computer1);
            repository.insert(computer2);

            // when
            var result = repository.findAll().toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsExactlyInAnyOrder("MacBook Pro", "ThinkPad");
            });
        }

        @Test
        @DisplayName("Should return paged result with sorting using Jakarta Data API")
        void shouldReturnPagedResultWithSorting() {

            // given
            repository.insert(Computer.of("MacBook Pro", 2023));
            repository.insert(Computer.of("ThinkPad", 2022));
            repository.insert(Computer.of("Dell XPS", 2021));

            var pageRequest = PageRequest.ofPage(1, 2, true);
            Order<Computer> sort = Order.by(Sort.asc("model"));

            // when
            var page = repository.findAll(pageRequest, sort);
            var content = page.content();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(content).hasSize(2);
                softly.assertThat(content)
                        .extracting(Computer::getModel)
                        .containsExactly("Dell XPS", "MacBook Pro");
                softly.assertThat(page.hasNext()).isTrue();
            });
        }

        @Test
        @DisplayName("Should throw exception when pageRequest is null")
        void shouldThrowExceptionWhenPageRequestIsNull() {

            Order<Computer> sort = Order.by(Sort.asc("model"));

            assertThatThrownBy(() -> repository.findAll(null, sort))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("pageRequest is required");
        }

        @Test
        @DisplayName("Should throw exception when sortBy is null")
        void shouldThrowExceptionWhenSortIsNull() {

            var pageRequest = PageRequest.ofPage(1, 2, true);

            assertThatThrownBy(() -> repository.findAll(pageRequest, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("sortBy is required");
        }
    }

    @Nested
    @DisplayName("WhenExistsAndFindByIdIn")
    class WhenExistsAndFindByIdIn {

        @BeforeEach
        void clean() {
            repository.deleteAll();
        }

        @Test
        @DisplayName("Should return true when entity exists by id")
        void shouldReturnTrueWhenEntityExists() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            var exists = repository.existsById(computer.getId());

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(exists).isTrue();
            });
        }

        @Test
        @DisplayName("Should return false when entity does not exist by id")
        void shouldReturnFalseWhenEntityDoesNotExist() {

            // when
            var exists = repository.existsById(999L);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(exists).isFalse();
            });
        }

        @Test
        @DisplayName("Should throw exception when id is null in existsById")
        void shouldThrowExceptionWhenIdIsNull() {

            assertThatThrownBy(() -> repository.existsById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("id is required");
        }

        @Test
        @DisplayName("Should return entities when ids are provided")
        void shouldReturnEntitiesWhenIdsProvided() {

            // given
            var computer1 = repository.insert(Computer.of("MacBook Pro", 2023));
            var computer2 = repository.insert(Computer.of("ThinkPad", 2022));

            var ids = List.of(computer1.getId(), computer2.getId());

            // when
            var result = repository.findByIdIn(ids).toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getId)
                        .containsExactlyInAnyOrder(computer1.getId(), computer2.getId());
            });
        }

        @Test
        @DisplayName("Should return empty when ids iterable is empty")
        void shouldReturnEmptyWhenIdsIsEmpty() {

            // when
            var result = repository.findByIdIn(List.<Long>of()).toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should throw exception when ids is null")
        void shouldThrowExceptionWhenIdsIsNull() {

            assertThatThrownBy(() -> repository.findByIdIn(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ids is required");
        }
    }

    @Nested
    @DisplayName("WhenExistsByIdAndFindByIdIn")
    class WhenExistsByIdAndFindByIdIn {

        @BeforeEach
        void clean() {
            repository.deleteAll();
        }

        @Test
        @DisplayName("Should return true when entity exists by id")
        void shouldReturnTrueWhenEntityExists() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            var exists = repository.existsById(computer.getId());

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(exists).isTrue();
            });
        }

        @Test
        @DisplayName("Should return false when entity does not exist by id")
        void shouldReturnFalseWhenEntityDoesNotExist() {

            // when
            var exists = repository.existsById(999L);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(exists).isFalse();
            });
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {

            assertThatThrownBy(() -> repository.existsById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("id is required");
        }

        @Test
        @DisplayName("Should return entities when ids are provided")
        void shouldReturnEntitiesWhenIdsProvided() {

            // given
            var computer1 = repository.insert(Computer.of("MacBook Pro", 2023));
            var computer2 = repository.insert(Computer.of("ThinkPad", 2022));

            var ids = List.of(computer1.getId(), computer2.getId());

            // when
            var result = repository.findByIdIn(ids).toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getId)
                        .containsExactlyInAnyOrder(computer1.getId(), computer2.getId());
            });
        }

        @Test
        @DisplayName("Should return empty when id iterable is empty")
        void shouldReturnEmptyWhenIdIsEmpty() {

            // given

            var id = Long.MAX_VALUE;

            // when
            var result = repository.findById(id);

            // then
           Assertions.assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return entity when id is provided")
        void shouldReturnEntityWhenIdProvided() {

            // given
            var computer1 = repository.insert(Computer.of("MacBook Pro", 2023));

            var id = computer1.getId();

            // when
            var result = repository.findById(id);

            // then
            Assertions.assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return empty when ids iterable is empty")
        void shouldReturnEmptyWhenIdsIsEmpty() {

            // when
            var result = repository.findByIdIn(List.<Long>of()).toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should throw exception when ids is null")
        void shouldThrowExceptionWhenIdsIsNull() {

            assertThatThrownBy(() -> repository.findByIdIn(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ids is required");
        }
    }

    @Nested
    @DisplayName("WhenDeleteOperations")
    class WhenDeleteOperations {

        @BeforeEach
        void clean() {
            repository.deleteAll();
        }

        @Test
        @DisplayName("Should delete entities by ids")
        void shouldDeleteByIdIn() {

            // given
            var computer1 = repository.insert(Computer.of("MacBook Pro", 2023));
            var computer2 = repository.insert(Computer.of("ThinkPad", 2022));

            var ids = List.of(computer1.getId(), computer2.getId());

            // when
            repository.deleteByIdIn(ids);
            var result = repository.findAll().toList();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should do nothing when deleteByIdIn receives empty list")
        void shouldDoNothingWhenDeleteByIdInIsEmpty() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            repository.deleteByIdIn(List.of());

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getId()).isEqualTo(computer.getId());
            });
        }

        @Test
        @DisplayName("Should throw exception when ids is null in deleteByIdIn")
        void shouldThrowExceptionWhenDeleteByIdInIdsIsNull() {

            assertThatThrownBy(() -> repository.deleteByIdIn(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ids is required");
        }

        @Test
        @DisplayName("Should delete all entities")
        void shouldDeleteAll() {

            // given
            repository.insert(Computer.of("MacBook Pro", 2023));
            repository.insert(Computer.of("ThinkPad", 2022));

            // when
            repository.deleteAll();

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should delete entity by id")
        void shouldDeleteById() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            repository.deleteById(computer.getId());

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should throw exception when id is null in deleteById")
        void shouldThrowExceptionWhenDeleteByIdIsNull() {

            assertThatThrownBy(() -> repository.deleteById(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("id is required");
        }

        @Test
        @DisplayName("Should delete entity by instance")
        void shouldDeleteEntity() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            repository.delete(computer);

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should throw exception when entity is null")
        void shouldThrowExceptionWhenEntityIsNull() {

            assertThatThrownBy(() -> repository.delete(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is required");
        }

        @Test
        @DisplayName("Should delete all provided entities")
        void shouldDeleteAllEntitiesFromList() {

            // given
            var computer1 = repository.insert(Computer.of("MacBook Pro", 2023));
            var computer2 = repository.insert(Computer.of("ThinkPad", 2022));

            var entities = List.of(computer1, computer2);

            // when
            repository.deleteAll(entities);

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }

        @Test
        @DisplayName("Should do nothing when deleteAll receives empty list")
        void shouldDoNothingWhenDeleteAllListIsEmpty() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            repository.deleteAll(List.of());

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).hasSize(1);
                softly.assertThat(result.get(0).getId()).isEqualTo(computer.getId());
            });
        }

        @Test
        @DisplayName("Should throw exception when deleteAll list is null")
        void shouldThrowExceptionWhenDeleteAllListIsNull() {

            assertThatThrownBy(() -> repository.deleteAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entities are required");
        }
    }

    @Nested
    @DisplayName("WhenUpdateOperations")
    class WhenUpdateOperations {

        @BeforeEach
        void clean() {
            repository.deleteAll();
        }

        @Test
        @DisplayName("Should update existing entity")
        void shouldUpdateEntity() {

            // given
            var computer = repository.insert(Computer.of("MacBook Pro", 2023));

            // when
            computer.setModel("MacBook Pro M3");
            var updated = repository.update(computer);

            // then
            var result = repository.findById(updated.getId()).orElseThrow();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(updated.getId()).isEqualTo(computer.getId());
                softly.assertThat(result.getModel()).isEqualTo("MacBook Pro M3");
            });
        }

        @Test
        @DisplayName("Should throw exception when entity is null in update")
        void shouldThrowExceptionWhenUpdateEntityIsNull() {

            assertThatThrownBy(() -> repository.update(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is required");
        }

        @Test
        @DisplayName("Should update all entities")
        void shouldUpdateAllEntities() {

            // given
            var computer1 = repository.insert(Computer.of("MacBook Pro", 2023));
            var computer2 = repository.insert(Computer.of("ThinkPad", 2022));

            computer1.setModel("MacBook Pro M3");
            computer2.setModel("ThinkPad X1");

            var entities = List.of(computer1, computer2);

            // when
            var updatedList = repository.updateAll(entities);

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(updatedList).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsExactlyInAnyOrder("MacBook Pro M3", "ThinkPad X1");
            });
        }

        @Test
        @DisplayName("Should throw exception when entities list is null in updateAll")
        void shouldThrowExceptionWhenUpdateAllEntitiesIsNull() {

            assertThatThrownBy(() -> repository.updateAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entities are required");
        }

        @Test
        @DisplayName("Should return empty list when updateAll receives empty list")
        void shouldReturnEmptyWhenUpdateAllIsEmpty() {

            // when
            var result = repository.updateAll(List.of());

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }
    }

    @Nested
    @DisplayName("WhenInsertOperations")
    class WhenInsertOperations {

        @BeforeEach
        void clean() {
            repository.deleteAll();
        }

        @Test
        @DisplayName("Should insert entity successfully")
        void shouldInsertEntity() {

            // given
            var computer = Computer.of("MacBook Pro", 2023);

            // when
            var inserted = repository.insert(computer);

            // then
            var result = repository.findById(inserted.getId()).orElseThrow();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(inserted.getId()).isNotNull();
                softly.assertThat(result.getModel()).isEqualTo("MacBook Pro");
            });
        }

        @Test
        @DisplayName("Should throw exception when entity is null in insert")
        void shouldThrowExceptionWhenInsertEntityIsNull() {

            assertThatThrownBy(() -> repository.insert(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is required");
        }

        @Test
        @DisplayName("Should insert all entities")
        void shouldInsertAllEntities() {

            // given
            var computer1 = Computer.of("MacBook Pro", 2023);
            var computer2 = Computer.of("ThinkPad", 2022);

            var entities = List.of(computer1, computer2);

            // when
            var insertedList = repository.insertAll(entities);

            // then
            var result = repository.findAll().toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(insertedList).hasSize(2);
                softly.assertThat(result)
                        .extracting(Computer::getModel)
                        .containsExactlyInAnyOrder("MacBook Pro", "ThinkPad");
            });
        }

        @Test
        @DisplayName("Should throw exception when entities list is null in insertAll")
        void shouldThrowExceptionWhenInsertAllIsNull() {

            assertThatThrownBy(() -> repository.insertAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entities are required");
        }

        @Test
        @DisplayName("Should return empty list when insertAll receives empty list")
        void shouldReturnEmptyWhenInsertAllIsEmpty() {

            // when
            var result = repository.insertAll(List.of());

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isEmpty();
            });
        }
    }
}