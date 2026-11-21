/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.rest.processor;

import org.eclipse.jnosql.rest.RestExposure;
import org.eclipse.jnosql.rest.RestOperation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

/**
 * Metadata of a repository annotated with {@code @RestRepository}, resolved from the annotation model.
 *
 * @param element       the repository interface
 * @param type          the repository type, used to resolve generic members
 * @param entity        the entity type
 * @param id            the identifier type
 * @param hasFindById   whether the repository inherits {@code BasicRepository}
 * @param path          the base path of the resource
 * @param exposure      the exposure policy
 * @param excluded      the excluded operation categories
 * @param dto           whether {@code @RestDTO} is present
 * @param dtoExclusions the attributes excluded from the response DTO
 */
record RepositoryInfo(TypeElement element,
                      DeclaredType type,
                      TypeMirror entity,
                      TypeMirror id,
                      boolean hasFindById,
                      String path,
                      RestExposure exposure,
                      Set<RestOperation> excluded,
                      boolean dto,
                      List<String> dtoExclusions) {

    String qualifiedName() {
        return element.getQualifiedName().toString();
    }

    String packageName() {
        String name = qualifiedName();
        return name.substring(0, name.length() - element.getSimpleName().length() - 1);
    }
}
