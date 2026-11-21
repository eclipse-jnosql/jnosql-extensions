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

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Reports compile-time problems and remembers whether an error was reported for the current repository.
 */
final class Diagnostics {

    private final Messager messager;
    private int errors;

    Diagnostics(Messager messager) {
        this.messager = messager;
    }

    void error(Element element, String message) {
        errors++;
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    void warning(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    void note(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    int errorCount() {
        return errors;
    }
}
