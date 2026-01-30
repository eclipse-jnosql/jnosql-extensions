/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities.projection;

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MovieSummaryTest {

    private EntitiesMetadata metadata = new LiteEntitiesMetadata();


    @Test
    @DisplayName("Should return movie summary")
    void shouldReturnMovieSummary() {
        Optional<ProjectionMetadata> projection = metadata.projection(MovieSummary.class);
        Assertions.assertThat(projection).isPresent();
    }

    @Test
    @DisplayName("Should return class name")
    void shouldClassName() {
        ProjectionMetadata projection = metadata.projection(MovieSummary.class).orElseThrow();
        Assertions.assertThat(projection.className()).isEqualTo(MovieSummary.class.getSimpleName());
    }

    @Test
    @DisplayName("Should return type")
    void shouldType() {
        ProjectionMetadata projection = metadata.projection(MovieSummary.class).orElseThrow();
        Assertions.assertThat(projection.type()).isEqualTo(MovieSummary.class);
    }

    @Test
    @DisplayName("Should from")
    void shouldFrom() {
        ProjectionMetadata projection = metadata.projection(MovieSummary.class).orElseThrow();
        Assertions.assertThat(projection.from()).isEqualTo(void.class);
    }

}