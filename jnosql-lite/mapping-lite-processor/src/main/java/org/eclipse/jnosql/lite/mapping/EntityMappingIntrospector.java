/*
 *  Copyright (c) 2025 Otávio Santana and others
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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.logging.Logger;

final class EntityMappingIntrospector {

    private static final Logger LOGGER = Logger.getLogger(EntityMappingIntrospector.class.getName());

    private final Element entity;
    private final EntityFieldCollector fieldCollector;
    private final EntityConstructorIntrospector constructorIntrospector;
    private final EntityModelIntrospector modelIntrospector;
    private final EntitySourceGenerator sourceGenerator;

    EntityMappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.fieldCollector = new EntityFieldCollector(processingEnv);
        this.constructorIntrospector = new EntityConstructorIntrospector(processingEnv);
        this.modelIntrospector = new EntityModelIntrospector();
        this.sourceGenerator = new EntitySourceGenerator(processingEnv);
    }

    MappingResult buildMappingMetadata(TypeElement typeElement) throws IOException {
        var fields = fieldCollector.collect(typeElement);
        var constructor = constructorIntrospector.introspect(typeElement);
        if (constructor.isPresent()) {
            sourceGenerator.generateConstructor(entity, constructor.get());
        }
        String constructorClassName = constructor
                .map(ConstructorMetamodel::getQualified)
                .orElse(null);
        EntityModel metadata = modelIntrospector.introspect(typeElement, fields, constructorClassName);
        sourceGenerator.generateEntity(entity, metadata);
        LOGGER.info("Found the fields: " + fields);

        return new MappingResult(MappingCategory.ENTITY, metadata.getQualified());
    }
}
