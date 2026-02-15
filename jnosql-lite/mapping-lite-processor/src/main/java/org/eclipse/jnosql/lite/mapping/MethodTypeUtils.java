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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
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

    private static final MethodPattern FIND_BY =
            MethodPattern.of("find", RepositoryMethodType.FIND_BY);

    private static final MethodPattern DELETE_BY =
            MethodPattern.of("delete", RepositoryMethodType.DELETE_BY);

    private static final MethodPattern COUNT_ALL =
            MethodPattern.of("countAll", RepositoryMethodType.COUNT_ALL);

    private static final MethodPattern COUNT_BY =
            MethodPattern.of("count", RepositoryMethodType.COUNT_BY);

    private static final MethodPattern EXISTS_BY =
            MethodPattern.of("exists", RepositoryMethodType.EXISTS_BY);

    private static final List<MethodPattern> METHOD_PATTERNS =
            List.of(FIND_BY, DELETE_BY, COUNT_ALL, COUNT_BY, EXISTS_BY);

    private static final String FIND_ALL = "findAll";
    private static final String JAKARTA_DATA_PAGE_CURSORED_PAGE = "jakarta.data.page.CursoredPage";

    public RepositoryMethodType type(Element method, ProcessingEnvironment processingEnv) {

        ExecutableElement executableElement = (ExecutableElement) method;

        if(executableElement.isDefault()) {
            return RepositoryMethodType.DEFAULT_METHOD;
        }

        var returnType = executableElement.getReturnType();

        if (isCursorMethod(returnType, processingEnv)) {
            return RepositoryMethodType.CURSOR_PAGINATION;
        }

        Predicate<MethodOperation> hasAnnotation =
                op -> method.getAnnotation(op.annotation()) != null;

        Optional<RepositoryMethodType> annotationMatch = OPERATION_ANNOTATIONS.stream()
                .filter(hasAnnotation)
                .map(MethodOperation::type)
                .findFirst();

        if (FIND_ALL.equals(method.getSimpleName().toString())) {
            return RepositoryMethodType.FIND_ALL;
        }

        return annotationMatch.orElseGet(() -> METHOD_PATTERNS.stream()
                .filter(pattern -> method.getSimpleName().toString().startsWith(pattern.keyword()))
                .findFirst()
                .map(MethodPattern::type)
                .orElse(RepositoryMethodType.UNKNOWN));

    }

    private static boolean isCursorMethod(TypeMirror returnType, ProcessingEnvironment processingEnv) {
        TypeElement returnElement = (TypeElement) processingEnv.getTypeUtils().asElement(returnType);
        return returnElement != null && JAKARTA_DATA_PAGE_CURSORED_PAGE.equals(returnElement.toString());
    }

    private record MethodOperation(Class<? extends Annotation> annotation,
                                   RepositoryMethodType type) {
        static MethodOperation of(Class<? extends Annotation> annotation, RepositoryMethodType type) {
            return new MethodOperation(annotation, type);
        }
    }

    private record MethodPattern(String keyword, RepositoryMethodType type) {
        static MethodPattern of(String keyword, RepositoryMethodType type) {
            return new MethodPattern(keyword, type);
        }
    }
}
