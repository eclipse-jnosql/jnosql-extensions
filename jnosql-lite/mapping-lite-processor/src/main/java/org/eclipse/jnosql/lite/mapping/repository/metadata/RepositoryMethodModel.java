/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository.metadata;

import org.eclipse.jnosql.lite.mapping.processing.BaseMappingModel;
import java.util.List;

class RepositoryMethodModel extends BaseMappingModel {

    private final String packageName;
    private final String methodName;
    private final String className;
    private final String methodType;
    private final String query;
    private final String find;
    private final String first;
    private final String returnType;
    private final String elementType;
    private final List<String> selects;
    private final List<String> sorts;
    private final List<String> annotations;
    private final List<String> params;
    private final String paramSignature;


    public RepositoryMethodModel(String packageName,
                                 String methodName,
                                 String className,
                                 String methodType,
                                 String query,
                                 String find,
                                 String first,
                                 String returnType,
                                 String elementType,
                                 List<String> selects,
                                 List<String> sorts,
                                 List<String> annotations,
                                 List<String> params,
                                 String paramSignature) {
        this.methodName = methodName;
        this.packageName = packageName;
        this.className = className;
        this.methodType = methodType;
        this.query = query;
        this.find = find;
        this.first = first;
        this.returnType = returnType;
        this.elementType = elementType;
        this.selects = selects;
        this.sorts = sorts;
        this.annotations = annotations;
        this.params = params;
        this.paramSignature = paramSignature;
    }

    public String getClassName() {
        return className +"RepositoryMethodLookup";
    }

    public String getPackageName() {
        return packageName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }

    public String getMethodType() {
        return methodType;
    }

    public String getQuery() {
        return query;
    }

    public String getFind() {
        return find;
    }

    public String getFirst() {
        return first;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getElementType() {
        return elementType;
    }

    public List<String> getSelects() {
        return selects;
    }

    public List<String> getSorts() {
        return sorts;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public List<String> getParams() {
        return params;
    }

    public String getParamSignature() {
        return paramSignature;
    }
}
