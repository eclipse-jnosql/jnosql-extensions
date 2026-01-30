/*
 *  Copyright (c) 2024 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping;

import java.util.List;

public class ConstructorMetamodel extends BaseMappingModel {

    private final String packageName;

    private final String className;

    private final List<ParameterResult> parameters;

    private final String entityName;

    private ConstructorMetamodel(String packageName, String className, List<ParameterResult> parameters,  String entityName) {
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
        return parameters.stream().map(ParameterResult::className).toList();
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

    public static ConstructorMetamodel of(String packageName, String className, List<ParameterResult> parameters, String entityName) {
        return new ConstructorMetamodel(packageName, className, parameters, entityName);
    }
}
