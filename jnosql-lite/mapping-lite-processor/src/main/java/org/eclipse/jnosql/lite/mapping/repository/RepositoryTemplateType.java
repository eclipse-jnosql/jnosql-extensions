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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.util.function.Supplier;

enum RepositoryTemplateType implements Supplier<Mustache> {
    SEMI_STRUCTURE("repository_semi_structure.mustache"),
    KEY_VALUE("repository_key-value.mustache");

    private final Mustache template;

    RepositoryTemplateType(String fileName) {
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(fileName);
    }

    @Override
    public Mustache get() {
        return template;
    }
}
