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

import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.repository.spi.FindByOperation;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.vector.DenseVector;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieRepositoryTest {

    @Mock
    private SemiStructuredTemplate template;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @Mock
    private LifecycleEventHandler lifecycleEventHandler;

    @InjectMocks
    private MovieRepositoryLiteVector repository;

    @Test
    void shouldGenerateVectorRepository() throws NoSuchMethodException {
        assertThat(repository.getClass().getSimpleName()).isEqualTo("MovieRepositoryLiteVector");
        Database database = repository.getClass().getAnnotation(Database.class);
        assertThat(database).isNotNull();
        assertThat(database.value()).isEqualTo(DatabaseType.VECTOR);
        Constructor<?> constructor = repository.getClass().getConstructors()[0];
        assertThat(constructor.getParameterTypes()[0]).isEqualTo(SemiStructuredTemplate.class);
        Database parameter = (Database) constructor.getParameterAnnotations()[0][0];
        assertThat(parameter.value()).isEqualTo(DatabaseType.VECTOR);
    }

    @Test
    void shouldSave() {
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
    void shouldFindById() {
        Movie entity = new Movie();
        when(template.find(Movie.class, "1")).thenReturn(Optional.of(entity));

        assertThat(repository.findById("1")).contains(entity);
    }

    @Test
    void shouldExecuteDerivedQuery() {
        FindByOperation operation = mock(FindByOperation.class);
        when(repositoryOperationProvider.findByOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(List.of(new Movie()));

        assertThat(repository.findByTitle("Matrix")).hasSize(1);
    }
}
