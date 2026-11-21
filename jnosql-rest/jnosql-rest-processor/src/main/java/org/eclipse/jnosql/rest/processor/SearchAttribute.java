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

/**
 * One entity attribute that a generated search request can filter on.
 *
 * @param constant      the name of the static attribute binding
 * @param name          the attribute name, also the JSON property
 * @param filterType    the simple name of the nested filter record
 * @param attributeType the Jakarta Data attribute type, such as {@code ComparableAttribute<Product, BigDecimal>}
 * @param factory       the expression creating the attribute binding
 */
public record SearchAttribute(String constant,
                              String name,
                              String filterType,
                              String attributeType,
                              String factory) {
}
