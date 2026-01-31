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

import java.util.List;

class RepositoriesMetadataModel extends BaseMappingModel {

    private final List<String> entities;

    private final List<String> projections;

    public RepositoriesMetadataModel(List<MappingResult> mappingResults) {
        this.entities = mappingResults.stream().filter(m -> m.category() == MappingCategory.ENTITY)
                .map(MappingResult::name).toList();
        this.projections = mappingResults.stream().filter(m -> m.category() == MappingCategory.PROJECTION)
                .map(MappingResult::name).toList();
    }

    public List<String> getEntities() {
        return entities;
    }

    public List<String> getProjections() {
        return projections;
    }

    public String getQualified() {
        return "org.eclipse.jnosql.lite.mapping.metadata.LiteEntitiesMetadata";
    }
}