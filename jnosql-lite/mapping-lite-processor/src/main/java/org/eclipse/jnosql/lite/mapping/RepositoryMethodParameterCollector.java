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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.List;

import static java.util.stream.Collectors.joining;

final class RepositoryMethodParameterCollector {

    private final ProcessingEnvironment processingEnv;
    private final Element method;

    RepositoryMethodParameterCollector(ProcessingEnvironment processingEnv, Element method) {
        this.processingEnv = processingEnv;
        this.method = method;
    }

    ParameterMetadata collect(ExecutableElement executableElement, String className, String packageName) {
        List<RepositoryMethodParameterIntrospector.ParamResult> results = executableElement.getParameters().stream()
                .map(parameter -> new RepositoryMethodParameterIntrospector(
                        processingEnv,
                        className,
                        packageName,
                        parameter,
                        method))
                .map(RepositoryMethodParameterIntrospector::createClass)
                .toList();

        List<String> params = results.stream()
                .map(RepositoryMethodParameterIntrospector.ParamResult::qualified)
                .toList();
        String signature = results.stream()
                .map(RepositoryMethodParameterIntrospector.ParamResult::type)
                .map(type -> type.concat(".class"))
                .collect(joining(","));

        return new ParameterMetadata(params, signature);
    }

    record ParameterMetadata(List<String> params, String signature) {
    }
}
