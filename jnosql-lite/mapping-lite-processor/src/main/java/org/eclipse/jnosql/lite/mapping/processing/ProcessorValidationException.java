/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.lite.mapping.processing;

import jakarta.nosql.MappingException;

public class ProcessorValidationException extends MappingException {

    public ProcessorValidationException(String message) {
        super(message);
    }

    public ProcessorValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
