/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import jakarta.nosql.AttributeConverter;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

/**
 *
 * @author Ondro Mihalyi
 */
class JakataPersistenceEntitiesMetadata implements EntitiesMetadata {

    private final PersistenceDatabaseManager databaseManager;

    public JakataPersistenceEntitiesMetadata(PersistenceDatabaseManager databaseManager) {
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
        return new EntityMetadata() {
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
                        .map(FieldMetadataImpl::new)
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
                    return Optional.of(new FieldMetadataImpl(attribute));
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

            record FieldMetadataImpl(Attribute<?, ?> attribute) implements FieldMetadata {

                @Override
                public String name() {
                    return attribute.getName();
                }

                @Override
                public Class<?> type() {
                    return attribute.getJavaType();
                }

                @Override
                public <X, Y, T extends AttributeConverter<X, Y>> Optional<Class<T>> converter() {
                    // Not need to convert, EntityManager works with the same type
                    // and makes the conversion when interacting with the database
                    return Optional.empty();
                }

                @Override
                public String fieldName() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public Object read(Object bean) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public void write(Object bean, Object value) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public Object value(Value value) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public boolean isId() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public Optional<String> udt() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public <T extends Annotation> Optional<String> value(Class<T> type) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public MappingType mappingType() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public <X, Y, T extends AttributeConverter<X, Y>> Optional<T> newConverter() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }
            }
        };
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

}
