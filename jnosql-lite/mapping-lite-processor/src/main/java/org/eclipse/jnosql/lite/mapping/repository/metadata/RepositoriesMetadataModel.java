/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository.metadata;

import org.eclipse.jnosql.lite.mapping.processing.BaseMappingModel;
import org.eclipse.jnosql.lite.mapping.processing.MappingCategory;
import org.eclipse.jnosql.lite.mapping.processing.MappingResult;
import java.util.List;

public class RepositoriesMetadataModel extends BaseMappingModel {

    private final List<String> repositories;

    private final List<String> projections;

    public RepositoriesMetadataModel(List<MappingResult> mappingResults) {
        this.repositories = mappingResults.stream().filter(m -> m.category() == MappingCategory.REPOSITORY)
                .map(MappingResult::name).toList();
        this.projections = mappingResults.stream().filter(m -> m.category() == MappingCategory.PROJECTION)
                .map(MappingResult::name).toList();
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public List<String> getProjections() {
        return projections;
    }

    public String getQualified() {
        return "org.eclipse.jnosql.lite.mapping.metadata.LiteRepositoriesMetadata";
    }
}