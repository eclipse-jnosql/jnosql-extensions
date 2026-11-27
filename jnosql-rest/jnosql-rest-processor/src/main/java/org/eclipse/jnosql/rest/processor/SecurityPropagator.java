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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Copies the Jakarta Annotations authorization annotations {@code @RolesAllowed}, {@code @PermitAll} and
 * {@code @DenyAll} from a repository or one of its methods to the generated resource. The annotations are matched
 * by qualified name, so nothing is selected, and the generated source never refers to them, unless the developer
 * declared them. No authorization policy is added or evaluated here.
 */
final class SecurityPropagator {

    private static final String BASE = "jakarta.annotation.security.";

    private static final Set<String> ANNOTATIONS = Set.of(BASE + "RolesAllowed", BASE + "PermitAll",
            BASE + "DenyAll");

    private final Diagnostics diagnostics;

    SecurityPropagator(Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * Selects the authorization annotations declared on an element.
     *
     * @param element the repository or repository method
     * @return the annotations to copy, empty when none is declared or when they conflict, in which case an error
     * is reported on the element
     */
    List<AnnotationMirror> select(Element element) {
        List<AnnotationMirror> selected = new ArrayList<>();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (ANNOTATIONS.contains(Annotations.qualifiedName(mirror))) {
                selected.add(mirror);
            }
        }
        if (selected.size() > 1) {
            List<String> names = selected.stream()
                    .map(mirror -> "@" + mirror.getAnnotationType().asElement().getSimpleName()).toList();
            diagnostics.error(element, "Conflicting authorization annotations on " + element.getSimpleName() + ": "
                    + String.join(", ", names) + ". Declare only one of @RolesAllowed, @PermitAll or @DenyAll.");
            return List.of();
        }
        return selected;
    }
}
