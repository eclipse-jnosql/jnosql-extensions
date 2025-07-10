/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Ondro Mihalyi
 */
public class CdiUtil {

    private CdiUtil() {
    }

    /**
     * Find all qualifiers in the list of annotations, including qualifiers nested in stereotypes
     * @param annotations
     * @return
     */
    public static Set<Annotation> getAllQualifiersRecursively(Annotation... annotations) {
        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Annotation> qualifiers = new HashSet<>();
        Set<Class<? extends Annotation>> visitedStereotypes = new HashSet<>();

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();

            if (beanManager.isQualifier(type)) {
                qualifiers.add(annotation);
            } else if (beanManager.isStereotype(type)) {
                resolveStereotypeQualifiers(type, beanManager, qualifiers, visitedStereotypes);
            }
        }

        return qualifiers;
    }

    private static void resolveStereotypeQualifiers(Class<? extends Annotation> stereotype,BeanManager beanManager,
            Set<Annotation> qualifiers, Set<Class<? extends Annotation>> visitedStereotypes) {
        if (!visitedStereotypes.add(stereotype)) {
            return; // avoid infinite loop
        }

        for (Annotation inner : stereotype.getAnnotations()) {
            Class<? extends Annotation> innerType = inner.annotationType();

            if (beanManager.isQualifier(innerType)) {
                qualifiers.add(inner);
            } else if (beanManager.isStereotype(innerType)) {
                resolveStereotypeQualifiers(innerType, beanManager, qualifiers, visitedStereotypes);
            }
        }
    }

}
