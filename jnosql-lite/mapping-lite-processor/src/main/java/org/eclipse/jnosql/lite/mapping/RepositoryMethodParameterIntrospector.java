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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.data.repository.By;
import jakarta.data.repository.Param;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

final class RepositoryMethodParameterIntrospector {

    private static final String MUSTACHE_TEMPLATE = "repository_method_params_metadata.mustache";

    private static final Mustache TEMPLATE;

    static {
        MustacheFactory factory = new DefaultMustacheFactory();
        TEMPLATE = factory.compile(MUSTACHE_TEMPLATE);
    }

    private final ProcessingEnvironment processingEnv;
    private final String methodClassName;
    private final String packageName;
    private final VariableElement variableElement;
    private final Element method;

    RepositoryMethodParameterIntrospector(ProcessingEnvironment processingEnv,
                                          String methodClassName,
                                          String packageName,
                                          VariableElement variableElement,
                                          Element method) {
        this.processingEnv = processingEnv;
        this.methodClassName = methodClassName;
        this.packageName = packageName;
        this.variableElement = variableElement;
        this.method = method;
    }


    String createClass() {
        String constraint = null;
        String name = variableElement.getSimpleName().toString();
        String param = Optional.ofNullable(variableElement.getAnnotation(Param.class))
                .map(Param::value).orElse(name);
        String className = methodClassName.concat(name);
        String by = Optional.ofNullable(variableElement.getAnnotation(By.class))
                .map(By::value).orElse(name);
        String elementType = "Optional.of(%s)".formatted(variableElement.asType().toString());
        var metadata = new RepositoryMethodParamModel(packageName, className, constraint, name, param, by, elementType);
        try {
            createClass(method, metadata);
        } catch (IOException exception) {
            error(exception);
        }
        return metadata.getQualified();
    }

    private void createClass(Element entity, RepositoryMethodParamModel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            TEMPLATE.execute(writer, metadata);
        }
    }

    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }
}
