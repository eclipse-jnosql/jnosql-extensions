/*
 *  Copyright (c) 2020 Otávio Santana and others
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


import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        mappings = new LiteEntitiesMetadata();
        entityMetadata = mappings.get(Movie.class);
    }

    @Nested
    @DisplayName("When the movie metadata is inspected")
    class WhenTheMovieMetadataIsInspected {


        @Test
        @DisplayName("Should expose the mapping name")
        void shouldExposeMappingName() {
            assertThat(entityMetadata.name()).as("value of entityMetadata.name()").isEqualTo("Movie");
        }


        @Test
        @DisplayName("Should expose the entity simple name")
        void shouldExposeEntitySimpleName() {
            assertThat(entityMetadata.simpleName()).as("value of entityMetadata.simpleName()").isEqualTo(Movie.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the entity class name")
        void shouldExposeEntityClassName() {
            assertThat(entityMetadata.className()).as("value of entityMetadata.className()").isEqualTo(Movie.class.getName());
        }


        @Test
        @DisplayName("Should expose the entity type")
        void shouldExposeEntityType() {
            assertThat(entityMetadata.type()).as("value of entityMetadata.type()").isEqualTo(Movie.class);
        }


        @Test
        @DisplayName("Should expose identifier metadata")
        void shouldExposeIdentifierMetadata() {
            Optional<FieldMetadata> id = entityMetadata.id();
            assertThat(id.isPresent()).as("value of id.isPresent()").isFalse();
        }


        @Test
        @DisplayName("Should create a new domain instance")
        void shouldCreateNewInstance() {
            Movie movie = entityMetadata.newInstance();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(movie).as("value of movie").isNotNull();
                soft.assertThat(movie).as("value of movie").isInstanceOf(Movie.class);
            });
        }


        @Test
        @DisplayName("Should expose every mapped field name")
        void shouldExposeMappedFieldNames() {
            List<String> fields = entityMetadata.fieldsName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fields.size()).as("value of fields.size()").isEqualTo(2);
                soft.assertThat(fields.contains("title")).as("value of fields.contains(\"title\")").isTrue();
                soft.assertThat(fields.contains("director")).as("value of fields.contains(\"director\")").isTrue();
            });
        }


        @Test
        @DisplayName("Should index mapped fields by name")
        void shouldIndexMappedFieldsByName() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(groupByName).as("value of groupByName").isNotNull();
                soft.assertThat(groupByName.get("title")).as("value of groupByName.get(\"title\")").isNotNull();
                soft.assertThat(groupByName.get("director")).as("value of groupByName.get(\"director\")").isNotNull();
            });
        }


        @Test
        @DisplayName("Should read mapped field values")
        void shouldReadMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Movie movie = new Movie();
            movie.setTitle("Movie");

            Director sample = new Director();
            sample.setName("Director name");
            movie.setDirector(sample);

            String title = entityMetadata.columnField("title");
            String director = entityMetadata.columnField("director");
            FieldMetadata fieldTitle = groupByName.get(title);
            FieldMetadata fieldDirector = groupByName.get(director);

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldDirector.read(movie)).as("value of fieldDirector.read(movie)").isEqualTo(sample);
                soft.assertThat(fieldTitle.read(movie)).as("value of fieldTitle.read(movie)").isEqualTo("Movie");
            });
        }


        @Test
        @DisplayName("Should write mapped field values")
        void shouldWriteMappedFieldValues() {
            Map<String, FieldMetadata> groupByName = entityMetadata.fieldsGroupByName();
            Movie movie = new Movie();

            Director sample = new Director();
            movie.setDirector(sample);

            String title = entityMetadata.columnField("title");
            String director = entityMetadata.columnField("director");
            FieldMetadata fieldTitle = groupByName.get(title);
            FieldMetadata fieldDirector = groupByName.get(director);

            fieldTitle.write(movie, "Movie");
            fieldDirector.write(movie, sample);
            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(fieldDirector.read(movie)).as("value of fieldDirector.read(movie)").isEqualTo(sample);
                soft.assertThat(fieldTitle.read(movie)).as("value of fieldTitle.read(movie)").isEqualTo("Movie");
            });

        }
    }
}
