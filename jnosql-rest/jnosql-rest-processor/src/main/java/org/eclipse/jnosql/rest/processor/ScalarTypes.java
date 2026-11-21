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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Set;

/**
 * Identifies the types that are transported unchanged between HTTP and Java: primitives, wrappers, strings,
 * numbers, identifiers, enums and {@code java.time} values.
 */
final class ScalarTypes {

    private static final Set<String> TYPES = Set.of(
            "java.lang.String", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte", "java.lang.Short",
            "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double",
            "java.math.BigDecimal", "java.math.BigInteger", "java.util.UUID", "java.util.Date",
            "java.util.Locale", "java.util.Currency", "java.net.URI", "java.net.URL");

    private ScalarTypes() {
    }

    private static final Map<String, String> PARSED = Map.ofEntries(
            Map.entry("java.time.LocalDate", "parse"), Map.entry("java.time.LocalDateTime", "parse"),
            Map.entry("java.time.LocalTime", "parse"), Map.entry("java.time.Instant", "parse"),
            Map.entry("java.time.OffsetDateTime", "parse"), Map.entry("java.time.OffsetTime", "parse"),
            Map.entry("java.time.ZonedDateTime", "parse"), Map.entry("java.time.Duration", "parse"),
            Map.entry("java.time.Period", "parse"), Map.entry("java.time.Year", "parse"),
            Map.entry("java.time.YearMonth", "parse"), Map.entry("java.time.MonthDay", "parse"),
            Map.entry("java.time.ZoneId", "of"), Map.entry("java.time.ZoneOffset", "of"),
            Map.entry("java.util.Locale", "forLanguageTag"), Map.entry("java.util.Currency", "getInstance"));

    /**
     * Finds the static factory that converts text into the type for HTTP binding, for the scalar types that Jakarta
     * REST cannot convert by itself.
     *
     * @param type the parameter type
     * @return the factory method name, or {@code null} when Jakarta REST binds the type natively or it is unsupported
     */
    static String parser(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return null;
        }
        return PARSED.get(((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString());
    }

    /**
     * Tells whether Jakarta REST can bind the type, directly or through {@link #parser(TypeMirror)}.
     *
     * @param type the parameter type
     * @return {@code true} when a query or path parameter of the type is usable
     */
    static boolean isHttpBindable(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return true;
        }
        if (!isScalar(type) || type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        return element.getKind() == ElementKind.ENUM || parser(type) != null
                || !element.getQualifiedName().toString().startsWith("java.time.");
    }

    static boolean isScalar(TypeMirror type) {
        if (type.getKind().isPrimitive()) {
            return true;
        }
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        String name = element.getQualifiedName().toString();
        return element.getKind() == ElementKind.ENUM || TYPES.contains(name) || name.startsWith("java.time.");
    }
}
