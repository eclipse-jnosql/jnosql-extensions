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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Renders types as Java source for one generated class and collects the imports it needs.
 * A type is imported when its simple name is free; otherwise its qualified name is used.
 */
final class ImportRegistry {

    private static final String JAVA_LANG = "java.lang";

    private final String packageName;
    private final Map<String, String> qualifiedBySimpleName = new HashMap<>();
    private final TreeSet<String> imports = new TreeSet<>();

    ImportRegistry(String packageName) {
        this.packageName = packageName;
    }

    String name(TypeElement element) {
        Element enclosing = element.getEnclosingElement();
        if (enclosing instanceof TypeElement outer) {
            return name(outer) + "." + element.getSimpleName();
        }
        return name(element.getQualifiedName().toString());
    }

    /**
     * Registers a top-level type given by its qualified name and returns the name to print.
     */
    String name(String qualifiedName) {
        int dot = qualifiedName.lastIndexOf('.');
        String simpleName = qualifiedName.substring(dot + 1);
        String typePackage = dot < 0 ? "" : qualifiedName.substring(0, dot);
        String registered = qualifiedBySimpleName.putIfAbsent(simpleName, qualifiedName);
        if (registered != null && !registered.equals(qualifiedName)) {
            return qualifiedName;
        }
        if (!typePackage.equals(packageName) && !typePackage.equals(JAVA_LANG) && !typePackage.isEmpty()) {
            imports.add(qualifiedName);
        }
        return simpleName;
    }

    String render(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            return renderDeclared((DeclaredType) type);
        }
        if (type.getKind() == TypeKind.ARRAY) {
            return render(((ArrayType) type).getComponentType()) + "[]";
        }
        if (type.getKind() == TypeKind.WILDCARD) {
            return renderWildcard((WildcardType) type);
        }
        return type.toString();
    }

    List<String> imports() {
        return new ArrayList<>(imports);
    }

    private String renderDeclared(DeclaredType type) {
        String name = name((TypeElement) type.asElement());
        if (type.getTypeArguments().isEmpty()) {
            return name;
        }
        List<String> arguments = new ArrayList<>();
        for (TypeMirror argument : type.getTypeArguments()) {
            arguments.add(render(argument));
        }
        return name + "<" + String.join(", ", arguments) + ">";
    }

    private String renderWildcard(WildcardType type) {
        if (type.getExtendsBound() != null) {
            return "? extends " + render(type.getExtendsBound());
        }
        if (type.getSuperBound() != null) {
            return "? super " + render(type.getSuperBound());
        }
        return "?";
    }
}
