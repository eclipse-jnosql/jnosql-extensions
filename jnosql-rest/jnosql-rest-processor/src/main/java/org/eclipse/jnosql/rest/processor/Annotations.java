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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.Optional;

/**
 * Lookup helpers for annotations that are identified by qualified name, so the processor does not need the
 * annotation classes on its own classpath.
 */
final class Annotations {

    private Annotations() {
    }

    static Optional<? extends AnnotationMirror> find(Element element, String qualifiedName) {
        return element.getAnnotationMirrors().stream()
                .filter(mirror -> qualifiedName(mirror).equals(qualifiedName))
                .findFirst();
    }

    static boolean has(Element element, String qualifiedName) {
        return find(element, qualifiedName).isPresent();
    }

    static boolean hasSimpleName(Element element, String simpleName) {
        return element.getAnnotationMirrors().stream()
                .anyMatch(mirror -> mirror.getAnnotationType().asElement().getSimpleName().contentEquals(simpleName));
    }

    static Optional<String> stringValue(Element element, String qualifiedName) {
        return find(element, qualifiedName)
                .flatMap(Annotations::value)
                .map(value -> value.getValue().toString())
                .filter(text -> !text.isBlank());
    }

    /**
     * Reads the {@code value} member of an annotation, keeping blank values, so an explicit empty value can be
     * told apart from an absent annotation.
     *
     * @param element       the annotated element
     * @param qualifiedName the annotation type
     * @return the value, or empty when the annotation is absent or has no value
     */
    static Optional<String> rawStringValue(Element element, String qualifiedName) {
        return find(element, qualifiedName)
                .flatMap(Annotations::value)
                .map(value -> value.getValue().toString());
    }

    static String qualifiedName(AnnotationMirror mirror) {
        return ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
    }

    private static Optional<? extends AnnotationValue> value(AnnotationMirror mirror) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals("value")) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
