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
 * A nested filter record of a generated search request, shared by all attributes with the same value type.
 *
 * @param name          the simple name of the filter record
 * @param valueType     the Java type of the compared values
 * @param attributeType the Jakarta Data attribute type accepted by the filter
 * @param comparable    whether ordering operators are supported
 */
public record SearchFilter(String name,
                           String valueType,
                           String attributeType,
                           boolean comparable) {
}
