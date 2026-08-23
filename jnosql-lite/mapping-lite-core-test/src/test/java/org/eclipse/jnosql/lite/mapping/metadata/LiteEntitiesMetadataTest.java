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
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiteEntitiesMetadataTest {

    @Nested
    @DisplayName("When entity type metadata is requested")
    class WhenTheEntityTypeMetadataIsRequested {


        @Test
        @DisplayName("Should return metadata when entity class exists")
        void shouldReturnMetadataWhenEntityClassExists() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            EntityMetadata result =
                    metadata.get(org.eclipse.jnosql.lite.mapping.entities.Movie.class);

            assertThat(result).as("value of result")
                    .isNotNull()
                    .extracting(EntityMetadata::type)
                    .isEqualTo(org.eclipse.jnosql.lite.mapping.entities.Movie.class);
        }


        @Test
        @DisplayName("Should throw exception when entity class is not registered")
        void shouldThrowExceptionWhenEntityClassIsNotRegistered() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            assertThatThrownBy(() -> metadata.get(String.class)).as("invalid input rejection")
                    .isInstanceOf(ClassInformationNotFoundException.class)
                    .hasMessageContaining("was not found");
        }


        @Test
        @DisplayName("Should fail fast when entity class is null")
        void shouldFailFastWhenEntityClassIsNull() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            assertThatThrownBy(() -> metadata.get(null)).as("invalid input rejection")
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entity is required");
        }
    }

    @Nested
    @DisplayName("When entity metadata is looked up")
    class WhenTheEntityMetadataIsLookedUp {


        @ParameterizedTest(name = "Entity name lookup should work for: {0}")
        @DisplayName("Should find entity metadata by entity name ignoring case")
        @ValueSource(strings = {"movie", "MOVIE", "Movie"})
        void shouldFindEntityByNameIgnoringCase(String name) {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            EntityMetadata result = metadata.findByName(name);

            assertThat(result).as("value of result")
                    .isNotNull()
                    .extracting(EntityMetadata::simpleName)
                    .isEqualTo("Movie");
        }


        @Test
        @DisplayName("Should throw exception when entity name does not exist")
        void shouldThrowExceptionWhenEntityNameDoesNotExist() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            assertThatThrownBy(() -> metadata.findByName("does_not_exist")).as("invalid input rejection")
                    .isInstanceOf(ClassInformationNotFoundException.class)
                    .hasMessageContaining("There is not entity found");
        }


        @Test
        @DisplayName("Should fail fast when entity name is null")
        void shouldFailFastWhenEntityNameIsNull() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            assertThatThrownBy(() -> metadata.findByName(null)).as("invalid input rejection")
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("name is required");
        }


        @ParameterizedTest(name = "{0}")
        @DisplayName("Should find entity using different lookup strategies")
        @MethodSource("org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadataTest#lookupScenarios")
        void shouldFindEntityUsingLookupStrategies(
                String description,
                Function<EntitiesMetadata, Optional<EntityMetadata>> lookup) {

            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            Optional<EntityMetadata> result = lookup.apply(metadata);

            assertThat(result).as("value of result")
                    .isPresent()
                    .get()
                    .extracting(EntityMetadata::simpleName)
                    .isEqualTo("Movie");
        }


        @Test
        @DisplayName("Should return empty when lookup value does not exist")
        void shouldReturnEmptyWhenLookupDoesNotExist() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(metadata.findBySimpleName("Unknown"))
                        .as("simple-name lookup")
                        .isEmpty();
                soft.assertThat(metadata.findByClassName("Unknown"))
                        .as("class-name lookup")
                        .isEmpty();
                soft.assertThat(metadata.findByMappingName("Unknown"))
                        .as("mapping-name lookup")
                        .isEmpty();
            });
        }
    }

    @Nested
    @DisplayName("When inheritance metadata is requested")
    class WhenTheInheritanceMetadataIsRequested {


        @Test
        @DisplayName("Should group inheritance metadata by discriminator value")
        void shouldGroupInheritanceMetadataByDiscriminatorValue() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            Map<String, InheritanceMetadata> result =
                    metadata.findByParentGroupByDiscriminatorValue(
                            org.eclipse.jnosql.lite.mapping.entities.inheritance.Notification.class
                    );

            assertThat(result).as("value of result")
                    .isNotEmpty()
                    .allSatisfy((key, value) ->
                            assertThat(value.isParent(
                                    org.eclipse.jnosql.lite.mapping.entities.inheritance.Notification.class)).as("value of value.isParent( org.eclipse.jnosql.lite.mapping.entities.inheritance.Notification.class)")
                                    .isTrue()
                    );
        }


        @Test
        @DisplayName("Should fail fast when parent class is null")
        void shouldFailFastWhenParentClassIsNull() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            assertThatThrownBy(() -> metadata.findByParentGroupByDiscriminatorValue(null)).as("invalid input rejection")
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("parent is required");
        }
    }

    @Nested
    @DisplayName("When projection metadata is requested")
    class WhenTheProjectionMetadataIsRequested {


        @Test
        @DisplayName("Should return empty when projection is not registered")
        void shouldReturnEmptyWhenProjectionIsNotRegistered() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            Optional<ProjectionMetadata> result = metadata.projection(String.class);

            assertThat(result).as("value of result").isEmpty();
        }


        @Test
        @DisplayName("Should fail fast when projection class is null")
        void shouldFailFastWhenProjectionClassIsNull() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            assertThatThrownBy(() -> metadata.projection(null)).as("invalid input rejection")
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("projection is required");
        }


        @Test
        @DisplayName("Should get projection metadata")
        void shouldGetProjectionMetadata() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();
            Optional<ProjectionMetadata> result = metadata.projection(MovieSummary.class);
            assertThat(result).as("value of result").isPresent();
        }
    }

    @Nested
    @DisplayName("When the metadata registry is described")
    class WhenTheRegistryIsDescribed {


        @Test
        @DisplayName("Should include registered metadata in its text representation")
        void shouldIncludeInternalStructuresInTextRepresentation() {
            EntitiesMetadata metadata = new LiteEntitiesMetadata();

            String value = metadata.toString();

            assertThat(value).as("value of value")
                    .contains("entities=")
                    .contains("findByClassName=")
                    .contains("findBySimpleName=")
                    .contains("mappings=");
        }
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
}
