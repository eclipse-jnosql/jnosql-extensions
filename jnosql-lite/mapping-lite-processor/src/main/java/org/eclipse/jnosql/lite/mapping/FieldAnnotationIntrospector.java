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

import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Id;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;

final class FieldAnnotationIntrospector {

    private static final List<String> DEFAULT_ANNOTATIONS = List.of(
            Column.class.getName(),
            Id.class.getName(),
            Convert.class.getName());

    private final Element field;

    FieldAnnotationIntrospector(Element field) {
        this.field = field;
    }

    AnnotationMetadata introspect(String fieldName) {
        var column = field.getAnnotation(Column.class);
        var id = field.getAnnotation(Id.class);
        var converter = field.getAnnotation(Convert.class);
        var valueAnnotations = new ArrayList<ValueAnnotationModel>();

        for (var annotationMirror : field.getAnnotationMirrors()) {
            var annotationType = annotationMirror.getAnnotationType();
            if (DEFAULT_ANNOTATIONS.contains(annotationType.toString())) {
                continue;
            }
            annotationMirror.getElementValues().entrySet().stream()
                    .filter(entry -> entry.getKey().toString().equals("value()"))
                    .findFirst()
                    .ifPresent(entry -> valueAnnotations.add(new ValueAnnotationModel(
                            annotationType + ".class",
                            entry.getValue().getValue().toString())));
        }

        return new AnnotationMetadata(
                name(fieldName, column, id),
                id != null,
                converter,
                column != null ? column.udt() : null,
                valueAnnotations);
    }

    private static String name(String fieldName, Column column, Id id) {
        if (id != null) {
            return id.value().isBlank() ? fieldName : id.value();
        }
        return column.value().isBlank() ? fieldName : column.value();
    }

    record AnnotationMetadata(String name,
                              boolean id,
                              Convert converter,
                              String udt,
                              List<ValueAnnotationModel> valueAnnotations) {
    }
}
