/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.metamodel.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Utility class containing static methods for processing and manipulating elements in annotation processors.
 * This class provides methods for retrieving package names, simple names, capitalizing strings, checking element types,
 * and extracting data from type strings.
 */
public final class ProcessorUtil {

    private ProcessorUtil() {
    }

    static String getPackageName(TypeElement classElement) {
        return ((PackageElement) classElement.getEnclosingElement()).getQualifiedName().toString();
    }

    static String getSimpleNameAsString(Element element) {
        return element.getSimpleName().toString();
    }
    /**
     * Checks if the given Element is an instance of TypeElement.
     *
     * @param element The Element to check.
     * @return true if the Element is a TypeElement, false otherwise.
     */
    public static boolean isTypeElement(Element element) {
        return element instanceof TypeElement;
    }
}
