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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;

import java.util.Collections;

@ApplicationScoped
class SqlReturnType {

    private static final Page<Object> EMPTY_PAGINATION = NoSQLPage.of(Collections.emptyList(), PageRequest.ofSize(1));
    private final EntitiesMetadata entitiesMetadata;
    private final ProjectorConverter projectorConverter;

    @Inject
    SqlReturnType(EntitiesMetadata entitiesMetadata, ProjectorConverter projectorConverter) {
        this.entitiesMetadata = entitiesMetadata;
        this.projectorConverter = projectorConverter;
    }

    SqlReturnType() {
        this.entitiesMetadata = null;
        this.projectorConverter = null;
    }
}
