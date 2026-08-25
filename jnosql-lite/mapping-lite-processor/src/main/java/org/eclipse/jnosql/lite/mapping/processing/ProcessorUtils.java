/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.lite.mapping.processing;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;

public final class ProcessorUtils {

    private static final Pattern GENERIC_TYPE = Pattern.compile("<(.*?)>");

    private ProcessorUtils() {
    }

    public static String packageName(TypeElement type) {
        return ((PackageElement) type.getEnclosingElement()).getQualifiedName().toString();
    }

    public static String simpleName(Element element) {
        return element.getSimpleName().toString();
    }

    public static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    public static boolean isTypeElement(Element element) {
        return element instanceof TypeElement;
    }

    public static String extractFromType(String returnType) {
        Matcher matcher = GENERIC_TYPE.matcher(returnType);
        return matcher.find() ? matcher.group(1) : returnType;
    }

    public static String className(String... components) {
        StringBuilder className = new StringBuilder();
        for (int index = 0; index < components.length; index++) {
            String component = components[index];
            className.append(index == 0 ? component : capitalize(component));
        }
        return className.toString();
    }
}
