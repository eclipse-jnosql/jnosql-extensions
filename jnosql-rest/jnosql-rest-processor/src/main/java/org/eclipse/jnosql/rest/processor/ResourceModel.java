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

import java.util.List;

/**
 * Everything needed to render one generated Jakarta REST resource.
 *
 * @param packageName          the package of the resource
 * @param className            the simple name of the resource
 * @param repositoryName       the simple name of the repository
 * @param path                 the base path
 * @param imports              the required imports
 * @param typeAnnotations      the class-level annotations, one source line each
 * @param dependencies         the injected dependencies in constructor order
 * @param constructorParameters the rendered parameter list of the injection constructor
 * @param methods              the resource methods
 */
public record ResourceModel(String packageName,
                            String className,
                            String repositoryName,
                            String path,
                            List<String> imports,
                            List<String> typeAnnotations,
                            List<Dependency> dependencies,
                            String constructorParameters,
                            List<MethodModel> methods) {
}
