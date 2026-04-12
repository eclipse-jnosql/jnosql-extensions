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

/**
 * SQL-specific implementation of {@link EntityMetadata} backed by Jakarta Persistence.
 *
 * <p>This implementation acts as an adapter between the JNoSQL metadata model and
 * the Jakarta Persistence metamodel. It provides only the minimal metadata required
 * to execute SQL-based repository operations, delegating the full mapping responsibility
 * to the underlying JPA provider (e.g., Hibernate or EclipseLink).</p>
 *
 * <p>Unlike document or key-value databases, relational persistence is already fully
 * described by JPA annotations and the persistence provider. Therefore, this class
 * intentionally exposes only a subset of the {@link EntityMetadata} contract, such as
 * entity name and identifier field.</p>
 *
 * <p>All other metadata operations that relate to field mapping, constructors,
 * inheritance, or column resolution are not supported and will throw
 * {@link UnsupportedOperationException}.</p>
 *
 * <p>This design avoids duplicating mapping logic that is already handled by the
 * Jakarta Persistence layer, keeping the integration lightweight and consistent
 * with the JPA programming model.</p>
 *
 * @see EntityMetadata
 * @see jakarta.persistence.EntityManager
 */
public final class SqlEntityMetadata implements EntityMetadata {

    private final String name;
    private final Class<?> entity;
    private final FieldMetadata idField;
    private final String idName;

    private SqlEntityMetadata(String name, Class<?> entity, FieldMetadata idField, String idName) {
        this.name = name;
        this.entity = entity;
        this.idField = idField;
        this.idName = idName;
    }

    public String idName() {
        return idName;
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


    /**
     * Creates a new instance of {@code SqlEntityMetadata} for the given entity type and entity manager.
     *
     * @param entityType    the class type of the entity for which metadata is being created. Must not be null.
     * @param entityManager the {@code EntityManager} used to retrieve the entity's metadata. Must not be null.
     * @return a {@code SqlEntityMetadata} instance containing metadata for the specified entity type.
     * @throws NullPointerException if {@code entityType} or {@code entityManager} is null.
     * @throws IllegalStateException if the specified entity type does not have a field annotated with {@code @Id}.
     */
    public static SqlEntityMetadata of(Class<?> entityType, EntityManager entityManager) {
        Objects.requireNonNull(entityType, "entityType is required");
        Objects.requireNonNull(entityManager, "entityManager is required");

        var metamodel = entityManager.getMetamodel().entity(entityType);

        String entityName = metamodel.getName();

        Field idAttribute = Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(jakarta.persistence.Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No @Id field found on entity " + entityType.getName()
                ));
        var idFieldName = idAttribute.getName();
        idAttribute.setAccessible(true);
        var idField = new SqlIdFieldMetadata(idFieldName, idAttribute);
        return new SqlEntityMetadata(entityName, entityType, idField, idFieldName);
    }
}