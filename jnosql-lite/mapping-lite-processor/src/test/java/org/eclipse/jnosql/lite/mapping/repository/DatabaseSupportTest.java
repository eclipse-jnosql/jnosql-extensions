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
package org.eclipse.jnosql.lite.mapping.repository;

import org.eclipse.jnosql.mapping.DatabaseType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSupportTest {

    @Test
    void shouldSupportVector() {
        assertThat(DatabaseSupport.valueOf("VECTOR").getDatabaseType()).isEqualTo(DatabaseType.VECTOR);
    }

    @Test
    void shouldKeepExistingTypes() {
        assertThat(Arrays.stream(DatabaseSupport.values()).map(DatabaseSupport::getDatabaseType))
                .contains(DatabaseType.DOCUMENT, DatabaseType.COLUMN, DatabaseType.KEY_VALUE,
                        DatabaseType.GRAPH, DatabaseType.TIME_SERIES, DatabaseType.VECTOR);
    }
}
