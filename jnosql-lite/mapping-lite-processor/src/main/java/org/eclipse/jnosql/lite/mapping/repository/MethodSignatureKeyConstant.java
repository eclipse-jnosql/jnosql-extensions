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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.lite.mapping.repository;

final class MethodSignatureKeyConstant {

    private final String constantName;            // e.g. METHOD_NAME_PARANAME_
    private final String methodName;              // e.g. "methodName"
    private final String parameterClassLiterals;  // e.g. "java.lang.String.class, int.class"

    MethodSignatureKeyConstant(String constantName, String methodName, String parameterClassLiterals) {
        this.constantName = constantName;
        this.methodName = methodName;
        this.parameterClassLiterals = parameterClassLiterals;
    }

    public String getConstantName() {
        return constantName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getParameterClassLiterals() {
        return parameterClassLiterals;
    }
}
