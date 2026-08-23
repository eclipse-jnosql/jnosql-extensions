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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class RepositoryMethodAnnotationCollector {

    private final ProcessingEnvironment processingEnv;
    private final Element method;

    RepositoryMethodAnnotationCollector(ProcessingEnvironment processingEnv, Element method) {
        this.processingEnv = processingEnv;
        this.method = method;
    }

    AnnotationMetadata collect(ExecutableElement executableElement, String className, String packageName) {
        List<String> annotations = new ArrayList<>();
        Set<String> declaredTypes = new HashSet<>();
        boolean provider = false;

        for (var annotationMirror : executableElement.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            if (declaredTypes.add(annotationName)) {
                var introspector = new RepositoryMethodAnnotationIntrospector(
                        className,
                        packageName,
                        processingEnv,
                        annotationMirror,
                        method);
                var annotationClass = introspector.createAnnotationClass();
                provider |= annotationClass.provider();
                annotations.add(annotationClass.qualified());
            }
        }

        return new AnnotationMetadata(annotations, provider);
    }

    record AnnotationMetadata(List<String> annotations, boolean provider) {
    }
}
