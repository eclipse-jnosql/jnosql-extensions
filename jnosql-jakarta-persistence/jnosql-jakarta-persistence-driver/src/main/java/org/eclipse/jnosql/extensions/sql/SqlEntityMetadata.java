/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql;

import jakarta.persistence.EntityManager;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class SqlEntityMetadata implements EntityMetadata {

    private final String name;
    private final Class<?> entity;
    private final FieldMetadata idField;

    private SqlEntityMetadata(String name, Class<?> entity, FieldMetadata idField) {
        this.name = name;
        this.entity = entity;
        this.idField = idField;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String mappingName() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public String simpleName() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public String className() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public List<String> fieldsName() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public Class<?> type() {
        return entity;
    }

    @Override
    public Optional<InheritanceMetadata> inheritance() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public boolean hasEntityName() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public boolean isInheritance() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public List<FieldMetadata> fields() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public <T> T newInstance() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public ConstructorMetadata constructor() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public String columnField(String javaField) {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public Optional<FieldMetadata> fieldMapping(String javaField) {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public Map<String, FieldMetadata> fieldsGroupByName() {
        throw new UnsupportedOperationException("SQL entities do not support mapping names");
    }

    @Override
    public Optional<FieldMetadata> id() {
        return Optional.ofNullable(idField);
    }


    public static SqlEntityMetadata of(Class<?> entityType, EntityManager entityManager) {
        Objects.requireNonNull(entityType, "entityType is required");
        Objects.requireNonNull(entityManager, "entityManager is required");

        var metamodel = entityManager.getMetamodel().entity(entityType);

        String entityName = metamodel.getName();

        // Find the @Id field manually (provider-safe)
        String idFieldName = Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(jakarta.persistence.Id.class))
                .findFirst()
                .map(Field::getName)
                .orElseThrow(() -> new IllegalStateException(
                        "No @Id field found on entity " + entityType.getName()
                ));
        var idField = new SqlIdFieldMetadata(idFieldName);
        return new SqlEntityMetadata(entityName, entityType, idField);
    }
}