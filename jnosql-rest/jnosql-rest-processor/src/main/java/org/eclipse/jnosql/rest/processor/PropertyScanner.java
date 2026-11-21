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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Discovers the attributes of an entity or DTO type through public getters, public fields and record components.
 * This is the access model of MapStruct, which performs the conversions of the generated mappers.
 * Attributes annotated with {@code @Transient} are ignored.
 */
final class PropertyScanner {

    private static final String OBJECT = "java.lang.Object";
    private static final Set<String> BOOLEANS = Set.of("boolean", "java.lang.Boolean");

    private final Elements elements;
    private final Types types;

    PropertyScanner(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    List<Property> scan(TypeElement type) {
        if (type.getKind() == ElementKind.RECORD) {
            return scanRecord(type);
        }
        Map<String, VariableElement> fields = fields(type);
        Map<String, Property> properties = new LinkedHashMap<>();
        DeclaredType declared = (DeclaredType) type.asType();
        for (ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers(type))) {
            String name = propertyName(method);
            if (name != null && !isTransient(name, method, fields)) {
                TypeMirror propertyType = ((ExecutableType) types.asMemberOf(declared, method)).getReturnType();
                boolean identifier = isIdentifier(name, method, fields);
                properties.putIfAbsent(name, new Property(name, propertyType, isWritable(type, name, fields), identifier));
            }
        }
        for (VariableElement field : ElementFilter.fieldsIn(elements.getAllMembers(type))) {
            String name = field.getSimpleName().toString();
            if (field.getModifiers().contains(Modifier.PUBLIC) && !field.getModifiers().contains(Modifier.STATIC)
                    && !isTransient(name, field, fields)) {
                boolean writable = !field.getModifiers().contains(Modifier.FINAL);
                properties.putIfAbsent(name, new Property(name, types.asMemberOf(declared, field), writable,
                        isIdentifier(name, field, fields)));
            }
        }
        return sorted(properties, fields);
    }

    private List<Property> scanRecord(TypeElement type) {
        List<Property> properties = new ArrayList<>();
        for (RecordComponentElement component : type.getRecordComponents()) {
            String name = component.getSimpleName().toString();
            boolean identifier = Annotations.hasSimpleName(component, "Id") || "id".equals(name);
            properties.add(new Property(name, component.asType(), true, identifier));
        }
        return properties;
    }

    private List<Property> sorted(Map<String, Property> properties, Map<String, VariableElement> fields) {
        List<String> declarationOrder = new ArrayList<>(fields.keySet());
        List<Property> result = new ArrayList<>(properties.values());
        result.sort(Comparator
                .comparingInt((Property property) -> position(declarationOrder, property.name()))
                .thenComparing(Property::name));
        return result;
    }

    private static int position(List<String> declarationOrder, String name) {
        int index = declarationOrder.indexOf(name);
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private Map<String, VariableElement> fields(TypeElement type) {
        List<TypeElement> hierarchy = new ArrayList<>();
        for (TypeElement current = type; current != null; current = superclass(current)) {
            hierarchy.add(0, current);
        }
        Map<String, VariableElement> fields = new LinkedHashMap<>();
        for (TypeElement current : hierarchy) {
            for (VariableElement field : ElementFilter.fieldsIn(current.getEnclosedElements())) {
                if (!field.getModifiers().contains(Modifier.STATIC)) {
                    fields.put(field.getSimpleName().toString(), field);
                }
            }
        }
        return fields;
    }

    private TypeElement superclass(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        if (superclass.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TypeElement element = (TypeElement) ((DeclaredType) superclass).asElement();
        return element.getQualifiedName().contentEquals(OBJECT) ? null : element;
    }

    private String propertyName(ExecutableElement method) {
        Set<Modifier> modifiers = method.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.STATIC)
                || !method.getParameters().isEmpty() || method.getReturnType().getKind() == TypeKind.VOID
                || ((TypeElement) method.getEnclosingElement()).getQualifiedName().contentEquals(OBJECT)) {
            return null;
        }
        String name = method.getSimpleName().toString();
        if (name.startsWith("get") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is") && name.length() > 2 && BOOLEANS.contains(method.getReturnType().toString())) {
            return decapitalize(name.substring(2));
        }
        return null;
    }

    private boolean isWritable(TypeElement type, String name, Map<String, VariableElement> fields) {
        String setter = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        boolean hasSetter = ElementFilter.methodsIn(elements.getAllMembers(type)).stream()
                .anyMatch(method -> method.getSimpleName().contentEquals(setter)
                        && method.getParameters().size() == 1
                        && method.getModifiers().contains(Modifier.PUBLIC)
                        && !method.getModifiers().contains(Modifier.STATIC));
        VariableElement field = fields.get(name);
        boolean publicField = field != null && field.getModifiers().contains(Modifier.PUBLIC)
                && !field.getModifiers().contains(Modifier.FINAL);
        return hasSetter || publicField;
    }

    private static boolean isTransient(String name, Element member, Map<String, VariableElement> fields) {
        VariableElement field = fields.get(name);
        return Annotations.hasSimpleName(member, "Transient")
                || (field != null && Annotations.hasSimpleName(field, "Transient"));
    }

    private static boolean isIdentifier(String name, Element member, Map<String, VariableElement> fields) {
        VariableElement field = fields.get(name);
        return Annotations.hasSimpleName(member, "Id")
                || (field != null && Annotations.hasSimpleName(field, "Id"))
                || (fields.values().stream().noneMatch(candidate -> Annotations.hasSimpleName(candidate, "Id"))
                && "id".equals(name));
    }

    private static String decapitalize(String text) {
        if (text.length() > 1 && Character.isUpperCase(text.charAt(0)) && Character.isUpperCase(text.charAt(1))) {
            return text;
        }
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }
}
