/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import java.util.regex.Pattern;

import org.eclipse.jnosql.lite.mapping.processing.ProcessorUtils;

/**
 * Utility class containing static methods for processing and manipulating elements in annotation processors.
 * This class provides methods for retrieving package names, simple names, capitalizing strings, checking element types,
 * and extracting data from type strings.
 */
public final class ProcessorUtil {

    /**
     * A compiled regular expression pattern for extracting text enclosed in angle brackets.
     */
    public static final Pattern COMPILE = Pattern.compile("<(.*?)>");

    private ProcessorUtil() {
    }

    public static String getPackageName(TypeElement classElement) {
        return ProcessorUtils.packageName(classElement);
    }

    public static String getSimpleNameAsString(Element element) {
        return ProcessorUtils.simpleName(element);
    }

    public static String capitalize(String name) {
        return ProcessorUtils.capitalize(name);
    }

    /**
     * Checks if the given Element is an instance of TypeElement.
     *
     * @param element The Element to check.
     * @return true if the Element is a TypeElement, false otherwise.
     */
    public static boolean isTypeElement(Element element) {
        return ProcessorUtils.isTypeElement(element);
    }

    /**
     * Extracts text enclosed in angle brackets from a type string using a regular expression pattern.
     *
     * @param returnType The input type string.
     * @return The extracted text from within angle brackets, or the input string if no match is found.
     */
    public static String extractFromType(String returnType) {
        return ProcessorUtils.extractFromType(returnType);
    }

    public static String generateClassName(String... components) {
        return ProcessorUtils.className(components);
    }
}
