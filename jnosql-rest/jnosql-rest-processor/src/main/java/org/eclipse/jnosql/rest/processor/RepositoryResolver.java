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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Validates a {@code @RestRepository} interface and resolves its entity, identifier and configuration.
 */
final class RepositoryResolver {

    private static final String REPOSITORY = "jakarta.data.repository.Repository";
    private static final String DATA_REPOSITORY = "jakarta.data.repository.DataRepository";
    private static final String BASIC_REPOSITORY = "jakarta.data.repository.BasicRepository";

    private static final Set<String> WRAPPERS = Set.of("java.util.List", "java.util.Set", "java.util.Collection",
            "java.util.Optional", "java.util.stream.Stream", "jakarta.data.page.Page",
            "jakarta.data.page.CursoredPage");

    private final Elements elements;
    private final Types types;
    private final Diagnostics diagnostics;

    RepositoryResolver(Elements elements, Types types, Diagnostics diagnostics) {
        this.elements = elements;
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
        if (elements.getPackageOf(element).isUnnamed()) {
            diagnostics.error(element, element.getSimpleName() + " is in the unnamed package; move it to a named "
                    + "package so the generated REST types can be placed next to it.");
            return Optional.empty();
        }
        DeclaredType dataRepository = findSupertype(element.asType(), DATA_REPOSITORY);
        if (dataRepository != null && dataRepository.getTypeArguments().size() != 2) {
            diagnostics.error(element, element.getSimpleName() + " must declare explicit type arguments when "
                    + "extending BasicRepository, CrudRepository or DataRepository.");
            return Optional.empty();
        }
        TypeMirror entity = dataRepository == null ? inferEntity(element) : dataRepository.getTypeArguments().get(0);
        TypeMirror id = dataRepository == null ? elements.getTypeElement("java.lang.Object").asType()
                : dataRepository.getTypeArguments().get(1);
        if (diagnostics.errorCount() > errorsBefore) {
            return Optional.empty();
        }
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
                id, findSupertype(element.asType(), BASIC_REPOSITORY) != null,
                normalizePath(rest.path()), rest.exposure(), excluded, dto != null, exclusions));
    }

    private TypeMirror inferEntity(TypeElement element) {
        Map<String, TypeMirror> candidates = new LinkedHashMap<>();
        DeclaredType declared = (DeclaredType) element.asType();
        for (ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers(element))) {
            if (method.getEnclosingElement().toString().equals("java.lang.Object")) {
                continue;
            }
            javax.lang.model.type.ExecutableType resolved =
                    (javax.lang.model.type.ExecutableType) types.asMemberOf(declared, method);
            candidate(resolved.getReturnType(), candidates);
            resolved.getParameterTypes().forEach(parameter -> candidate(parameter, candidates));
        }
        if (candidates.size() != 1) {
            diagnostics.error(element, "The entity of " + element.getSimpleName() + " cannot be inferred ("
                    + (candidates.isEmpty() ? "no method uses an entity type" : "candidates: " + candidates.keySet())
                    + "). Extend BasicRepository or CrudRepository, or make every method use a single entity type.");
            return null;
        }
        return candidates.values().iterator().next();
    }

    private void candidate(TypeMirror type, Map<String, TypeMirror> candidates) {
        if (type.getKind() != TypeKind.DECLARED) {
            return;
        }
        DeclaredType declared = (DeclaredType) type;
        TypeElement owner = (TypeElement) declared.asElement();
        String name = owner.getQualifiedName().toString();
        if (WRAPPERS.contains(name) && declared.getTypeArguments().size() == 1) {
            candidate(declared.getTypeArguments().get(0), candidates);
        } else if (!ScalarTypes.isScalar(type) && !name.startsWith("java.") && !name.startsWith("jakarta.data.")
                && !name.startsWith("jakarta.nosql.") && declared.getTypeArguments().isEmpty()) {
            candidates.putIfAbsent(name, type);
        }
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
