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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plans the generated search request of a repository: the entity attributes that can be filtered on, their
 * Jakarta Data attribute bindings and the filter records shared by attributes of the same type.
 */
final class SearchPlanner {

    private static final String BOOLEAN = "java.lang.Boolean";

    private final Elements elements;
    private final Types types;
    private final Diagnostics diagnostics;
    private final PropertyScanner scanner;

    SearchPlanner(Elements elements, Types types, Diagnostics diagnostics) {
        this.elements = elements;
        this.types = types;
        this.diagnostics = diagnostics;
        this.scanner = new PropertyScanner(elements, types);
    }

    static String requestName(RepositoryInfo repository) {
        return repository.element().getSimpleName() + "SearchRequest";
    }

    /**
     * Plans the search request of the repository.
     *
     * @return the specification, or {@code null} after an error was reported
     */
    SearchSpec plan(RepositoryInfo repository, boolean paginated) {
        TypeElement entity = (TypeElement) ((DeclaredType) repository.entity()).asElement();
        ImportRegistry imports = new ImportRegistry(repository.packageName());
        imports.name("java.util.List");
        imports.name("java.util.ArrayList");
        imports.name("jakarta.ws.rs.BadRequestException");
        imports.name("jakarta.data.restrict.Restriction");
        imports.name("java.util.Objects");
        if (paginated) {
            imports.name("org.eclipse.jnosql.rest.pagination.PaginationRequest");
        }
        imports.name("jakarta.data.restrict.Restrict");
        imports.name("jakarta.data.metamodel.BasicAttribute");
        imports.name("jakarta.data.metamodel.ComparableAttribute");
        String entityType = imports.name(entity);
        List<SearchAttribute> attributes = new ArrayList<>();
        Map<String, SearchFilter> filters = new LinkedHashMap<>();
        Map<String, String> filterOwners = new LinkedHashMap<>();
        for (Property property : scanner.scan(entity)) {
            if (repository.dtoExclusions().contains(property.name()) || !ScalarTypes.isScalar(property.type())) {
                continue;
            }
            TypeMirror value = property.type().getKind().isPrimitive()
                    ? types.boxedClass((javax.lang.model.type.PrimitiveType) property.type()).asType()
                    : property.type();
            String valueType = imports.render(value);
            String qualified = ((TypeElement) ((DeclaredType) value).asElement()).getQualifiedName().toString();
            boolean comparable = !qualified.equals(BOOLEAN) && types.isAssignable(types.erasure(value),
                    types.erasure(elements.getTypeElement("java.lang.Comparable").asType()));
            String filterName = valueType.substring(valueType.lastIndexOf('.') + 1) + "Filter";
            String owner = filterOwners.putIfAbsent(filterName, qualified);
            if (owner != null && !owner.equals(qualified)) {
                diagnostics.error(repository.element(), "The attributes of " + entity.getQualifiedName()
                        + " use two types named " + filterName.replace("Filter", "") + " (" + owner + " and "
                        + qualified + "), which the generated search request cannot distinguish.");
                return null;
            }
            String attribute = imports.name("jakarta.data.metamodel."
                    + (comparable ? "ComparableAttribute" : "BasicAttribute"));
            String attributeType = attribute + "<" + entityType + ", " + valueType + ">";
            filters.putIfAbsent(filterName, new SearchFilter(filterName, valueType, attributeType, comparable));
            attributes.add(new SearchAttribute(constant(property.name()), property.name(), filterName,
                    attributeType, attribute + ".of(" + entityType + ".class, \"" + property.name() + "\", "
                    + valueType + ".class)"));
        }
        if (attributes.isEmpty()) {
            diagnostics.error(repository.element(), "Entity " + entity.getQualifiedName() + " has no attribute "
                    + "that can be used in a search request after applying @RestDTO exclusions.");
            return null;
        }
        return new SearchSpec(repository.packageName(), requestName(repository),
                "Search criteria of {@link " + repository.element().getSimpleName() + "}: each property filters one "
                        + "attribute with operators that are combined with AND; {@code or} lists alternatives.",
                entityType, imports.imports(), attributes, new ArrayList<>(filters.values()), repository.element(),
                paginated);
    }

    private static String constant(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
    }
}
