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

import com.github.mustachejava.Mustache;
import org.eclipse.jnosql.lite.mapping.ValidationException;
import org.eclipse.jnosql.mapping.DatabaseType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.function.Supplier;
import java.util.logging.Logger;

final class RepositoryAnalyzer implements Supplier<String> {

    private static final Logger LOGGER = Logger.getLogger(RepositoryAnalyzer.class.getName());

    private final Element entity;

    private final ProcessingEnvironment processingEnv;

    private final DatabaseType type;

    RepositoryAnalyzer(Element repositoryType, ProcessingEnvironment processingEnv, DatabaseType type) {
        this.entity = repositoryType;
        this.processingEnv = processingEnv;
        this.type = type;
    }

    @Override
    public String get() {
        LOGGER.info("Starting to process the repository processor with the class ");
        RepositoryElement element = RepositoryElement.of(entity, processingEnv, type);
        Filer filer = processingEnv.getFiler();
        RepositoryMetadata metadata = element.getMetadata(element.getType());
        RepositoryTemplateType templateType = metadata.getTemplateType();
        LOGGER.info("Starting to generate the generate class to " + metadata.getQualified());
        try {
            JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
            try (Writer writer = fileObject.openWriter()) {
                Mustache template = templateType.get();
                template.execute(writer, metadata);
            }
        } catch (IOException exp) {
            throw new ValidationException("There is an issue when try to create the Repository class", exp);
        }
        return element.getClassName();
    }

}
