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
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.communication;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PersistenceEntityMetadata implements EntityMetadata {

    private final EntityType<?> entityType;

    private final Class<?> entityClass;

    PersistenceEntityMetadata(EntityType<?> entityType, Class<?> entityClass) {
        this.entityType = entityType;
        this.entityClass = entityClass;
    }

    @Override
    public String name() {
        return entityType.getName();
    }

    @Override
    public String simpleName() {
        return entityClass.getSimpleName();
    }

    @Override
    public String className() {
        return entityClass.getName();
    }

    @Override
    public List<String> fieldsName() {
        return entityType.getAttributes().stream()
                .map(Attribute::getName)
                .toList();
    }

    @Override
    public Class<?> type() {
        return entityClass;
    }

    @Override
    public List<FieldMetadata> fields() {
        List<FieldMetadata> result = new java.util.ArrayList<>();
        entityType.getAttributes().stream()
                .map(PersistenceFieldMetadata::new)
                .forEach(result::add);
        return result;
    }

    @Override
    public Optional<InheritanceMetadata> inheritance() {
        //Inheritance for JPA entities shouldn't be handled by JNoSQL directly but by an entityManager
        return Optional.empty();
    }

    @Override
    public String columnField(String javaField) {
        return javaField;
    }

    @Override
    public Optional<FieldMetadata> fieldMapping(String javaField) {
        try {
            Attribute<?, ?> attribute = entityType.getAttribute(javaField);
            return Optional.of(new PersistenceFieldMetadata(attribute));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasEntityName() {
        throw new UnsupportedOperationException("Is it used or handled by an entityManager?");
    }

    @Override
    public boolean isInheritance() {
        throw new UnsupportedOperationException("Is it used or handled by an entityManager?");
    }

    @Override
    public <T> T newInstance() {
        throw new UnsupportedOperationException("Is it used or handled by an entityManager?");
    }

    @Override
    public ConstructorMetadata constructor() {
        throw new UnsupportedOperationException("Is it used or handled by an entityManager?");
    }

    @Override
    public Map<String, FieldMetadata> fieldsGroupByName() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Optional<FieldMetadata> id() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String mappingName() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


}
