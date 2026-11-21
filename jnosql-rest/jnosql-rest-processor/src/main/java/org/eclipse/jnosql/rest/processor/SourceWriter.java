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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

/**
 * Renders the Mustache templates of the generated resources, DTO records and mappers through the {@link Filer}.
 */
final class SourceWriter {

    private final Mustache resource;
    private final Mustache record;
    private final Mustache mapper;
    private final Mustache search;
    private final Filer filer;

    SourceWriter(Filer filer) {
        MustacheFactory factory = new DefaultMustacheFactory();
        this.resource = factory.compile("rest-resource.mustache");
        this.record = factory.compile("rest-dto-record.mustache");
        this.mapper = factory.compile("rest-mapper.mustache");
        this.search = factory.compile("rest-search-request.mustache");
        this.filer = filer;
    }

    void write(ResourceModel model, Element origin) throws IOException {
        render(resource, model, model.packageName() + "." + model.className(), origin);
    }

    void write(RecordSpec spec) throws IOException {
        render(record, spec, spec.qualifiedName(), spec.origin());
    }

    void write(MapperSpec spec) throws IOException {
        render(mapper, spec, spec.qualifiedName(), spec.origin());
    }

    void write(SearchSpec spec) throws IOException {
        render(search, spec, spec.qualifiedName(), spec.origin());
    }

    private void render(Mustache template, Object model, String qualifiedName, Element origin) throws IOException {
        JavaFileObject file = filer.createSourceFile(qualifiedName, origin);
        try (Writer writer = file.openWriter()) {
            template.execute(writer, model).flush();
        }
    }
}
