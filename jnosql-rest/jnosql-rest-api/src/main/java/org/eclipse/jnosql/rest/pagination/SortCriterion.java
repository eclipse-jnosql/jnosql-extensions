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
package org.eclipse.jnosql.rest.pagination;

/**
 * One sorting criterion of a generated search request body, equivalent to one {@code attribute:direction} term of
 * the {@code sort} query parameter.
 *
 * @param attribute the entity attribute to sort by
 * @param direction {@code asc} or {@code desc}, case-insensitive; {@code asc} when absent
 */
public record SortCriterion(String attribute, String direction) {
}
