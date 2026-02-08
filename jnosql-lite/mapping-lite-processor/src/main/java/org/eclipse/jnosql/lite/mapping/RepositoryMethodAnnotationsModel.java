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

class RepositoryMethodAnnotationsModel extends BaseMappingModel {

    private final String packageName;
    private final String className;
    private final String annotation;
    private final String providerAnnotation;
    private final String provider;

    RepositoryMethodAnnotationsModel(String packageName,
                                            String className,
                                            String annotation,
                                            String providerAnnotation,
                                            String provider) {
        this.packageName = packageName;
        this.className = className;
        this.annotation = annotation;
        this.providerAnnotation = providerAnnotation;
        this.provider = provider;
    }

    public String getClassName() {
        return className +"Annotations";
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getProviderAnnotation() {
        return providerAnnotation;
    }

    public String getProvider() {
        return provider;
    }
}
