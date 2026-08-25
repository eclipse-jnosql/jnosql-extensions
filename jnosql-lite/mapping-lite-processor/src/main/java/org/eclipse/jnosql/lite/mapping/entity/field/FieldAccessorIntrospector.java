/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entity.field;

import org.eclipse.jnosql.lite.mapping.processing.ElementPredicates;
import org.eclipse.jnosql.lite.mapping.processing.ProcessorUtils;
import org.eclipse.jnosql.lite.mapping.processing.ProcessorValidationException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.function.Predicate;

final class FieldAccessorIntrospector {

    private static final Predicate<Element> IS_METHOD = element -> element.getKind() == ElementKind.METHOD;

    private final Element field;
    private final ProcessingEnvironment processingEnv;
    private final TypeElement entity;

    FieldAccessorIntrospector(Element field, ProcessingEnvironment processingEnv, TypeElement entity) {
        this.field = field;
        this.processingEnv = processingEnv;
        this.entity = entity;
    }

    AccessorMetadata introspect() {
        String fieldName = field.getSimpleName().toString();
        String capitalizedName = ProcessorUtils.capitalize(fieldName);
        Predicate<Element> validName = element -> element.getSimpleName().toString().contains(capitalizedName);

        var accessors = processingEnv.getElementUtils()
                .getAllMembers(entity).stream()
                .filter(validName.and(IS_METHOD).and(ElementPredicates.HAS_ACCESS))
                .map(element -> element.getSimpleName().toString())
                .toList();

        String reader = entity.getRecordComponents().isEmpty()
                ? accessors.stream()
                .filter(name -> name.equals("get" + capitalizedName) || name.equals(fieldName))
                .findFirst()
                .orElseThrow(() -> missingGetter(fieldName))
                : fieldName;
        String writer = accessors.stream()
                .filter(name -> name.equals("set" + capitalizedName) || name.equals("is" + capitalizedName))
                .findFirst()
                .orElse(null);

        return new AccessorMetadata(reader, writer);
    }

    private ProcessorValidationException missingGetter(String fieldName) {
        String packageName = ProcessorUtils.packageName(entity);
        String entityName = ProcessorUtils.simpleName(entity);
        return new ProcessorValidationException("There is not valid getter method to the field: "
                + fieldName + " in the class: " + packageName + "." + entityName);
    }

    record AccessorMetadata(String reader, String writer) {
    }
}
