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
import jakarta.data.repository.Is;
import jakarta.data.repository.Param;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

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


    ParamResult createClass() {

        String constraint = Optional.ofNullable(variableElement.getAnnotation(Is.class))
                .map(Object::toString)
                .orElse("Optional.empty()");
        var name = variableElement.getSimpleName().toString();
        var param = Optional.ofNullable(variableElement.getAnnotation(Param.class))
                .map(Param::value).orElse(name);
        var simpleName = variableElement.getSimpleName().toString().substring(0, 1).toUpperCase()
                .concat(variableElement.getSimpleName().toString()
                        .substring(1));
        var className = methodClassName.concat(simpleName);
        var by = Optional.ofNullable(variableElement.getAnnotation(By.class))
                .map(By::value).orElse(name);

        var types = processingEnv.getTypeUtils();
        var type = types.erasure(variableElement.asType()).toString();
        var elementType = getElementType();
        var metadata = new RepositoryMethodParamModel(packageName, className, constraint, name, param, by,type,
                elementType);
        try {
            createClass(method, metadata);
        } catch (IOException exception) {
            error(exception);
        }
        return new ParamResult(type, metadata.getQualified());
    }

    private String getElementType() {
        TypeMirror typeMirror = variableElement.asType();
        if (typeMirror instanceof DeclaredType declaredType) {
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if(typeArguments.isEmpty()) {
                return "Optional.empty()";
            }
            var elementType = typeArguments.stream().map(TypeMirror::toString).collect(joining(","));
            return "Optional.of(%s.class)".formatted(elementType);
        }
        if (typeMirror instanceof ArrayType) {
            var elementType = typeMirror.toString();
            return "Optional.of(%s.class)".formatted(elementType);
        }
        return "Optional.empty()";
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

    record ParamResult(String type, String qualified) {}
}
