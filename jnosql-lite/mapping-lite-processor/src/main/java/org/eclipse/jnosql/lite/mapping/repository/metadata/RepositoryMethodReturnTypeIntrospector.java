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
package org.eclipse.jnosql.lite.mapping.repository.metadata;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;

import static java.util.Optional.ofNullable;

final class RepositoryMethodReturnTypeIntrospector {

    private static final String OPTIONAL_EMPTY = "Optional.empty()";
    private static final String OPTIONAL_CLASS_MASK = "Optional.of(%s.class)";

    private final ProcessingEnvironment processingEnv;

    RepositoryMethodReturnTypeIntrospector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    ReturnTypeMetadata introspect(ExecutableElement method) {
        return new ReturnTypeMetadata(returnType(method), elementType(method));
    }

    private String returnType(ExecutableElement method) {
        TypeElement returnElement =
                (TypeElement) processingEnv.getTypeUtils().asElement(method.getReturnType());
        return ofNullable(returnElement)
                .map(Object::toString)
                .map(OPTIONAL_CLASS_MASK::formatted)
                .orElse(OPTIONAL_CLASS_MASK.formatted(method.getReturnType().toString()));
    }

    private static String elementType(ExecutableElement method) {
        if (method.getReturnType() instanceof DeclaredType declaredType) {
            return declaredType.getTypeArguments().stream()
                    .map(Object::toString)
                    .findFirst()
                    .map(OPTIONAL_CLASS_MASK::formatted)
                    .orElse(OPTIONAL_EMPTY);
        }
        if (method.getReturnType() instanceof ArrayType arrayType) {
            return OPTIONAL_CLASS_MASK.formatted(arrayType.getComponentType().toString());
        }
        return OPTIONAL_EMPTY;
    }

    record ReturnTypeMetadata(String returnType, String elementType) {
    }
}
