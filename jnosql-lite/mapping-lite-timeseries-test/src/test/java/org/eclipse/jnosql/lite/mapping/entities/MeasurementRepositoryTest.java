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
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurementRepositoryTest {

    @Mock
    private TimeSeriesTemplate template;

    @Mock
    private RepositoryOperationProvider repositoryOperationProvider;

    @Mock
    private LifecycleEventHandler lifecycleEventHandler;

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
        FindByOperation operation = mock(FindByOperation.class);
        when(repositoryOperationProvider.findByOperation()).thenReturn(operation);
        when(operation.execute(any())).thenReturn(List.of(new Measurement()));

        List<Measurement> result = repository.findByTimestamp(timestamp);

        assertThat(result).hasSize(1);
        verify(repositoryOperationProvider).findByOperation();
        verify(operation).execute(any());
    }
}
