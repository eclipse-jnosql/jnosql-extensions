/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping;


import jakarta.data.exceptions.MappingException;

/**
 * A custom exception class for handling validation-related errors.
 * This exception class extends the MappingException class, allowing
 * it to inherit exception handling features and behaviors from its parent class.
 */
public class ValidationException extends MappingException {

    /**
     * Constructs a ValidationException with the specified error message.
     *
     * @param message The error message describing the reason for the exception.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a ValidationException with the specified error message and cause.
     *
     * @param message The error message describing the reason for the exception.
     * @param cause   The underlying cause of the exception, which can be another exception.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}