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

import jakarta.ws.rs.BadRequestException;

import java.util.function.Function;

/**
 * Converts query parameters whose Java type has no {@code valueOf(String)}, {@code fromString(String)} or
 * {@code String} constructor, such as the {@code java.time} types, which Jakarta REST cannot bind on its own.
 */
public final class QueryValues {

    private QueryValues() {
    }

    /**
     * Parses a query parameter value.
     *
     * @param name   the parameter name, used in the error message
     * @param value  the received text, possibly {@code null}
     * @param parser the conversion from text
     * @param <T>    the target type
     * @return the parsed value, or {@code null} when the text is {@code null}
     * @throws BadRequestException if the text cannot be parsed
     */
    public static <T> T parse(String name, String value, Function<String, T> parser) {
        if (value == null) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (RuntimeException exception) {
            throw new BadRequestException("'" + name + "' is not a valid value");
        }
    }
}
