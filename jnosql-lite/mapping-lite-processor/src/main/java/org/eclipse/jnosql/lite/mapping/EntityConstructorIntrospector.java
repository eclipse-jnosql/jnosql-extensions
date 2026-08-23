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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.logging.Logger;

import static org.eclipse.jnosql.lite.mapping.ParameterAnalyzer.INJECT_CONSTRUCTOR;

final class EntityConstructorIntrospector {

    private static final Logger LOGGER = Logger.getLogger(EntityConstructorIntrospector.class.getName());

    private final ProcessingEnvironment processingEnv;

    EntityConstructorIntrospector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    Optional<ConstructorMetamodel> introspect(TypeElement entity) {
        return processingEnv.getElementUtils()
                .getAllMembers(entity)
                .stream()
                .filter(MappingProcessor.IS_CONSTRUCTOR
                        .and(MappingProcessor.HAS_ACCESS)
                        .and(INJECT_CONSTRUCTOR))
                .findFirst()
                .map(ExecutableElement.class::cast)
                .map(constructor -> metadata(entity, constructor));
    }

    private ConstructorMetamodel metadata(TypeElement entity, ExecutableElement constructor) {
        var parameters = constructor.getParameters().stream()
                .map(parameter -> new ParameterAnalyzer(parameter, processingEnv, entity))
                .map(ParameterAnalyzer::get)
                .toList();
        LOGGER.finest("Found the parameters: " + parameters);
        String packageName = ProcessorUtil.getPackageName(entity);
        String entityName = ProcessorUtil.getSimpleNameAsString(entity);
        return ConstructorMetamodel.of(packageName, entityName, parameters, entityName);
    }
}
