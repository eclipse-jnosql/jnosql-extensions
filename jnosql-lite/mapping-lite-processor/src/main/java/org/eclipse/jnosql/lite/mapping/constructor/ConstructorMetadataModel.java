/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.lite.mapping.constructor;

import org.eclipse.jnosql.lite.mapping.processing.BaseMappingModel;

import java.util.List;

public final class ConstructorMetadataModel extends BaseMappingModel {

    private final String packageName;
    private final String className;
    private final List<ConstructorParameter> parameters;
    private final String entityName;

    private ConstructorMetadataModel(String packageName,
                                     String className,
                                     List<ConstructorParameter> parameters,
                                     String entityName) {
        this.packageName = packageName;
        this.className = className;
        this.parameters = parameters;
        this.entityName = entityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className + "ConstructorMetadata";
    }

    public List<String> getParameters() {
        return parameters.stream().map(ConstructorParameter::className).toList();
    }

    public String getConstructorParameters() {
        var constructorParameters = new StringBuilder();
        for (int index = 0; index < parameters.size(); index++) {
            constructorParameters.append('(').append(parameters.get(index).type()).append(')');
            constructorParameters.append(" parameters[").append(index).append("]");
            if (index < parameters.size() - 1) {
                constructorParameters.append(", ");
            }
        }
        return constructorParameters.toString();
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }

    public String getEntityName() {
        return entityName;
    }

    public static ConstructorMetadataModel of(String packageName,
                                              String className,
                                              List<ConstructorParameter> parameters,
                                              String entityName) {
        return new ConstructorMetadataModel(packageName, className, parameters, entityName);
    }
}
