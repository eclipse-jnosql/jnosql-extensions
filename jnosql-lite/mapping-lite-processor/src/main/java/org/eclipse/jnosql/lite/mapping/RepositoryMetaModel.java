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

class RepositoryMetaModel extends BaseMappingModel {

    private final String packageName;

    private final String entity;

    private final String name;

    private final List<String> methods;

    private final String type;

     RepositoryMetaModel(String packageName,
                         String entity,
                         String name,
                         String type,
                         List<String> methods) {
        this.packageName = packageName;
        this.entity = entity;
        this.name = name;
        this.methods = methods;
        this.type = type;
    }


    public String getPackageName() {
        return packageName;
    }

    public String getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public List<String> getMethods() {
        return methods;
    }

    public String getType() {
        return type;
    }

    public String getEntityQualified() {
        return packageName + '.' + entity;
    }

    public String getClassName() {
        return entity + "RepositoryMetadata";
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }
}
