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
package org.eclipse.jnosql.lite.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;

import javax.xml.transform.Templates;
import java.util.Objects;

public final class JNoSQLRepositoryProcessor {

    private final Templates templates;

    private final EntityMetadata entityMetadata;

    private final RepositoryMetadata repositoryMetadata;

    private final RepositoryOperationProvider repositoryOperationProvider;

    private JNoSQLRepositoryProcessor(Templates templates, EntityMetadata entityMetadata,
                                RepositoryMetadata repositoryMetadata,
                                     RepositoryOperationProvider repositoryOperationProvider) {
        this.templates = templates;
        this.entityMetadata = entityMetadata;
        this.repositoryMetadata = repositoryMetadata;
        this.repositoryOperationProvider = repositoryOperationProvider;
    }



    public static JNoSQLRepositoryProcessor of(Templates templates,
                                               EntityMetadata entityMetadata,
                                               RepositoryMetadata repositoryMetadata,
                                               RepositoryOperationProvider repositoryOperationProvider) {

        Objects.requireNonNull(templates, "templates is required");
        Objects.requireNonNull(entityMetadata, "entityMetadata is required");
        Objects.requireNonNull(repositoryMetadata, "repositoryMetadata is required");
        Objects.requireNonNull(repositoryOperationProvider, "repositoryOperationProvider is required");

        return new JNoSQLRepositoryProcessor(templates, entityMetadata, repositoryMetadata, repositoryOperationProvider);
    }
}
