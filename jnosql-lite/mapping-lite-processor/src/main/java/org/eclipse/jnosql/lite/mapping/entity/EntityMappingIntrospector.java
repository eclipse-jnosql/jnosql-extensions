/*
 *  Copyright (c) 2025 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entity;

import org.eclipse.jnosql.lite.mapping.constructor.ConstructorMetadataModel;
import org.eclipse.jnosql.lite.mapping.processing.MappingCategory;
import org.eclipse.jnosql.lite.mapping.processing.MappingResult;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.logging.Logger;

public final class EntityMappingIntrospector {

    private static final Logger LOGGER = Logger.getLogger(EntityMappingIntrospector.class.getName());

    private final Element entity;
    private final EntityFieldCollector fieldCollector;
    private final EntityConstructorIntrospector constructorIntrospector;
    private final EntityModelIntrospector modelIntrospector;
    private final EntitySourceGenerator sourceGenerator;

    public EntityMappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.fieldCollector = new EntityFieldCollector(processingEnv);
        this.constructorIntrospector = new EntityConstructorIntrospector(processingEnv);
        this.modelIntrospector = new EntityModelIntrospector();
        this.sourceGenerator = new EntitySourceGenerator(processingEnv);
    }

    public MappingResult buildMappingMetadata(TypeElement typeElement) throws IOException {
        var fields = fieldCollector.collect(typeElement);
        var constructor = constructorIntrospector.introspect(typeElement);
        if (constructor.isPresent()) {
            sourceGenerator.generateConstructor(entity, constructor.get());
        }
        String constructorClassName = constructor
                .map(ConstructorMetadataModel::getQualified)
                .orElse(null);
        EntityModel metadata = modelIntrospector.introspect(typeElement, fields, constructorClassName);
        sourceGenerator.generateEntity(entity, metadata);
        LOGGER.info("Found the fields: " + fields);

        return new MappingResult(MappingCategory.ENTITY, metadata.getQualified());
    }
}
