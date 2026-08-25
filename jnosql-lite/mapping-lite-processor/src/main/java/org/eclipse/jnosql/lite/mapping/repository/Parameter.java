/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;

import jakarta.data.repository.By;
import jakarta.data.repository.Param;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.joining;

record Parameter(String name, Param param, By by, TypeElement type, String genericType, String arrayType,
                 TypeKind kind) {

    public boolean hasParam() {
        return param != null;
    }

    public String parameterName() {
        if (isGeneric()) {
            return type.toString() + "<" + genericType + "> " + name;
        }
        if (isArray()) {
            return arrayType + " " + name;
        } else if (kind.isPrimitive()) {
            return kind.name().toLowerCase(Locale.ENGLISH) + " " + name;
        }
        return type.toString() + " " + name();
    }

    public boolean isGeneric() {
        return genericType != null && !genericType.isBlank();
    }

    public boolean isArray() {
        return arrayType != null;
    }

    public static Parameter of(VariableElement element, ProcessingEnvironment processingEnv) {
        String name = element.getSimpleName().toString();
        Param param = element.getAnnotation(Param.class);
        By by = element.getAnnotation(By.class);
        TypeMirror typeMirror = element.asType();
        String arrayType = null;
        String genericType = null;
        if (typeMirror instanceof DeclaredType declaredType) {
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            genericType = typeArguments.stream().map(TypeMirror::toString).collect(joining(","));
        }
        if (typeMirror instanceof ArrayType) {
            arrayType = typeMirror.toString();
        }

        TypeElement type = (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);
        return new Parameter(name, param, by, type, genericType, arrayType, typeMirror.getKind());
    }


}
