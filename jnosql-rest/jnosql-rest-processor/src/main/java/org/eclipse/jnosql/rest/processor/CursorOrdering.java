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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Checks that a cursor-paginated method declares an ordering that includes the unique identifier of the entity, the
 * only attribute the processor can prove to be unique. Any ordering that contains it is a total order.
 */
final class CursorOrdering {

    private static final String ORDER_BY = "jakarta.data.repository.OrderBy";
    private static final String ORDER_BY_LIST = "jakarta.data.repository.OrderBy.List";
    private static final String QUERY = "jakarta.data.repository.Query";

    private CursorOrdering() {
    }

    /**
     * Explains why the ordering of the method is unusable for cursors.
     *
     * @param method     the repository method
     * @param identifier the name of the identifier attribute, or {@code null} when unknown
     * @return the problem, or {@code null} when the ordering is acceptable
     */
    static String problem(ExecutableElement method, String identifier) {
        List<String> ordering = ordering(method);
        if (ordering.isEmpty()) {
            return "a CursoredPage needs a deterministic ordering: declare @OrderBy attributes ending with the "
                    + "unique identifier";
        }
        if (identifier == null) {
            return "the identifier attribute of the entity cannot be established, so the ordering cannot be proven "
                    + "total";
        }
        String id = identifier.toLowerCase(Locale.ROOT);
        if (!ordering.contains(id)) {
            return "the ordering " + ordering + " is not provably unique: cursor pagination needs the unique "
                    + "identifier '" + id + "' among the ordered attributes, for instance @OrderBy(\"name\") "
                    + "@OrderBy(\"" + id + "\")";
        }
        return null;
    }

    private static List<String> ordering(ExecutableElement method) {
        List<String> attributes = new ArrayList<>();
        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            String name = Annotations.qualifiedName(mirror);
            if (name.equals(ORDER_BY)) {
                value(mirror).ifPresent(attributes::add);
            } else if (name.equals(ORDER_BY_LIST)) {
                for (AnnotationValue nested : annotationValues(mirror)) {
                    value((AnnotationMirror) nested.getValue()).ifPresent(attributes::add);
                }
            } else if (name.equals(QUERY)) {
                value(mirror).ifPresent(text -> attributes.addAll(queryOrdering(text)));
            }
        }
        String methodName = method.getSimpleName().toString();
        int index = methodName.indexOf("OrderBy");
        if (index >= 0) {
            for (String part : methodName.substring(index + "OrderBy".length()).split("Asc|Desc")) {
                if (!part.isEmpty()) {
                    attributes.add(part.toLowerCase(Locale.ROOT));
                }
            }
        }
        return attributes.stream().map(attribute -> attribute.toLowerCase(Locale.ROOT)).toList();
    }

    private static List<String> queryOrdering(String query) {
        int index = query.toUpperCase(Locale.ROOT).indexOf("ORDER BY");
        List<String> attributes = new ArrayList<>();
        if (index < 0) {
            return attributes;
        }
        for (String term : query.substring(index + "ORDER BY".length()).split(",")) {
            String attribute = term.trim().split("\\s+")[0];
            attributes.add(attribute.substring(attribute.lastIndexOf('.') + 1));
        }
        return attributes;
    }

    private static java.util.Optional<String> value(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals("value"))
                .map(entry -> entry.getValue().getValue()).filter(String.class::isInstance)
                .map(String.class::cast).findFirst();
    }

    @SuppressWarnings("unchecked")
    private static List<? extends AnnotationValue> annotationValues(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals("value"))
                .map(entry -> (List<? extends AnnotationValue>) entry.getValue().getValue())
                .findFirst().orElse(List.of());
    }
}
