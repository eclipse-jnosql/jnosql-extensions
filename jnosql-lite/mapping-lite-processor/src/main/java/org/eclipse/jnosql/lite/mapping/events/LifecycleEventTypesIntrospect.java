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
package org.eclipse.jnosql.lite.mapping.events;



import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.function.Supplier;
import java.util.logging.Logger;

final class LifecycleEventTypesIntrospect implements Supplier<String> {

    private static final Logger LOGGER = Logger.getLogger(LifecycleEventTypesIntrospect.class.getName());

    private final Element entity;

    private final ProcessingEnvironment processingEnv;

    LifecycleEventTypesIntrospect(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;
    }

    @Override
    public String get() {
        return "";
    }
}
