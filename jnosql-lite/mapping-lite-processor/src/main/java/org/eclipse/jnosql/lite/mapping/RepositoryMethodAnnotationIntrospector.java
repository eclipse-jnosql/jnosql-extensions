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

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class RepositoryMethodAnnotationIntrospector {

    private static final String MUSTACHE_TEMPLATE = "repository_method_annotations_metadata.mustache";

    private final ProcessingEnvironment processingEnv;
    private final AnnotationMirror annotationMirror;
    private final Element method;
    private final Mustache template;
    private final String methodClassName;
    private final String packageName;

    public RepositoryMethodAnnotationIntrospector(String methodClassName,
                                                  String packageName,
                                                  ProcessingEnvironment processingEnv,
                                                  AnnotationMirror annotationMirror,
                                                  Element method) {
        this.methodClassName = methodClassName;
        this.packageName = packageName;
        this.processingEnv = processingEnv;
        this.annotationMirror = annotationMirror;
        this.method = method;
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(MUSTACHE_TEMPLATE);
    }


    public String createAnnotationClass() {
        Element element = annotationMirror.getAnnotationType().asElement();
        String annotation = element.toString();
        String className = methodClassName.concat(element.getSimpleName().toString());
        //TODO update those values in a next interaction
        String providerAnnotation = "false";
        String provider = "Optional.empty()";
        List<String> attributes = new ArrayList<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            var key = entry.getKey().getSimpleName().toString();
            var value = entry.getValue().getValue() instanceof String ? "\"" + entry.getValue().getValue()+ "\"":
                    entry.getValue().getValue();
            attributes.add("this.attributes.put(" + key + ", " + value + ");");
        }
        var metadata = new RepositoryMethodAnnotationModel(packageName,
                className,
                annotation,
                providerAnnotation,
                provider,
                attributes);
        try {
            createClass(method, metadata);
        } catch (IOException exception) {
            error(exception);
        }
        return metadata.getQualified();
    }

    private void createClass(Element entity, RepositoryMethodAnnotationModel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            template.execute(writer, metadata);
        }
    }

    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }


}
