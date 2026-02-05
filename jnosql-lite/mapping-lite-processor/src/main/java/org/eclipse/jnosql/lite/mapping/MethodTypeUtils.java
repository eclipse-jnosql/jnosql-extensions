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

import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

enum MethodTypeUtils {
    INSTANCE;

    private static final MethodOperation INSERT =
            MethodOperation.of(Insert.class, RepositoryMethodType.INSERT);

    private static final MethodOperation SAVE =
            MethodOperation.of(Save.class, RepositoryMethodType.SAVE);

    private static final MethodOperation DELETE =
            MethodOperation.of(Delete.class, RepositoryMethodType.DELETE);

    private static final MethodOperation UPDATE =
            MethodOperation.of(Update.class, RepositoryMethodType.UPDATE);

    private static final MethodOperation QUERY =
            MethodOperation.of(Query.class, RepositoryMethodType.QUERY);

    private static final MethodOperation FIND_QUERY =
            MethodOperation.of(Find.class, RepositoryMethodType.PARAMETER_BASED);

    private static final Set<MethodOperation> OPERATION_ANNOTATIONS =
            Set.of(INSERT, SAVE, DELETE, UPDATE, QUERY, FIND_QUERY);

    public RepositoryMethodType type(Element method) {

        Predicate<MethodOperation> hasAnnotation =
                op -> method.getAnnotation(op.annotation()) != null;
        Optional<RepositoryMethodType> annotationMatch = OPERATION_ANNOTATIONS.stream()
                .filter(hasAnnotation)
                .map(MethodOperation::type)
                .findFirst();

        return annotationMatch.orElse(RepositoryMethodType.UNKNOWN);

    }

    private record MethodOperation(Class<? extends Annotation> annotation,
                                   RepositoryMethodType type) {
        static MethodOperation of(Class<? extends Annotation> annotation, RepositoryMethodType type) {
            return new MethodOperation(annotation, type);
        }
    }
}
