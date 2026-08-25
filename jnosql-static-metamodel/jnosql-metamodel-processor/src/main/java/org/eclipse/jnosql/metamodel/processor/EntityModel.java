/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.metamodel.processor;

import java.util.List;

final class EntityModel extends BaseMappingModel {

    private final String packageName;

    private final String entity;

    private final String name;

    private final List<FieldModel> fields;

    private final String superClass;

    EntityModel(String packageName, String entity, String name,
                       List<FieldModel> fields, String superClass) {
        this.packageName = packageName;
        this.entity = entity;
        this.name = name;
        this.fields = fields;
        this.superClass = superClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getEntity() {
        return entity;
    }

    public String getEntityQualified() {
        return packageName + '.' + entity;
    }

    public String getClassName() {
        return "_" + entity ;
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }

    public String getName() {
        return name;
    }

    public List<FieldModel> getFields() {
        return fields;
    }

    public String getSuperClass() {
        return superClass;
    }

    public boolean containsStringAttribute() {
        return this.fields.stream().anyMatch(FieldModel::isStringAttribute);
    }

    public boolean containsBooleanAttribute() {
        return this.fields.stream().anyMatch(FieldModel::isBooleanAttribute);
    }

    public boolean containsCriteriaAttribute() {
        return this.fields.stream().anyMatch(FieldModel::isCriteriaAttribute);
    }

    @Override
    public String toString() {
        return "EntityModel{" +
                "packageName='" + packageName + '\'' +
                ", entity='" + entity + '\'' +
                ", name='" + name + '\'' +
                ", fields=" + fields +
                ", superClass='" + superClass + '\'' +
                '}';
    }
}
