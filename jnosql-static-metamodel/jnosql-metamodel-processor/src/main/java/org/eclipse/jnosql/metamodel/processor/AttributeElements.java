/*
 *  Copyright (c) 2025 Otávio Santana and others
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
package org.eclipse.jnosql.metamodel.processor;

enum AttributeElements {

    TEXT_ATTRIBUTE("TextAttribute", "TextAttributeRecord"),
    NUMERIC_ATTRIBUTE("NumericAttribute", "NumericAttributeRecord"),
    COMPARABLE_ATTRIBUTE("ComparableAttribute", "ComparableAttributeRecord"),
    SORTABLE_ATTRIBUTE("SortableAttribute", "SortableAttributeRecord"),
    NAVIGABLE_ATTRIBUTE("NavigableAttribute", "NavigableAttributeRecord"),
    TEMPORAL_ATTRIBUTE("TemporalAttribute", "TemporalAttributeRecord"),
    BASIC_ATTRIBUTE("BasicAttribute", "BasicAttributeRecord");

    private final String type;
    private final String implementation;

    AttributeElements(String type, String implementation) {
        this.type = type;
        this.implementation = implementation;
    }

    public String getType() {
        return type;
    }

    public String getImplementation() {
        return implementation;
    }
}
