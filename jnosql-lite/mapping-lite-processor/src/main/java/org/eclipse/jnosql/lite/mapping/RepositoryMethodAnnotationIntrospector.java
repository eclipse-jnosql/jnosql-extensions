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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
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

    public RepositoryMethodAnnotationResult createAnnotationClass() {
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

        List<String> attributes = resolveAttributes(annotationType);

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
        return new RepositoryMethodAnnotationResult(metadata.getQualified(), providerInfo.present());
    }

    private List<String> resolveAttributes(TypeElement annotationType) {
        List<String> attributes = new ArrayList<>();

        for (Element enclosed : annotationType.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement methodElement = (ExecutableElement) enclosed;
            String attributeName = methodElement.getSimpleName().toString();

            Object value = resolveAnnotationValue(methodElement);

            if (value == null) {
                continue;
            }

            String literal = toJavaLiteral(value, methodElement.getReturnType());

            attributes.add("this.attributes.put(\"" + attributeName + "\", " + literal + ")");
        }

        return attributes;
    }

    private Object resolveAnnotationValue(ExecutableElement attributeMethod) {

        for (var entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().equals(attributeMethod)) {
                return entry.getValue().getValue();
            }
        }

        AnnotationValue defaultValue = attributeMethod.getDefaultValue();
        return defaultValue != null ? defaultValue.getValue() : null;
    }

    private String toJavaLiteral(Object value, TypeMirror returnType) {

        if (value instanceof String s) {
            return "\"" + escape(s) + "\"";
        }

        if (value instanceof Character c) {
            return "'" + escape(c.toString()) + "'";
        }

        if (value instanceof Boolean
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Double
                || value instanceof Float
                || value instanceof Short
                || value instanceof Byte) {
            return value.toString();
        }

        if (value instanceof VariableElement enumConstant) {
            TypeElement enumType = (TypeElement) enumConstant.getEnclosingElement();
            return enumType.getQualifiedName() + "." + enumConstant.getSimpleName();
        }

        if (value instanceof List<?> list) {
            List<String> values = new ArrayList<>();
            for (Object v : list) {
                if (v instanceof AnnotationValue av) {
                    values.add(toJavaLiteral(av.getValue(), returnType));
                }
            }
            return "java.util.List.of(" + String.join(", ", values) + ")";
        }

        return "\"" + escape(value.toString()) + "\"";
    }

    private ProviderQueryInfo resolveProviderQuery(TypeElement annotationType) {
        for (AnnotationMirror mirror : annotationType.getAnnotationMirrors()) {
            Element element = mirror.getAnnotationType().asElement();
            if (!(element instanceof TypeElement typeElement)) {
                continue;
            }

            String fqcn = typeElement.getQualifiedName().toString();

            if (PROVIDER_QUERY_FQCN.equals(fqcn)) {
                String value = extractValue(mirror);
                return new ProviderQueryInfo(true, value);
            }
        }
        return new ProviderQueryInfo(false, null);
    }

    private String extractValue(AnnotationMirror mirror) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals("value")) {
                Object v = entry.getValue().getValue();
                return v == null ? null : v.toString();
            }
        }
        return null;
    }

    private String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\"':
                    escaped.append("\\\"");
                    break;
                case '\'':
                    escaped.append("\\'");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                default:
                    if (ch < 0x20 || ch == 0x7f) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                    break;
            }
        }
        return escaped.toString();
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

    record RepositoryMethodAnnotationResult(String qualified, boolean provider) {
    }
}