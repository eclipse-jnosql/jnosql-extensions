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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

final class RepositoryMethodAnnotationIntrospector {

    private static final String MUSTACHE_TEMPLATE = "repository_method_annotations_metadata.mustache";
    private static final String PROVIDER_QUERY_FQCN = "org.eclipse.jnosql.mapping.ProviderQuery";

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
        TypeElement annotationType =
                (TypeElement) annotationMirror.getAnnotationType().asElement();

        String annotation = annotationType.getQualifiedName().toString();

        String className = methodClassName
                .concat(annotationType.getSimpleName().toString())
                .concat(Integer.toHexString(annotation.hashCode()));

        ProviderQueryInfo providerInfo = resolveProviderQuery(annotationType);

        String providerAnnotation = Boolean.toString(providerInfo.present());
        String provider = (providerInfo.value() == null || providerInfo.value().isBlank())
                ? "Optional.empty()"
                : "Optional.of(\"" + escape(providerInfo.value()) + "\")";

        List<String> attributes = new ArrayList<>();

        var metadata = new RepositoryMethodAnnotationModel(
                packageName,
                className,
                annotation,
                providerAnnotation,
                provider,
                attributes
        );

        try {
            createClass(method, metadata);
        } catch (IOException exception) {
            error(exception);
        }

        return metadata.getQualified();
    }

    private ProviderQueryInfo resolveProviderQuery(TypeElement annotationType) {
        for (AnnotationMirror mirror : annotationType.getAnnotationMirrors()) {
            Element element = mirror.getAnnotationType().asElement();
            if (!(element instanceof TypeElement typeElement)) {
                continue;
            }

            String fqcn = typeElement.getQualifiedName().toString();

            if (PROVIDER_QUERY_FQCN.equals(fqcn)) {
                String value = extractValue(mirror, "value");
                return new ProviderQueryInfo(true, value);
            }
        }
        return new ProviderQueryInfo(false, null);
    }

    private String extractValue(AnnotationMirror mirror, String attributeName) {
        for (var entry : mirror.getElementValues().entrySet()) {
            String name = entry.getKey().getSimpleName().toString();
            if (attributeName.equals(name)) {
                Object v = entry.getValue().getValue();
                return v == null ? null : v.toString();
            }
        }

        TypeElement annotationElement =
                (TypeElement) mirror.getAnnotationType().asElement();

        for (Element e : annotationElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD
                    && e.getSimpleName().contentEquals(attributeName)) {

                AnnotationValue defaultValue =
                        ((ExecutableElement) e).getDefaultValue();

                if (defaultValue == null) {
                    return null;
                }

                Object v = defaultValue.getValue();
                return v == null ? null : v.toString();
            }
        }
        return null;
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private void createClass(Element entity,
                             RepositoryMethodAnnotationModel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject =
                filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            template.execute(writer, metadata);
        }
    }

    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "failed to write extension file: " + exception.getMessage()
        );
    }

    private record ProviderQueryInfo(boolean present, String value) {
    }
}