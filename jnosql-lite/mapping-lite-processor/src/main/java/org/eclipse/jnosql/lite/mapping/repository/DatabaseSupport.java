/*
 *  Copyright (c) 2025 Otávio Santana and others
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

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

enum DatabaseSupport {

    DOCUMENT("org.eclipse.jnosql.mapping.document.DocumentTemplate", DatabaseType.DOCUMENT),
    TIME_SERIES("org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate", DatabaseType.TIME_SERIES),
    COLUMN("org.eclipse.jnosql.mapping.column.ColumnTemplate", DatabaseType.COLUMN),
    KEY_VALUE("org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate", DatabaseType.KEY_VALUE),
    GRAPH("org.eclipse.jnosql.mapping.graph.GraphTemplate", DatabaseType.GRAPH);

    private final String requiredClassName;
    private final DatabaseType databaseType;

    DatabaseSupport(String requiredClassName, DatabaseType databaseType) {
        this.requiredClassName = requiredClassName;
        this.databaseType = databaseType;
    }

    boolean isSupported(ProcessingEnvironment processingEnv) {
        return processingEnv.getElementUtils()
                .getTypeElement(requiredClassName) != null;
    }

    DatabaseType getDatabaseType() {
        return databaseType;
    }

    static Set<DatabaseType> types(ProcessingEnvironment processingEnv) {
        return Arrays.stream(values())
                .filter(databaseSupport -> databaseSupport.isSupported(processingEnv))
                .map(DatabaseSupport::getDatabaseType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DatabaseType.class)));
    }
}