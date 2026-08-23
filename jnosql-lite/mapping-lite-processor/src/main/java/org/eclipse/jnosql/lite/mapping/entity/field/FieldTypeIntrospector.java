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
package org.eclipse.jnosql.lite.mapping.entity.field;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.lite.mapping.processing.CollectionUtil;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

final class FieldTypeIntrospector {

    private static final String NULL = "null";

    private final Element field;

    FieldTypeIntrospector(Element field) {
        this.field = field;
    }

    TypeMetadata introspect() {
        TypeMirror type = field.asType();
        if (type instanceof DeclaredType declaredType) {
            return declaredType(declaredType);
        }
        if (type instanceof ArrayType arrayType) {
            return arrayType(arrayType);
        }
        return new TypeMetadata(
                type.toString(),
                NULL,
                NULL,
                false,
                MappingType.DEFAULT,
                CollectionUtil.DEFAULT,
                null,
                null,
                null);
    }

    private static TypeMetadata declaredType(DeclaredType declaredType) {
        Element element = declaredType.asElement();
        String type = element.toString();
        String collectionInstance = CollectionUtil.INSTANCE.apply(type);
        return new TypeMetadata(
                type,
                typeArgument(declaredType, 0),
                typeArgument(declaredType, 1),
                isEmbeddable(declaredType),
                mappingType(element, collectionInstance),
                collectionInstance,
                declaredType.toString(),
                null,
                null);
    }

    private static TypeMetadata arrayType(ArrayType arrayType) {
        TypeMirror componentType = arrayType.getComponentType();
        String type = arrayType.toString();
        String elementType = componentType + ".class";
        String arrayElement = componentType.toString();
        boolean embeddable = false;

        if (componentType instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            elementType = element + ".class";
            arrayElement = element.toString();
            embeddable = element.getAnnotation(Entity.class) != null
                    || element.getAnnotation(Embeddable.class) != null;
        }

        return new TypeMetadata(
                type,
                elementType,
                NULL,
                embeddable,
                MappingType.ARRAY,
                CollectionUtil.DEFAULT,
                componentType instanceof DeclaredType ? componentType.toString() : type,
                type.replace("[]", "[collection.size()]"),
                arrayElement);
    }

    private static String typeArgument(DeclaredType declaredType, int index) {
        return declaredType.getTypeArguments().stream()
                .skip(index)
                .findFirst()
                .map(type -> type + ".class")
                .orElse(NULL);
    }

    private static boolean isEmbeddable(DeclaredType declaredType) {
        return declaredType.getTypeArguments().stream()
                .filter(DeclaredType.class::isInstance)
                .map(DeclaredType.class::cast)
                .map(DeclaredType::asElement)
                .findFirst()
                .map(element -> element.getAnnotation(Embeddable.class) != null
                        || element.getAnnotation(Entity.class) != null)
                .orElse(false);
    }

    private static MappingType mappingType(Element element, String collection) {
        var embeddable = element.getAnnotation(Embeddable.class);
        if (embeddable != null) {
            return Embeddable.EmbeddableType.FLAT.equals(embeddable.value())
                    ? MappingType.EMBEDDED
                    : MappingType.EMBEDDED_GROUP;
        }
        if (element.getAnnotation(Entity.class) != null) {
            return MappingType.ENTITY;
        }
        if (!collection.equals(CollectionUtil.DEFAULT)) {
            return MappingType.COLLECTION;
        }
        if (element.toString().equals("java.util.Map")) {
            return MappingType.MAP;
        }
        return MappingType.DEFAULT;
    }

    record TypeMetadata(String type,
                        String elementType,
                        String valueType,
                        boolean embeddable,
                        MappingType mappingType,
                        String collectionInstance,
                        String supplierElement,
                        String newArrayInstance,
                        String arrayElement) {
    }
}
