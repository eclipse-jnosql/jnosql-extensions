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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MovieTest {

    private EntitiesMetadata mappings;

    private EntityMetadata entityMetadata;

    @BeforeEach
    public void setUp() {
        this.mappings = new LiteEntitiesMetadata();
        this.entityMetadata = this.mappings.get(Movie.class);
    }

    @Test
    void shouldGetName() {
        Assertions.assertEquals("Movie", entityMetadata.name());
    }

    @Test
    void shouldGetSimpleName() {
        Assertions.assertEquals(Movie.class.getSimpleName(), entityMetadata.simpleName());
    }

    @Test
    void shouldGetClassName() {
        Assertions.assertEquals(Movie.class.getName(), entityMetadata.className());
    }

    @Test
    void shouldGetClassInstance() {
        Assertions.assertEquals(Movie.class, entityMetadata.type());
    }

    @Test
    void shouldGetId() {
        Optional<FieldMetadata> id = this.entityMetadata.id();
        Assertions.assertFalse(id.isPresent());
    }

    @Test
    void shouldCreateNewInstance() {
        Movie movie = entityMetadata.newInstance();
        Assertions.assertNotNull(movie);
        Assertions.assertInstanceOf(Movie.class, movie);
    }

    @Test
    void shouldGetFieldsName() {
        List<String> fields = entityMetadata.fieldsName();
        Assertions.assertEquals(2, fields.size());
        Assertions.assertTrue(fields.contains("title"));
        Assertions.assertTrue(fields.contains("director"));
    }

    @Test
    void shouldGetFieldsGroupByName() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Assertions.assertNotNull(groupByName);
        Assertions.assertNotNull(groupByName.get("title"));
        Assertions.assertNotNull(groupByName.get("director"));
    }

    @Test
    void shouldGetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Movie movie = new Movie();
        movie.setTitle("Movie");

        Director sample = new Director();
        sample.setName("Director name");
        movie.setDirector(sample);

        String title = this.entityMetadata.columnField("title");
        String director = this.entityMetadata.columnField("director");
        FieldMetadata fieldTitle = groupByName.get(title);
        FieldMetadata fieldDirector = groupByName.get(director);

        Assertions.assertEquals(sample, fieldDirector.read(movie));
        Assertions.assertEquals("Movie", fieldTitle.read(movie));
    }

    @Test
    void shouldSetter() {
        Map<String, FieldMetadata> groupByName = this.entityMetadata.fieldsGroupByName();
        Movie movie = new Movie();

        Director sample = new Director();
        movie.setDirector(sample);

        String title = this.entityMetadata.columnField("title");
        String director = this.entityMetadata.columnField("director");
        FieldMetadata fieldTitle = groupByName.get(title);
        FieldMetadata fieldDirector = groupByName.get(director);

        fieldTitle.write(movie, "Movie");
        fieldDirector.write(movie, sample);
        Assertions.assertEquals(sample, fieldDirector.read(movie));
        Assertions.assertEquals("Movie", fieldTitle.read(movie));

    }


}
