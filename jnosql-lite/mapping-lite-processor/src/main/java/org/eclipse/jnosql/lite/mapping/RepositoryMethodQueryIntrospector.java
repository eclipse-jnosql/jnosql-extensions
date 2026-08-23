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

import jakarta.data.repository.Find;
import jakarta.data.repository.First;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Select;

import javax.lang.model.element.Element;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;

final class RepositoryMethodQueryIntrospector {

    private static final String OPTIONAL_EMPTY = "Optional.empty()";
    private static final int FIND_INITIAL_SUBSTRING = 30;
    private static final String FIND_LAST_SUBSTRING = ")";
    private static final String SORT_DESC_MASK = "Sort.desc(\"%s\")";
    private static final String SORT_ASC_MASK = "Sort.asc(\"%s\")";

    private final Element method;

    RepositoryMethodQueryIntrospector(Element method) {
        this.method = method;
    }

    QueryMetadata introspect() {
        return new QueryMetadata(query(), find(), first(), selects(), sorts());
    }

    private String query() {
        return ofNullable(method.getAnnotation(Query.class))
                .map(Query::value)
                .map("Optional.of(\"%s\")"::formatted)
                .orElse(OPTIONAL_EMPTY);
    }

    private String find() {
        return ofNullable(method.getAnnotation(Find.class))
                .map(Find::toString)
                .map(value -> value.substring(FIND_INITIAL_SUBSTRING, value.lastIndexOf(FIND_LAST_SUBSTRING)))
                .map("Optional.of(%s)"::formatted)
                .orElse(OPTIONAL_EMPTY);
    }

    private String first() {
        return ofNullable(method.getAnnotation(First.class))
                .map(First::value)
                .map("OptionalInt.of(%d)"::formatted)
                .orElse("OptionalInt.empty()");
    }

    private List<String> selects() {
        return Arrays.stream(method.getAnnotationsByType(Select.class))
                .map(Select::value)
                .toList();
    }

    private List<String> sorts() {
        return Arrays.stream(method.getAnnotationsByType(OrderBy.class))
                .map(orderBy -> orderBy.descending()
                        ? SORT_DESC_MASK.formatted(orderBy.value())
                        : SORT_ASC_MASK.formatted(orderBy.value()))
                .toList();
    }

    record QueryMetadata(String query, String find, String first, List<String> selects, List<String> sorts) {
    }
}
