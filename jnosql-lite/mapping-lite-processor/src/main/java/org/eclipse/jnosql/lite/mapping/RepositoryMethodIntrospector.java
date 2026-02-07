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
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

final class RepositoryMethodIntrospector {

    private static final String MUSTACHE_TEMPLATE = "repository_method_metadata.mustache";

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
        String query = "Optional.empty()";
        String find = "Optional.empty()";
        RepositoryMethodModel metadata = new RepositoryMethodModel(packageName, methodName, className,
                methodType, query, find);
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
