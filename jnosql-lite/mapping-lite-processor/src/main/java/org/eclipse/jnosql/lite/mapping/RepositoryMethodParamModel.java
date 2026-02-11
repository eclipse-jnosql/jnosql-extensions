/*
 *  Copyright (c) 2026 Otávio Santana and others
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

class RepositoryMethodParamModel extends BaseMappingModel {

    private final String packageName;
    private final String className;
    private final String constraint;
    private final String name;
    private final String param;
    private final String by;
    private final String elementType;

    RepositoryMethodParamModel(String packageName,
                               String className,
                               String constraint,
                               String name,
                               String param,
                               String by,
                               String elementType) {

        this.packageName = packageName;
        this.className = className;
        this.constraint = constraint;
        this.name = name;
        this.param = param;
        this.by = by;
        this.elementType = elementType;
    }


    public String getClassName() {
        return className +"RepositoryParameter";
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getConstraint() {
        return constraint;
    }

    public String getName() {
        return name;
    }

    public String getParam() {
        return param;
    }

    public String getBy() {
        return by;
    }

    public String getElementType() {
        return elementType;
    }
}
