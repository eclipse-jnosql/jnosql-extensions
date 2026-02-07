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
import jakarta.data.repository.Find;
import jakarta.data.repository.First;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

final class RepositoryMethodIntrospector {

    private static final String MUSTACHE_TEMPLATE = "repository_method_metadata.mustache";
    private static final String OPTIONAL_EMPTY = "Optional.empty()";
    private static final int FIND_INITIAL_SUBSTRING = 30;
    private static final String FIND_LAST_SUBSTRING = ")";
    private static final String SORT_DESC_MASK = "Sort.desc(\"%s\")";
    private static final String SORT_ASC_MASK = "Sort.asc(\"%s\")";

    private final Element method;
    private final String repository;
    private final ProcessingEnvironment processingEnv;
    private final Mustache template;

    RepositoryMethodIntrospector(Element method, String repository, ProcessingEnvironment processingEnv) {
        this.method = method;
        this.repository = repository;
        this.processingEnv = processingEnv;
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(MUSTACHE_TEMPLATE);
    }

    public static RepositoryMethodIntrospector of(Element method, String type, ProcessingEnvironment processingEnv) {
        return new RepositoryMethodIntrospector(method, type, processingEnv);
    }

    String generateMethodClass() {
        String className = ProcessorUtil.generateClassName(repository, method.getSimpleName().toString());
        String methodName = method.getSimpleName().toString();
        String packageName = method.getEnclosingElement().getEnclosingElement().toString();
        String methodType = MethodTypeUtils.INSTANCE.type(method, processingEnv).name();

        String query = ofNullable(method.getAnnotation(Query.class))
                .map(Query::value)
                .map("Optional.of(\"%s\")"::formatted)
                .orElse(OPTIONAL_EMPTY);
        String first =  ofNullable(method.getAnnotation(First.class))
                .map(First::value)
                .map("OptionalInt.of(%d)"::formatted)
                .orElse("OptionalInt.empty()");
        String find = ofNullable(method.getAnnotation(Find.class))
                .map(Find::toString)
                .map(s -> s.substring(FIND_INITIAL_SUBSTRING, s.lastIndexOf(FIND_LAST_SUBSTRING)))
                .map("Optional.of(%s)"::formatted)
                .orElse(OPTIONAL_EMPTY);
        String returnType = OPTIONAL_EMPTY;
        String elementType = OPTIONAL_EMPTY;

        List<String> selects = Collections.emptyList();
        List<String> sorts = Arrays.stream(method.getAnnotationsByType(OrderBy.class))
                .map(orderBy -> orderBy.descending() ? SORT_DESC_MASK.formatted(orderBy.value()) :
                        SORT_ASC_MASK.formatted(orderBy.value())).toList();
        List<String> annotations = Collections.emptyList();
        List<String> params = Collections.emptyList();
        var metadata = new RepositoryMethodModel(packageName, methodName, className,
                methodType, query, find, first, returnType, elementType,
                selects, sorts, annotations, params);
        try {
            createClass(method, metadata);
        } catch (IOException exception) {
            error(exception);
        }
        return metadata.getQualified();
    }


    private void createClass(Element entity, RepositoryMethodModel metadata) throws IOException {
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
