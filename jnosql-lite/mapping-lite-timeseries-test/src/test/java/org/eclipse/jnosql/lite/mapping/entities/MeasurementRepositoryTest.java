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
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurementRepositoryTest {

    @Mock
    private TimeSeriesTemplate template;

    @InjectMocks
    private MeasurementRepositoryLiteTimeSeries repository;

    @Test
    void shouldSaveMeasurement() {
        Measurement measurement = new Measurement();
        when(template.insert(measurement)).thenReturn(measurement);

        Measurement result = repository.save(measurement);

        assertThat(result).isSameAs(measurement);
        verify(template).insert(measurement);
    }

    @Test
    void shouldFindMeasurementById() {
        Instant id = Instant.parse("2026-09-10T13:00:00Z");
        Measurement measurement = new Measurement();
        when(template.find(Measurement.class, id)).thenReturn(Optional.of(measurement));

        Optional<Measurement> result = repository.findById(id);

        assertThat(result).contains(measurement);
        verify(template).find(Measurement.class, id);
    }

    @Test
    void shouldFindMeasurementByTimestamp() {
        Instant timestamp = Instant.parse("2026-09-10T13:00:00Z");
        when(template.select(any(SelectQuery.class))).thenReturn(Stream.of(new Measurement()));

        List<Measurement> result = repository.findByTimestamp(timestamp);

        assertThat(result).hasSize(1);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        CriteriaCondition condition = captor.getValue().condition().orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get(Instant.class)).isEqualTo(timestamp);
        });
    }
}
