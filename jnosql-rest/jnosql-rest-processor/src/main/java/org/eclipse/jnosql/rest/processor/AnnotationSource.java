/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.rest.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor14;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes an {@link AnnotationMirror} as Java source using the annotation model, preserving strings, arrays,
 * enum constants, class literals and nested annotations. Only explicitly declared values are written.
 */
final class AnnotationSource {

    private final ImportRegistry imports;
    private final ValueWriter valueWriter = new ValueWriter();

    AnnotationSource(ImportRegistry imports) {
        this.imports = imports;
    }

    String write(AnnotationMirror mirror) {
        String name = "@" + imports.name((TypeElement) mirror.getAnnotationType().asElement());
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
        if (values.isEmpty()) {
            return name;
        }
        if (values.size() == 1) {
            var entry = values.entrySet().iterator().next();
            if (entry.getKey().getSimpleName().contentEquals("value")) {
                return name + "(" + write(entry.getValue()) + ")";
            }
        }
        List<String> assignments = new ArrayList<>();
        values.forEach((key, value) -> assignments.add(key.getSimpleName() + " = " + write(value)));
        return name + "(" + String.join(", ", assignments) + ")";
    }

    private String write(AnnotationValue value) {
        return value.accept(valueWriter, null);
    }

    static String quote(String text) {
        StringBuilder builder = new StringBuilder("\"");
        for (char character : text.toCharArray()) {
            builder.append(escape(character));
        }
        return builder.append('"').toString();
    }

    private static String escape(char character) {
        return switch (character) {
            case '"' -> "\\\"";
            case '\\' -> "\\\\";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            default -> character < 0x20 ? String.format("\\u%04x", (int) character) : String.valueOf(character);
        };
    }

    private final class ValueWriter extends SimpleAnnotationValueVisitor14<String, Void> {

        @Override
        public String visitString(String value, Void unused) {
            return quote(value);
        }

        @Override
        public String visitBoolean(boolean value, Void unused) {
            return String.valueOf(value);
        }

        @Override
        public String visitInt(int value, Void unused) {
            return String.valueOf(value);
        }

        @Override
        public String visitLong(long value, Void unused) {
            return value + "L";
        }

        @Override
        public String visitFloat(float value, Void unused) {
            return value + "F";
        }

        @Override
        public String visitDouble(double value, Void unused) {
            return String.valueOf(value);
        }

        @Override
        public String visitShort(short value, Void unused) {
            return "(short) " + value;
        }

        @Override
        public String visitByte(byte value, Void unused) {
            return "(byte) " + value;
        }

        @Override
        public String visitChar(char value, Void unused) {
            return "'" + (value == '\'' ? "\\'" : escape(value)) + "'";
        }

        @Override
        public String visitType(TypeMirror type, Void unused) {
            return imports.render(type) + ".class";
        }

        @Override
        public String visitEnumConstant(VariableElement constant, Void unused) {
            return imports.name((TypeElement) constant.getEnclosingElement()) + "." + constant.getSimpleName();
        }

        @Override
        public String visitAnnotation(AnnotationMirror annotation, Void unused) {
            return write(annotation);
        }

        @Override
        public String visitArray(List<? extends AnnotationValue> values, Void unused) {
            List<String> items = new ArrayList<>();
            values.forEach(item -> items.add(write(item)));
            return "{" + String.join(", ", items) + "}";
        }

        @Override
        protected String defaultAction(Object value, Void unused) {
            throw new IllegalStateException("Unsupported annotation value: " + value);
        }
    }
}
