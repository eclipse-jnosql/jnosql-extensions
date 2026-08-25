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

import org.eclipse.jnosql.lite.mapping.processing.ProcessorUtils;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;

final class RepositoryMethodIntrospector {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final Element method;
    private final String repository;
    private final ProcessingEnvironment processingEnv;
    private final RepositoryMethodQueryIntrospector queryIntrospector;
    private final RepositoryMethodReturnTypeIntrospector returnTypeIntrospector;
    private final RepositoryMethodAnnotationCollector annotationCollector;
    private final RepositoryMethodParameterCollector parameterCollector;
    private final RepositoryMethodSourceGenerator sourceGenerator;

    RepositoryMethodIntrospector(Element method, String repository, ProcessingEnvironment processingEnv) {
        this.method = method;
        this.repository = repository;
        this.processingEnv = processingEnv;
        this.queryIntrospector = new RepositoryMethodQueryIntrospector(method);
        this.returnTypeIntrospector = new RepositoryMethodReturnTypeIntrospector(processingEnv);
        this.annotationCollector = new RepositoryMethodAnnotationCollector(processingEnv, method);
        this.parameterCollector = new RepositoryMethodParameterCollector(processingEnv, method);
        this.sourceGenerator = new RepositoryMethodSourceGenerator(processingEnv);
    }

    public static RepositoryMethodIntrospector of(Element method, String type, ProcessingEnvironment processingEnv) {
        return new RepositoryMethodIntrospector(method, type, processingEnv);
    }

    String generateMethodClass() {

        ExecutableElement executableElement = (ExecutableElement) method;
        List<String> nameElements = new ArrayList<>();
        nameElements.add(repository);
        nameElements.add(method.getSimpleName().toString());
        executableElement.getParameters().stream()
                .map(v -> v.getSimpleName().toString())
                .forEach(nameElements::add);
        String className = ProcessorUtils.className(nameElements.toArray(EMPTY_STRING_ARRAY));
        String methodName = method.getSimpleName().toString();
        String packageName = method.getEnclosingElement().getEnclosingElement().toString();
        String methodType = MethodTypeUtils.INSTANCE.type(method, processingEnv).name();

        var queryMetadata = queryIntrospector.introspect();
        var returnTypeMetadata = returnTypeIntrospector.introspect(executableElement);
        var annotationMetadata = annotationCollector.collect(executableElement, className, packageName);
        if (annotationMetadata.provider()) {
            methodType = "PROVIDER_OPERATION";
        }
        var parameterMetadata = parameterCollector.collect(executableElement, className, packageName);

        var metadata = new RepositoryMethodModel(packageName, methodName, className,
                methodType, queryMetadata.query(), queryMetadata.find(), queryMetadata.first(),
                returnTypeMetadata.returnType(), returnTypeMetadata.elementType(),
                queryMetadata.selects(), queryMetadata.sorts(), annotationMetadata.annotations(),
                parameterMetadata.params(), parameterMetadata.signature());
        sourceGenerator.generate(method, metadata);
        return metadata.getQualified();
    }
}
