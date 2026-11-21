/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.rest.processor;

import javax.lang.model.element.Element;
import java.util.List;

/**
 * Everything needed to render one generated DTO record.
 *
 * @param packageName the package of the record
 * @param name        the simple name of the record
 * @param javadoc     the one-line description of the record
 * @param components  the record components
 * @param imports     the imports required by the components
 * @param origin      the element that triggered the generation
 */
public record RecordSpec(String packageName,
                         String name,
                         String javadoc,
                         List<Component> components,
                         List<String> imports,
                         Element origin) {

    String qualifiedName() {
        return packageName.isEmpty() ? name : packageName + "." + name;
    }
}
