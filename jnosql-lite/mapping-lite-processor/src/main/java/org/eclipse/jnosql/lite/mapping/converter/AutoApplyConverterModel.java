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
package org.eclipse.jnosql.lite.mapping.converter;

import java.time.LocalDateTime;
import java.time.Year;

public class AutoApplyConverterModel {

    private static final String PACKAGE = "org.eclipse.jnosql.lite.mapping.converter";

    private final String className;
    private final String type;
    private final String typeConverter;

    public AutoApplyConverterModel(String className, String type, String typeConverter) {
        this.className = className;
        this.type = type;
        this.typeConverter = typeConverter;
    }

    public String getClassName() {
        return className;
    }

    public String getType() {
        return type;
    }

    public String getTypeConverter() {
        return typeConverter;
    }

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public String getCurrentYear(){
        return Year.now().toString();
    }

    public String getQualified() {
        return PACKAGE + "." + className;
    }
}
