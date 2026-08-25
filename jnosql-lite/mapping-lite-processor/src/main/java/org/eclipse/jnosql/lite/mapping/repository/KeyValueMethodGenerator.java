/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;

import java.util.List;

class KeyValueMethodGenerator implements MethodGenerator {

    private final MethodMetadata metadata;
    KeyValueMethodGenerator(MethodMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public List<String> getLines() {
        KeyValueMethodBuilder methodBuilder = KeyValueMethodBuilder.of(this.metadata);
        return methodBuilder.apply(this.metadata);
    }

    @Override
    public boolean hasReturn() {
        KeyValueMethodBuilder methodBuilder = KeyValueMethodBuilder.of(this.metadata);
        return !KeyValueMethodBuilder.NOT_SUPPORTED.equals(methodBuilder)
                &&
                !metadata.getReturnType().equals(Void.TYPE.getName());
    }
}
