/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.metadata;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;

import java.util.Optional;

@ApplicationScoped
public class LiteRepositoryMetadata implements RepositoriesMetadata {

    @Override
    public Optional<RepositoryMetadata> get(Class<?> type) {
        return Optional.empty();
    }
}
