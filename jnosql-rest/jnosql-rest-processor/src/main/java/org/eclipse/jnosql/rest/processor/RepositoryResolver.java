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

import org.eclipse.jnosql.rest.RestDTO;
import org.eclipse.jnosql.rest.RestRepository;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validates a {@code @RestRepository} interface and resolves its entity, identifier and configuration.
 */
final class RepositoryResolver {

    private static final String REPOSITORY = "jakarta.data.repository.Repository";
    private static final String DATA_REPOSITORY = "jakarta.data.repository.DataRepository";
    private static final String BASIC_REPOSITORY = "jakarta.data.repository.BasicRepository";

    private final Types types;
    private final Diagnostics diagnostics;

    RepositoryResolver(Types types, Diagnostics diagnostics) {
        this.types = types;
        this.diagnostics = diagnostics;
    }

    Optional<RepositoryInfo> resolve(TypeElement element) {
        int errorsBefore = diagnostics.errorCount();
        RestRepository rest = element.getAnnotation(RestRepository.class);
        if (element.getKind() != ElementKind.INTERFACE) {
            diagnostics.error(element, "@RestRepository can only be used on an interface, but "
                    + element.getSimpleName() + " is a " + element.getKind().toString().toLowerCase() + ".");
            return Optional.empty();
        }
        if (!Annotations.has(element, REPOSITORY)) {
            diagnostics.error(element, element.getSimpleName() + " must be annotated with "
                    + "@jakarta.data.repository.Repository so a CDI-managed repository exists to delegate to.");
        }
        if (rest.path().isBlank()) {
            diagnostics.error(element, "@RestRepository.path of " + element.getSimpleName() + " must not be blank.");
        }
        DeclaredType dataRepository = findSupertype(element.asType(), DATA_REPOSITORY);
        if (dataRepository == null || dataRepository.getTypeArguments().size() != 2) {
            diagnostics.error(element, element.getSimpleName() + " must extend BasicRepository, CrudRepository or "
                    + "DataRepository with explicit type arguments so the entity and identifier types can be resolved.");
            return Optional.empty();
        }
        if (diagnostics.errorCount() > errorsBefore) {
            return Optional.empty();
        }
        TypeMirror entity = dataRepository.getTypeArguments().get(0);
        if (entity.getKind() != TypeKind.DECLARED) {
            diagnostics.error(element, "The entity type of " + element.getSimpleName() + " must be a class or record.");
            return Optional.empty();
        }
        RestDTO dto = element.getAnnotation(RestDTO.class);
        Set<org.eclipse.jnosql.rest.RestOperation> excluded = rest.exclude().length == 0
                ? EnumSet.noneOf(org.eclipse.jnosql.rest.RestOperation.class)
                : EnumSet.copyOf(Arrays.asList(rest.exclude()));
        List<String> exclusions = dto == null ? List.of() : List.of(dto.exclude());
        return Optional.of(new RepositoryInfo(element, (DeclaredType) element.asType(), entity,
                dataRepository.getTypeArguments().get(1), findSupertype(element.asType(), BASIC_REPOSITORY) != null,
                normalizePath(rest.path()), rest.exposure(), excluded, dto != null, exclusions));
    }

    private static String normalizePath(String path) {
        String normalized = path.strip();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.length() > 1 && normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private DeclaredType findSupertype(TypeMirror type, String qualifiedName) {
        for (TypeMirror supertype : types.directSupertypes(type)) {
            if (supertype.getKind() != TypeKind.DECLARED) {
                continue;
            }
            DeclaredType declared = (DeclaredType) supertype;
            if (((TypeElement) declared.asElement()).getQualifiedName().contentEquals(qualifiedName)) {
                return declared;
            }
            DeclaredType found = findSupertype(supertype, qualifiedName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
