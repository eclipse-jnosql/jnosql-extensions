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

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.function.Function;

abstract class RepositoryMetadata implements Function<MethodMetadata, MethodGenerator> {

    private final RepositoryElement element;

    private final String providerType;

    protected RepositoryMetadata(RepositoryElement element, String providerType) {
        this.element = element;
        this.element.getMethods().forEach(m -> m.update(this));
        this.providerType = providerType;
    }

    abstract String getClassName();

    abstract RepositoryTemplateType getTemplateType();

    public String getQualified() {
        return this.getPackage() + '.' + getClassName();
    }

    public String getPackage() {
        return element.getPackage();
    }

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public String getEntityType() {
        return this.element.getEntityType();
    }

    public String getKeyType() {
        return this.element.getKeyType();
    }

    public String getRepository() {
        return this.element.getRepository();
    }

    public List<MethodMetadata> getMethods() {
        return this.element.getMethods();
    }

    public RepositoryElement getElement() {
        return element;
    }

    public String getProviderType() {
        return providerType;
    }

    public String getCurrentYear() {
        return Year.now().toString();
    }
}
