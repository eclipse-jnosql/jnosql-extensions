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
package org.eclipse.jnosql.lite.mapping;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ProjectionMappingIntrospector implements Supplier<String> {

    private static final Logger LOGGER = Logger.getLogger(ProjectionMappingIntrospector.class.getName());
    private static final String NEW_INSTANCE = "entity_metadata.mustache";
    private static final String INJECTABLE_CONSTRUCTOR = "projector_metadata.mustache";

    private final Element entity;

    private final ProcessingEnvironment processingEnv;

    private final Mustache template;
    private final Mustache constructorTemplate;

    ProjectionMappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(NEW_INSTANCE);
        this.constructorTemplate = factory.compile(INJECTABLE_CONSTRUCTOR);
    }

    @Override
    public String get() {
        return "";
    }
}
