package org.eclipse.jnosql.lite.mapping.repository;

import org.eclipse.jnosql.mapping.DatabaseType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

enum DatabaseSupport {

    DOCUMENT("org.eclipse.jnosql.mapping.document.DocumentTemplate", DatabaseType.DOCUMENT),
    COLUMN("org.eclipse.jnosql.mapping.column.ColumnTemplate", DatabaseType.COLUMN),
    KEY_VALUE("org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate", DatabaseType.KEY_VALUE),
    GRAPH("org.eclipse.jnosql.mapping.graph.GraphTemplate", DatabaseType.GRAPH);

    private final String requiredClassName;
    private final DatabaseType databaseType;

    DatabaseSupport(String requiredClassName, DatabaseType databaseType) {
        this.requiredClassName = requiredClassName;
        this.databaseType = databaseType;
    }

    boolean isSupported() {
        try {
            Class.forName(requiredClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    DatabaseType getDatabaseType() {
        return databaseType;
    }

    static Set<DatabaseType> detectSupportedTypes() {
        return Arrays.stream(values())
                .filter(DatabaseSupport::isSupported)
                .map(DatabaseSupport::getDatabaseType)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DatabaseType.class)));
    }
}
