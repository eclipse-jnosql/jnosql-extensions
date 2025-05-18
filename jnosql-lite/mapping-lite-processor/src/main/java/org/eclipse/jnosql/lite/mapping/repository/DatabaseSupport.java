package org.eclipse.jnosql.lite.mapping.repository;

import org.eclipse.jnosql.mapping.DatabaseType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

enum DatabaseSupport {

    DOCUMENT("org.eclipse.jnosql.mapping.document.DocumentTemplate"),
    COLUMN("org.eclipse.jnosql.mapping.column.ColumnTemplate"),
    KEY_VALUE("org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate"),
    GRAPH("org.eclipse.jnosql.mapping.graph.GraphTemplate");

    private final String requiredClassName;

    DatabaseSupport(String requiredClassName) {
        this.requiredClassName = requiredClassName;
    }

    public boolean isAvailable() {
        return isClassPresent(requiredClassName);
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    static Set<DatabaseType> detectSupportedTypes() {
        return Arrays.stream(values())
                .filter(DatabaseSupport::isAvailable)
                .map(DatabaseSupport::getDatabaseType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DatabaseType.class)));
    }
}
