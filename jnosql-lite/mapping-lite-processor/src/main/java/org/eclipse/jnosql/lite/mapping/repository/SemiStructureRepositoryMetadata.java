/*
 *  Copyright (c) 2021 Otavio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;

import java.util.Locale;

class SemiStructureRepositoryMetadata extends RepositoryMetadata {

    private final String provider;

    SemiStructureRepositoryMetadata(RepositoryElement element, String provider) {
        super(element, provider.toUpperCase(Locale.ENGLISH));
        this.provider = provider;
    }

    @Override
    String getClassName() {
        return this.getElement().getSimpleName() + "Lite" + provider;
    }

    @Override
    RepositoryTemplateType getTemplateType() {
        return RepositoryTemplateType.SEMI_STRUCTURE;
    }

    @Override
    public MethodGenerator apply(MethodMetadata metadata) {
        return new MethodGenerator(metadata);
    }
}
