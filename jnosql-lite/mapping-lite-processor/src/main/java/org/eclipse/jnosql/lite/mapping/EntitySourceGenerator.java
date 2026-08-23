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
import java.io.IOException;
import java.io.Writer;

final class EntitySourceGenerator {

    private static final Mustache ENTITY_TEMPLATE =
            new DefaultMustacheFactory().compile("entity_metadata.mustache");
    private static final Mustache CONSTRUCTOR_TEMPLATE =
            new DefaultMustacheFactory().compile("constructor_metadata.mustache");

    private final ProcessingEnvironment processingEnv;

    EntitySourceGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    void generateEntity(Element entity, EntityModel metadata) throws IOException {
        generate(entity, metadata.getQualified(), metadata, ENTITY_TEMPLATE);
    }

    void generateConstructor(Element entity, ConstructorMetamodel metadata) throws IOException {
        generate(entity, metadata.getQualified(), metadata, CONSTRUCTOR_TEMPLATE);
    }

    private void generate(Element entity, String qualifiedName, Object metadata, Mustache template)
            throws IOException {
        var fileObject = processingEnv.getFiler().createSourceFile(qualifiedName, entity);
        try (Writer writer = fileObject.openWriter()) {
            template.execute(writer, metadata);
        }
    }
}
