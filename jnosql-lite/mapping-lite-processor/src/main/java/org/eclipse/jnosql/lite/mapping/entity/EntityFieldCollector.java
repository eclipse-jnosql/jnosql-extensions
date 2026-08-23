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
package org.eclipse.jnosql.lite.mapping.entity;

import jakarta.nosql.MappedSuperclass;
import org.eclipse.jnosql.lite.mapping.entity.field.FieldAnalyzer;
import org.eclipse.jnosql.lite.mapping.processing.ElementPredicates;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.stream.Stream;

final class EntityFieldCollector {

    private final ProcessingEnvironment processingEnv;

    EntityFieldCollector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    List<String> collect(TypeElement entity) {
        TypeElement superclass =
                (TypeElement) ((DeclaredType) entity.getSuperclass()).asElement();
        Stream<? extends Element> inheritedFields = Stream.empty();
        if (superclass.getAnnotation(MappedSuperclass.class) != null) {
            inheritedFields = processingEnv.getElementUtils()
                    .getAllMembers(superclass)
                    .stream();
        }
        Stream<? extends Element> entityFields = processingEnv.getElementUtils()
                .getAllMembers(entity)
                .stream();

        return Stream.concat(entityFields, inheritedFields)
                .filter(ElementPredicates.IS_FIELD.and(ElementPredicates.HAS_MAPPING_ANNOTATION))
                .map(field -> new FieldAnalyzer(field, processingEnv, entity))
                .map(FieldAnalyzer::get)
                .toList();
    }
}
