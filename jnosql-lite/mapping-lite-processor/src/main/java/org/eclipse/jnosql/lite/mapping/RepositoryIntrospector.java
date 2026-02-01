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
import javax.lang.model.element.Element;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RepositoryIntrospector implements Supplier<MappingResult> {

    private static final Logger LOGGER = Logger.getLogger(RepositoryIntrospector.class.getName());

    private final Element repository;

    private final ProcessingEnvironment processingEnv;
    private final EntityMappingIntrospector entityMappingIntrospector;
    private final ProjectionMappingIntrospector projectionMappingIntrospector;

    RepositoryIntrospector(Element repository, ProcessingEnvironment processingEnv) {
        this.repository = repository;
        this.processingEnv = processingEnv;
        this.entityMappingIntrospector = new EntityMappingIntrospector(repository, processingEnv);
        this.projectionMappingIntrospector = new ProjectionMappingIntrospector(repository, processingEnv);
    }

    @Override
    public MappingResult get() {
        LOGGER.info("Processing the repository: " + repository);
        return new MappingResult(MappingCategory.PROJECTION, "");
    }
}
