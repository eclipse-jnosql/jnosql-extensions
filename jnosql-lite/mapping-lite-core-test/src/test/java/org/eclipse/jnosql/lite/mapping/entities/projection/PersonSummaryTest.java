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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.lite.mapping.entities.Person;
import org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionBuilder;
import org.eclipse.jnosql.mapping.metadata.ProjectionConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

class PersonSummaryTest {

    private final EntitiesMetadata metadata = new LiteEntitiesMetadata();


    @Test
    @DisplayName("Should return movie summary")
    void shouldReturnMovieSummary() {
        Optional<ProjectionMetadata> projection = metadata.projection(PersonSummary.class);
        Assertions.assertThat(projection).isPresent();
    }

    @Test
    @DisplayName("Should return class name")
    void shouldClassName() {
        ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
        Assertions.assertThat(projection.className()).isEqualTo(PersonSummary.class.getSimpleName());
    }

    @Test
    @DisplayName("Should return type")
    void shouldType() {
        ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
        Assertions.assertThat(projection.type()).isEqualTo(PersonSummary.class);
    }

    @Test
    @DisplayName("Should from")
    void shouldFrom() {
        ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
        Assertions.assertThat(projection.from()).isEqualTo(Person.class);
    }

    @Test
    void shouldConstructor() {
        ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
        ProjectionConstructorMetadata constructor = projection.constructor();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(constructor.parameters()).hasSize(3);
            var name = constructor.parameters().getFirst();
            var release = constructor.parameters().get(1);
            var price = constructor.parameters().get(2);

            soft.assertThat(name.name()).isEqualTo("final_name");
            soft.assertThat(name.type()).isEqualTo(String.class);

            soft.assertThat(release.name()).isEqualTo("birthday");
            soft.assertThat(release.type()).isEqualTo(LocalDate.class);

            soft.assertThat(price.name()).isEqualTo("salary");
            soft.assertThat(price.type()).isEqualTo(BigDecimal.class);
        });
    }

    @Test
    @DisplayName("Should create by constructor")
    void shouldCreate() {
        var projection = metadata.projection(PersonSummary.class).orElseThrow();
        var constructor = projection.constructor();
        ProjectionBuilder projectionBuilder = ProjectionBuilder.of(constructor);
        projectionBuilder.add("Otavio");
        projectionBuilder.add(LocalDate.now());
        projectionBuilder.add(BigDecimal.TEN);
        PersonSummary personSummary = projectionBuilder.build();
        Assertions.assertThat(personSummary).isNotNull();
    }
}