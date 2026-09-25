/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.core.repository;

/**
 * Resolves a custom repository instance.
 *
 * <p>
 * This interface abstracts the CDI {@code CDI.current().select(...)} lookup
 * pattern used by {@code DefaultCustomRepositoryMethodOperator}, enabling
 * Spring Boot environments to provide a resolver implementation without
 * coupling to CDI APIs.
 *
 * @see jakarta.enterprise.inject.Instance
 */
public interface CustomRepositoryResolver {

    /**
     * Resolves and returns a custom repository instance for the given class.
     *
     * @param repositoryClass the repository interface class
     * @return a custom repository instance
     */
    Object resolve(Class<?> repositoryClass);
}