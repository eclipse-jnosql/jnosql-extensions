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
package org.eclipse.jnosql.lite.mapping.entity.field;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.eclipse.jnosql.lite.mapping.processing.ProcessorValidationException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

final class FieldSourceGenerator {

    private static final String NULL = "null";
    private static final Mustache DEFAULT_TEMPLATE = template("field_metadata.mustache");
    private static final Mustache COLLECTION_TEMPLATE = template("field_collection_metadata.mustache");
    private static final Mustache MAP_TEMPLATE = template("field_map_metadata.mustache");
    private static final Mustache ARRAY_TEMPLATE = template("field_array_metadata.mustache");

    private final ProcessingEnvironment processingEnv;

    FieldSourceGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    void generate(TypeElement entity, FieldModel metadata) {
        JavaFileObject fileObject = fileObject(entity, metadata);
        try (Writer writer = fileObject.openWriter()) {
            template(metadata).execute(writer, metadata);
        } catch (IOException exception) {
            throw new ProcessorValidationException("An error to compile the class: "
                    + metadata.getQualified(), exception);
        }
    }

    private JavaFileObject fileObject(TypeElement entity, FieldModel metadata) {
        try {
            return processingEnv.getFiler().createSourceFile(metadata.getQualified(), entity);
        } catch (IOException exception) {
            throw new ProcessorValidationException("An error to create the class: "
                    + metadata.getQualified(), exception);
        }
    }

    private static Mustache template(FieldModel metadata) {
        if (NULL.equals(metadata.getElementType())) {
            return DEFAULT_TEMPLATE;
        }
        if (metadata.getType().contains("Map")) {
            return MAP_TEMPLATE;
        }
        if (metadata.getType().contains("[]")) {
            return ARRAY_TEMPLATE;
        }
        return COLLECTION_TEMPLATE;
    }

    private static Mustache template(String name) {
        return new DefaultMustacheFactory().compile(name);
    }
}
