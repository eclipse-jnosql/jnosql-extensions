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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.stream.Collectors;

final class MethodSignatureKeyExtractor {

    static List<MethodSignatureKeyConstant> extract(List<? extends Element> enclosed,
                                                    ProcessingEnvironment env) {
        return enclosed.stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(m -> !m.getModifiers().contains(Modifier.DEFAULT)) // ignore default
                .map(m -> toConstant(m, env))
                .collect(Collectors.toList());
    }

    private static MethodSignatureKeyConstant toConstant(ExecutableElement method,
                                                         ProcessingEnvironment env) {
        String methodName = method.getSimpleName().toString();

        String constantName = buildConstantName(method);

        String parameterClassLiterals = method.getParameters().stream()
                .map(p -> toClassLiteral(p.asType(), env))
                .collect(Collectors.joining(", "));

        return new MethodSignatureKeyConstant(constantName, methodName, parameterClassLiterals);
    }

    private static String buildConstantName(ExecutableElement method) {
        String methodToken = method.getSimpleName().toString().toUpperCase();

        String paramsToken = method.getParameters().stream()
                .map(VariableElement::getSimpleName)
                .map(Object::toString)
                .map(String::toUpperCase)
                .collect(Collectors.joining("_"));

        return paramsToken.isEmpty() ? methodToken : methodToken + "_" + paramsToken;
    }

    private static String toClassLiteral(TypeMirror mirror, ProcessingEnvironment env) {
        Types types = env.getTypeUtils();

        if (mirror.getKind().isPrimitive()) {
            return mirror.getKind().name().toLowerCase() + ".class";
        }

        TypeMirror erased = types.erasure(mirror);
        return erased.toString() + ".class";
    }

    private MethodSignatureKeyExtractor() {}
}