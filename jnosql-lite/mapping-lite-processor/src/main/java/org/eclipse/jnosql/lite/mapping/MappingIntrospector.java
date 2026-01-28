/*
 *  Copyright (c) 2020 Otávio Santana and others
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

import jakarta.nosql.Projection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Logger;

class MappingIntrospector implements Supplier<MappingResult> {

    private static final Logger LOGGER = Logger.getLogger(MappingIntrospector.class.getName());

    private final Element entity;

    private final ProcessingEnvironment processingEnv;
    private final EntityMappingIntrospector entityMappingIntrospector;
    private final ProjectionMappingIntrospector projectionMappingIntrospector;

    MappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;
        this.entityMappingIntrospector = new EntityMappingIntrospector(entity, processingEnv);
        this.projectionMappingIntrospector = new ProjectionMappingIntrospector(entity, processingEnv);
    }

    @Override
    public MappingResult get() {
        if (ProcessorUtil.isTypeElement(entity)) {
            TypeElement typeElement = (TypeElement) entity;
            LOGGER.info("Processing the class: " + typeElement);
            var annotation = typeElement.getAnnotation(Projection.class);
            if (annotation != null) {
                try {
                    return projectionMappingIntrospector.buildMappingMetadata(typeElement);
                } catch (IOException exception) {
                    error(exception);
                }
            } else {
                var mappingResult = entityMapping(typeElement);
                if (mappingResult != null) {
                    return mappingResult;
                }
            }
        }
        return MappingResult.EMPTY;
    }

    private MappingResult entityMapping(TypeElement typeElement) {
        boolean hasValidConstructor = processingEnv.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(MappingProcessor.IS_CONSTRUCTOR.and(MappingProcessor.HAS_ACCESS))
                .anyMatch(MappingProcessor.IS_CONSTRUCTOR.and(MappingProcessor.HAS_ACCESS));
        if (hasValidConstructor) {
            try {
                return entityMappingIntrospector.buildMappingMetadata(typeElement);
            } catch (IOException exception) {
                error(exception);
            }
        } else {
            throw new ValidationException("The class " + ProcessorUtil.getSimpleNameAsString(entity)
                    + " must have at least an either public or default constructor");
        }
        return null;
    }

    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }
}
