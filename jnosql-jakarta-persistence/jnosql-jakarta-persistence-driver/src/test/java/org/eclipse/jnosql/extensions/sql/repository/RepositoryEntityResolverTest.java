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

import jakarta.data.repository.Insert;
import jakarta.data.repository.Update;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepositoryEntityResolverTest {
    interface BaseBasicRepository extends jakarta.data.repository.BasicRepository<Computer, Long> {}

    interface BaseCrudRepository extends jakarta.data.repository.CrudRepository<Computer, Long> {}

    interface BaseDataRepository extends jakarta.data.repository.DataRepository<Computer, Long> {}

    interface NoSqlRepository extends org.eclipse.jnosql.mapping.NoSQLRepository<Computer, Long> {}

    interface Level1 extends BaseBasicRepository {}
    interface Level2 extends Level1 {}
    interface Level3 extends Level2 {}
    interface MultiRepository extends BaseCrudRepository, BaseDataRepository {}
    interface InvalidRepository {}


    @Nested
    @DisplayName("WhenResolvingEntityType")
    class WhenResolvingEntityType {

        @Test
        @DisplayName("Should resolve entity from BasicRepository")
        void shouldResolveFromBasicRepository() {

            var result = RepositoryEntityResolver.INSTANCE.resolveEntityType(BaseBasicRepository.class);

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(result).isEqualTo(Computer.class)
            );
        }

        @Test
        @DisplayName("Should resolve entity from CrudRepository")
        void shouldResolveFromCrudRepository() {

            var result = RepositoryEntityResolver.INSTANCE.resolveEntityType(BaseCrudRepository.class);

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(result).isEqualTo(Computer.class)
            );
        }

        @Test
        @DisplayName("Should resolve entity from DataRepository")
        void shouldResolveFromDataRepository() {

            var result = RepositoryEntityResolver.INSTANCE.resolveEntityType(BaseDataRepository.class);

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(result).isEqualTo(Computer.class)
            );
        }

        @Test
        @DisplayName("Should resolve entity from NoSQLRepository")
        void shouldResolveFromNoSqlRepository() {

            var result = RepositoryEntityResolver.INSTANCE.resolveEntityType(NoSqlRepository.class);

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(result).isEqualTo(Computer.class)
            );
        }

        @Test
        @DisplayName("Should resolve entity from deep inheritance chain")
        void shouldResolveFromDeepInheritance() {

            var result = RepositoryEntityResolver.INSTANCE.resolveEntityType(Level3.class);

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(result).isEqualTo(Computer.class)
            );
        }

        @Test
        @DisplayName("Should resolve entity from multiple repository inheritance")
        void shouldResolveFromMultipleInheritance() {

            var result = RepositoryEntityResolver.INSTANCE.resolveEntityType(MultiRepository.class);

            SoftAssertions.assertSoftly(softly ->
                    softly.assertThat(result).isEqualTo(Computer.class)
            );
        }

        @Test
        @DisplayName("Should throw exception when repository class is null")
        void shouldThrowWhenRepositoryIsNull() {

            assertThatThrownBy(() -> RepositoryEntityResolver.INSTANCE.resolveEntityType(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("repositoryClass is required");
        }

        @Test
        @DisplayName("Should throw exception when entity cannot be resolved")
        void shouldThrowWhenEntityCannotBeResolved() {

            assertThatThrownBy(() -> RepositoryEntityResolver.INSTANCE.resolveEntityType(InvalidRepository.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot resolve entity type");
        }
    }

    @Nested
    @DisplayName("When resolve entity from custom repository")
    class WhenResolveEntityFromCustomRepository {


        interface CustomRepository {
            Computer findComputerById(Long id);
        }

        interface CustomInsertRepository {
            @Insert
            Computer[] insert(Long id);
        }

        interface CustomUpdateRepository {
            @Update
            List<Computer> update(Long id);
        }
    }
}