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
import java.util.List;

public class AutoApplyConverterModel {

    private final List<ConverterEntryType> converterEntryTypes;
    private final  List<ConverterEntryInstance> converterEntryInstances;

    public AutoApplyConverterModel(List<ConverterEntryType> converterEntryTypes,
                                   List<ConverterEntryInstance> converterEntryInstances) {
        this.converterEntryTypes = converterEntryTypes;
        this.converterEntryInstances = converterEntryInstances;
    }

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public String getCurrentYear(){
        return Year.now().toString();
    }

    public List<ConverterEntryType> getConverterEntryTypes() {
        return converterEntryTypes;
    }

    public List<ConverterEntryInstance> getConverterEntryInstances() {
        return converterEntryInstances;
    }
}
