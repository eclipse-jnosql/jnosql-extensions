/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import org.eclipse.jnosql.mapping.NoSQLRepository;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class RepositoryUtil {

    static final Predicate<String> IS_NOSQL_REPOSITORY = q -> NoSQLRepository.class.getName().equals(q);
    static final Predicate<String> IS_CRUD_REPOSITORY = q -> CrudRepository.class.getName().equals(q);
    static final Predicate<String> IS_BASIC_REPOSITORY = q -> BasicRepository.class.getName().equals(q);
    static final Predicate<String> IS_JAKARTA_DATA_REPOSITORY = IS_NOSQL_REPOSITORY.or(IS_CRUD_REPOSITORY).or(IS_BASIC_REPOSITORY);

    private RepositoryUtil() {
    }

    static Optional<TypeMirror> findRepository(List<? extends TypeMirror> interfaces,
                                               ProcessingEnvironment processingEnv) {
        for (TypeMirror mirror : interfaces) {
            TypeElement element = (TypeElement) processingEnv.getTypeUtils().asElement(mirror);
            if (IS_JAKARTA_DATA_REPOSITORY.test(element.getQualifiedName().toString())) {
                return Optional.of(mirror);
            }
        }
        return Optional.empty();
    }

    static List<String> findParameters(TypeMirror repository) {
        if (repository instanceof DeclaredType declaredType) {
            return declaredType.getTypeArguments().stream()
                    .map(TypeMirror::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    static boolean isNoSQLRepository(List<? extends TypeMirror> interfaces, ProcessingEnvironment processingEnv) {
        return interfaces.stream()
                .map(processingEnv.getTypeUtils()::asElement)
                .map(TypeElement.class::cast)
                .map(t -> t.getQualifiedName().toString())
                .anyMatch(IS_NOSQL_REPOSITORY);
    }

    static boolean isCrudRepository(List<? extends TypeMirror> interfaces, ProcessingEnvironment processingEnv) {
        return isNoSQLRepository(interfaces, processingEnv) ||
                interfaces.stream()
                .map(processingEnv.getTypeUtils()::asElement)
                .map(TypeElement.class::cast)
                .map(t -> t.getQualifiedName().toString())
                .anyMatch(IS_CRUD_REPOSITORY);
    }
}
