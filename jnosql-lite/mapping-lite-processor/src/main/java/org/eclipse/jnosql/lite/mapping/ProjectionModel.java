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

public class ProjectionModel extends BaseMappingModel {

    private final String packageName;

    private final String className;

    private final String type;

    private final String from;

     ProjectionModel(String packageName,
                           String className,
                           String type,
                           String from) {

        this.packageName = packageName;
        this.className = className;
        this.type = type;
        this.from = from;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className + "ProjectionMetadata";
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }
}
