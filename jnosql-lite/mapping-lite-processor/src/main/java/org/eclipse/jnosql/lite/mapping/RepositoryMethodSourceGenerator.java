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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;

final class RepositoryMethodSourceGenerator {

    private static final String MUSTACHE_TEMPLATE = "repository_method_metadata.mustache";
    private static final Mustache TEMPLATE = new DefaultMustacheFactory().compile(MUSTACHE_TEMPLATE);

    private final ProcessingEnvironment processingEnv;

    RepositoryMethodSourceGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    void generate(Element method, RepositoryMethodModel metadata) {
        try {
            var fileObject = processingEnv.getFiler().createSourceFile(metadata.getQualified(), method);
            try (Writer writer = fileObject.openWriter()) {
                TEMPLATE.execute(writer, metadata);
            }
        } catch (IOException exception) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "failed to write extension file: " + exception.getMessage());
        }
    }
}
