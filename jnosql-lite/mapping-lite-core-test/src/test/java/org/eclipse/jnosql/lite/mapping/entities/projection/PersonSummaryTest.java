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

import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;

class PersonSummaryTest {

    private final EntitiesMetadata metadata = new LiteEntitiesMetadata();

    @Nested
    @DisplayName("When projection metadata is inspected")
    class WhenTheProjectionMetadataIsInspected {


        @Test
        @DisplayName("Should expose projection metadata")
        void shouldExposeProjectionMetadata() {
            Optional<ProjectionMetadata> projection = metadata.projection(PersonSummary.class);
            assertThat(projection).as("value of projection").isPresent();
        }


        @Test
        @DisplayName("Should expose the projection class name")
        void shouldExposeProjectionClassName() {
            ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
            assertThat(projection.className()).as("value of projection.className()").isEqualTo(PersonSummary.class.getSimpleName());
        }


        @Test
        @DisplayName("Should expose the projection type")
        void shouldExposeProjectionType() {
            ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
            assertThat(projection.type()).as("value of projection.type()").isEqualTo(PersonSummary.class);
        }


        @Test
        @DisplayName("Should expose the projection source type")
        void shouldExposeProjectionSourceType() {
            ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
            assertThat(projection.from()).as("value of projection.from()").isEqualTo(Person.class);
        }


        @Test
        @DisplayName("Should describe the projection constructor")
        void shouldDescribeProjectionConstructor() {
            ProjectionMetadata projection = metadata.projection(PersonSummary.class).orElseThrow();
            ProjectionConstructorMetadata constructor = projection.constructor();

            SoftAssertions.assertSoftly(soft -> {
                soft.assertThat(constructor.parameters()).as("value of constructor.parameters()").hasSize(3);
                var name = constructor.parameters().getFirst();
                var release = constructor.parameters().get(1);
                var price = constructor.parameters().get(2);

                soft.assertThat(name.name()).as("value of name.name()").isEqualTo("final_name");
                soft.assertThat(name.type()).as("value of name.type()").isEqualTo(String.class);

                soft.assertThat(release.name()).as("value of release.name()").isEqualTo("birthday");
                soft.assertThat(release.type()).as("value of release.type()").isEqualTo(LocalDate.class);

                soft.assertThat(price.name()).as("value of price.name()").isEqualTo("salary");
                soft.assertThat(price.type()).as("value of price.type()").isEqualTo(BigDecimal.class);
            });
        }


        @Test
        @DisplayName("Should build the projection from mapped values")
        void shouldBuildProjectionFromMappedValues() {
            var projection = metadata.projection(PersonSummary.class).orElseThrow();
            var constructor = projection.constructor();
            ProjectionBuilder projectionBuilder = ProjectionBuilder.of(constructor);
            projectionBuilder.add("Otavio");
            projectionBuilder.add(LocalDate.now());
            projectionBuilder.add(BigDecimal.TEN);
            PersonSummary personSummary = projectionBuilder.build();
            assertThat(personSummary).as("value of personSummary").isNotNull();
        }
    }
}
