/*
 *  Copyright (c) 2025 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.lite.mapping.entities.projection.MovieSummary;
import org.eclipse.jnosql.mapping.metadata.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class LiteEntitiesMetadataTest {

    @Test
    @DisplayName("Should return metadata when entity class exists")
    void shouldReturnMetadataWhenEntityClassExists() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        EntityMetadata result =
                metadata.get(org.eclipse.jnosql.lite.mapping.entities.Movie.class);

        assertThat(result)
                .isNotNull()
                .extracting(EntityMetadata::type)
                .isEqualTo(org.eclipse.jnosql.lite.mapping.entities.Movie.class);
    }

    @Test
    @DisplayName("Should throw exception when entity class is not registered")
    void shouldThrowExceptionWhenEntityClassIsNotRegistered() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThatThrownBy(() -> metadata.get(String.class))
                .isInstanceOf(ClassInformationNotFoundException.class)
                .hasMessageContaining("was not found");
    }

    @Test
    @DisplayName("Should fail fast when entity class is null")
    void shouldFailFastWhenEntityClassIsNull() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThatThrownBy(() -> metadata.get(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("entity is required");
    }

    @ParameterizedTest(name = "Entity name lookup should work for: {0}")
    @DisplayName("Should find entity metadata by entity name ignoring case")
    @ValueSource(strings = {"movie", "MOVIE", "Movie"})
    void shouldFindEntityByNameIgnoringCase(String name) {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        EntityMetadata result = metadata.findByName(name);

        assertThat(result)
                .isNotNull()
                .extracting(EntityMetadata::simpleName)
                .isEqualTo("Movie");
    }

    @Test
    @DisplayName("Should throw exception when entity name does not exist")
    void shouldThrowExceptionWhenEntityNameDoesNotExist() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThatThrownBy(() -> metadata.findByName("does_not_exist"))
                .isInstanceOf(ClassInformationNotFoundException.class)
                .hasMessageContaining("There is not entity found");
    }

    @Test
    @DisplayName("Should fail fast when entity name is null")
    void shouldFailFastWhenEntityNameIsNull() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThatThrownBy(() -> metadata.findByName(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("name is required");
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Should find entity using different lookup strategies")
    @MethodSource("lookupScenarios")
    void shouldFindEntityUsingLookupStrategies(
            String description,
            Function<EntitiesMetadata, Optional<EntityMetadata>> lookup) {

        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        Optional<EntityMetadata> result = lookup.apply(metadata);

        assertThat(result)
                .isPresent()
                .get()
                .extracting(EntityMetadata::simpleName)
                .isEqualTo("Movie");
    }

    static Stream<Arguments> lookupScenarios() {
        return Stream.of(
                Arguments.of(
                        "Find by simple name",
                        (Function<EntitiesMetadata, Optional<EntityMetadata>>)
                                m -> m.findBySimpleName("Movie")
                ),
                Arguments.of(
                        "Find by class name",
                        (Function<EntitiesMetadata, Optional<EntityMetadata>>)
                                m -> m.findByClassName(
                                        "org.eclipse.jnosql.lite.mapping.entities.Movie")
                ),
                Arguments.of(
                        "Find by mapping name",
                        (Function<EntitiesMetadata, Optional<EntityMetadata>>)
                                m -> m.findByMappingName("Movie")
                )
        );
    }

    @Test
    @DisplayName("Should return empty when lookup value does not exist")
    void shouldReturnEmptyWhenLookupDoesNotExist() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThat(metadata.findBySimpleName("Unknown")).isEmpty();
        assertThat(metadata.findByClassName("Unknown")).isEmpty();
        assertThat(metadata.findByMappingName("Unknown")).isEmpty();
    }

    @Test
    @DisplayName("Should group inheritance metadata by discriminator value")
    void shouldGroupInheritanceMetadataByDiscriminatorValue() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        Map<String, InheritanceMetadata> result =
                metadata.findByParentGroupByDiscriminatorValue(
                        org.eclipse.jnosql.lite.mapping.entities.inheritance.Notification.class
                );

        assertThat(result)
                .isNotEmpty()
                .allSatisfy((key, value) ->
                        assertThat(value.isParent(
                                org.eclipse.jnosql.lite.mapping.entities.inheritance.Notification.class))
                                .isTrue()
                );
    }

    @Test
    @DisplayName("Should fail fast when parent class is null")
    void shouldFailFastWhenParentClassIsNull() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThatThrownBy(() -> metadata.findByParentGroupByDiscriminatorValue(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("parent is required");
    }

    @Test
    @DisplayName("Should return empty when projection is not registered")
    void shouldReturnEmptyWhenProjectionIsNotRegistered() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        Optional<ProjectionMetadata> result = metadata.projection(String.class);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should fail fast when projection class is null")
    void shouldFailFastWhenProjectionClassIsNull() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        assertThatThrownBy(() -> metadata.projection(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("projection is required");
    }

    @Test
    @DisplayName("toString should include main internal structures")
    void toStringShouldIncludeMainInternalStructures() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();

        String value = metadata.toString();

        assertThat(value)
                .contains("entities=")
                .contains("findByClassName=")
                .contains("findBySimpleName=")
                .contains("mappings=");
    }

    @Test
    @DisplayName("Should get projection metadata")
    void shouldGetProjectionMetadata() {
        EntitiesMetadata metadata = new LiteEntitiesMetadata();
        Optional<ProjectionMetadata> result = metadata.projection(MovieSummary.class);
        assertThat(result).isPresent();
    }
}
