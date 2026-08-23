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

import jakarta.nosql.DiscriminatorColumn;
import jakarta.nosql.DiscriminatorValue;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;
import jakarta.nosql.Inheritance;
import org.eclipse.jnosql.lite.mapping.processing.ProcessorUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

final class EntityModelIntrospector {

    EntityModel introspect(TypeElement element, List<String> fields, String constructorClassName) {
        TypeElement superclass =
                (TypeElement) ((DeclaredType) element.getSuperclass()).asElement();
        Entity annotation = element.getAnnotation(Entity.class);
        boolean entityAnnotation = annotation != null;
        boolean embedded = element.getAnnotation(Embeddable.class) != null;
        boolean hasInheritanceAnnotation = element.getAnnotation(Inheritance.class) != null;
        String packageName = ProcessorUtils.packageName(element);
        String sourceClassName = ProcessorUtils.simpleName(element);

        String entityName = Optional.ofNullable(annotation)
                .map(Entity::value)
                .filter(value -> !value.isBlank())
                .orElse(sourceClassName);
        String mappingName = Optional.ofNullable(annotation)
                .map(Entity::name)
                .filter(value -> !value.isBlank())
                .orElse(sourceClassName);
        String inheritanceParameter = null;
        boolean notConcrete = element.getModifiers().contains(Modifier.ABSTRACT)
                || !element.getRecordComponents().isEmpty();

        if (superclass.getAnnotation(Inheritance.class) != null) {
            inheritanceParameter = inheritanceParameter(element, superclass);
            Entity superEntity = superclass.getAnnotation(Entity.class);
            entityName = superEntity.value().isBlank()
                    ? ProcessorUtils.simpleName(superclass)
                    : annotation.value();
        } else if (hasInheritanceAnnotation) {
            inheritanceParameter = inheritanceParameter(element, element);
        }

        return new EntityModel(
                packageName,
                sourceClassName,
                entityName,
                mappingName,
                fields,
                embedded,
                notConcrete,
                inheritanceParameter,
                entityAnnotation,
                hasInheritanceAnnotation,
                constructorClassName);
    }

    private static String inheritanceParameter(TypeElement element, TypeElement superclass) {
        String discriminatorColumn = Optional
                .ofNullable(superclass.getAnnotation(DiscriminatorColumn.class))
                .map(DiscriminatorColumn::value)
                .orElse(DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN);
        String discriminatorValue = Optional
                .ofNullable(element.getAnnotation(DiscriminatorValue.class))
                .map(DiscriminatorValue::value)
                .orElse(element.getSimpleName().toString());

        return new StringJoiner(",\n")
                .add("\"" + discriminatorValue + "\"")
                .add("\"" + discriminatorColumn + "\"")
                .add(superclass.getQualifiedName() + ".class")
                .add(element.getQualifiedName() + ".class")
                .toString();
    }
}
