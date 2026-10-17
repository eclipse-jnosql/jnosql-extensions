/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.entities;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.vector.DenseVector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieRepositoryTest {

    @Mock
    private SemiStructuredTemplate template;

    @InjectMocks
    private MovieRepositoryLiteVector repository;

    @Test
    void shouldGenerateVectorRepository() {
        Class<?> type = MovieRepositoryLiteVector.class;
        Database database = type.getAnnotation(Database.class);
        assertThat(database).isNotNull();
        assertThat(database.value()).isEqualTo(DatabaseType.VECTOR);

        Constructor<?> constructor = type.getConstructors()[0];
        assertThat(constructor.getParameterTypes()[0]).isEqualTo(SemiStructuredTemplate.class);
        Database qualifier = (Database) constructor.getParameterAnnotations()[0][0];
        assertThat(qualifier.value()).isEqualTo(DatabaseType.VECTOR);
    }

    @Test
    void shouldSaveMovie() {
        Movie entity = new Movie();
        DenseVector embedding = DenseVector.of(0.1f, 0.2f, 0.3f);
        entity.setEmbedding(embedding);
        when(template.insert(entity)).thenReturn(entity);

        Movie result = repository.save(entity);

        assertThat(result).isSameAs(entity);
        assertThat(result.getEmbedding()).isInstanceOf(DenseVector.class);
        verify(template).insert(entity);
    }

    @Test
    void shouldFindMovieById() {
        Movie entity = new Movie();
        when(template.find(Movie.class, "1")).thenReturn(Optional.of(entity));

        Optional<Movie> result = repository.findById("1");

        assertThat(result).contains(entity);
        verify(template).find(Movie.class, "1");
    }

    @Test
    void shouldFindMovieByTitle() {
        when(template.select(any(SelectQuery.class))).thenReturn(Stream.of(new Movie()));

        List<Movie> result = repository.findByTitle("Matrix");

        assertThat(result).hasSize(1);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        CriteriaCondition condition = captor.getValue().condition().orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get(String.class)).isEqualTo("Matrix");
        });
    }
}
