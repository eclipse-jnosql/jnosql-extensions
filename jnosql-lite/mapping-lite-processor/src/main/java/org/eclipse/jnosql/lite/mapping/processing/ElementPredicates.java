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
package org.eclipse.jnosql.lite.mapping.processing;

import jakarta.nosql.Column;
import jakarta.nosql.Id;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.EnumSet;
import java.util.function.Predicate;

import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class ElementPredicates {

    private static final EnumSet<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);
    private static final Predicate<VariableElement> ID_PARAMETER =
            parameter -> parameter.getAnnotation(Id.class) != null;
    private static final Predicate<VariableElement> COLUMN_PARAMETER =
            parameter -> parameter.getAnnotation(Column.class) != null;

    public static final Predicate<Element> IS_CONSTRUCTOR =
            element -> element.getKind() == ElementKind.CONSTRUCTOR;
    public static final Predicate<Element> HAS_EXPLICIT_ACCESS =
            element -> element.getModifiers().stream().anyMatch(ACCESS_MODIFIERS::contains);
    public static final Predicate<Element> HAS_DEFAULT_ACCESS =
            element -> element.getModifiers().isEmpty();
    public static final Predicate<Element> HAS_ACCESS =
            HAS_EXPLICIT_ACCESS.or(HAS_DEFAULT_ACCESS);
    public static final Predicate<Element> HAS_COLUMN_ANNOTATION =
            element -> element.getAnnotation(Column.class) != null;
    public static final Predicate<Element> HAS_ID_ANNOTATION =
            element -> element.getAnnotation(Id.class) != null;
    public static final Predicate<Element> HAS_MAPPING_ANNOTATION =
            HAS_COLUMN_ANNOTATION.or(HAS_ID_ANNOTATION);
    public static final Predicate<Element> IS_FIELD =
            element -> element.getKind() == ElementKind.FIELD;
    public static final Predicate<Element> IS_INJECTABLE_CONSTRUCTOR = element -> {
        ExecutableElement constructor = (ExecutableElement) element;
        return constructor.getParameters().stream().anyMatch(ID_PARAMETER.or(COLUMN_PARAMETER));
    };

    private ElementPredicates() {
    }
}
