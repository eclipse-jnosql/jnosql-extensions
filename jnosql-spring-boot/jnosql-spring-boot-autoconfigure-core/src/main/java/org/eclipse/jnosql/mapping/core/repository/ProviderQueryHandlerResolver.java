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

import org.eclipse.jnosql.mapping.metadata.repository.spi.ProviderQueryHandler;

/**
 * Resolves a {@link ProviderQueryHandler} instance.
 *
 * <p>
 * This interface abstracts the CDI {@code Instance<ProviderQueryHandler>} lookup
 * pattern used by {@code CoreProviderOperation}, enabling Spring Boot environments
 * to provide a resolver implementation without coupling to CDI APIs.
 * </p>
 *
 * @see ProviderQueryHandler
 */
public interface ProviderQueryHandlerResolver {

    /**
     * Resolves and returns a {@link ProviderQueryHandler} instance.
     *
     * @return a {@link ProviderQueryHandler} instance
     */
    ProviderQueryHandler resolve();
}