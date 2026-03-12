/*
 * Copyright (c) 2024,2026 Contributors to the Eclipse Foundation
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
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.communication;

import jakarta.persistence.metamodel.EntityType;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Ondro Mihalyi
 */
class JakartaPersistenceEntitiesMetadata implements EntitiesMetadata {

    private final PersistenceDatabaseManager databaseManager;

    public JakartaPersistenceEntitiesMetadata(PersistenceDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Optional<EntityMetadata> findByClassName(String name) {
        EntityType<?> entityType;
        try {
            entityType = databaseManager.findEntityType(name);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.of(get(entityType.getJavaType()));
    }

    @Override
    public EntityMetadata get(Class<?> entityClass) {
        final EntityType<?> entityType = databaseManager.getEntityManager().getMetamodel().entity(entityClass);
        return PersistenceEntityMetadata.of(entityType, entityClass);
    }

    @Override
    public Map<String, InheritanceMetadata> findByParentGroupByDiscriminatorValue(Class<?> parent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public EntityMetadata findByName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Optional<EntityMetadata> findBySimpleName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Optional<EntityMetadata> findByMappingName(String mappingName) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Optional<ProjectionMetadata> projection(Class<?> projection) {
      return Optional.empty(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
