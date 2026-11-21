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
 * Everything needed to render the generated search request of one repository.
 *
 * @param packageName the package of the request
 * @param name        the simple name of the request
 * @param javadoc     the one-line description of the request
 * @param entityType  the rendered entity type
 * @param imports     the imports required by the request
 * @param attributes  the searchable attributes
 * @param filters     the nested filter records
 * @param origin      the element that triggered the generation
 * @param paginated   whether the request carries a {@code pagination} member
 * @param sorted      whether the request carries a {@code sort} member
 */
public record SearchSpec(String packageName,
                         String name,
                         String javadoc,
                         String entityType,
                         List<String> imports,
                         List<SearchAttribute> attributes,
                         List<SearchFilter> filters,
                         Element origin,
                         boolean paginated,
                         boolean sorted) {

    /**
     * Gives nested templates access to the request name, which {@code name} hides inside filter sections.
     *
     * @return the simple name of the request
     */
    public String requestName() {
        return name;
    }

    String qualifiedName() {
        return packageName.isEmpty() ? name : packageName + "." + name;
    }
}
