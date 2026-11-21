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
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Copies MicroProfile OpenAPI annotations from a repository, its methods and its parameters to the generated
 * resource. It is inactive when MicroProfile OpenAPI is not on the compilation classpath, so the generated source
 * never refers to OpenAPI types in that case.
 */
final class OpenApiPropagator {

    private static final String BASE = "org.eclipse.microprofile.openapi.annotations.";
    private static final String PROBE = BASE + "Operation";

    private static final Set<String> TYPE_ANNOTATIONS = Set.of(
            BASE + "tags.Tag",
            BASE + "tags.Tags",
            BASE + "security.SecurityRequirement",
            BASE + "security.SecurityRequirements");

    private static final Set<String> METHOD_ANNOTATIONS = Set.of(
            BASE + "Operation",
            BASE + "responses.APIResponse",
            BASE + "responses.APIResponses",
            BASE + "tags.Tag",
            BASE + "tags.Tags",
            BASE + "security.SecurityRequirement",
            BASE + "security.SecurityRequirements",
            BASE + "parameters.Parameter",
            BASE + "parameters.Parameters");

    private static final Set<String> QUERY_PARAMETER_ANNOTATIONS = Set.of(BASE + "parameters.Parameter");

    private static final Set<String> BODY_ANNOTATIONS = Set.of(BASE + "parameters.RequestBody");

    private final boolean available;

    OpenApiPropagator(Elements elements) {
        this.available = elements.getTypeElement(PROBE) != null;
    }

    boolean available() {
        return available;
    }

    List<AnnotationMirror> forType(Element repository) {
        return select(repository, TYPE_ANNOTATIONS);
    }

    List<AnnotationMirror> forMethod(Element method) {
        return select(method, METHOD_ANNOTATIONS);
    }

    List<AnnotationMirror> forQueryParameter(Element parameter) {
        return select(parameter, QUERY_PARAMETER_ANNOTATIONS);
    }

    List<AnnotationMirror> forBody(Element parameter) {
        return select(parameter, BODY_ANNOTATIONS);
    }

    private List<AnnotationMirror> select(Element element, Set<String> qualifiedNames) {
        List<AnnotationMirror> selected = new ArrayList<>();
        if (!available) {
            return selected;
        }
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (qualifiedNames.contains(Annotations.qualifiedName(mirror))) {
                selected.add(mirror);
            }
        }
        return selected;
    }
}
