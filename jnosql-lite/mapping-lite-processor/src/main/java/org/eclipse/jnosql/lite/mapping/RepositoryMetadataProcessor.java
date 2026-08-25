/*
 *  Copyright (c) 2026 Otávio Santana and others
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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.eclipse.jnosql.lite.mapping.processing.MappingResult;
import org.eclipse.jnosql.lite.mapping.repository.metadata.RepositoriesMetadataModel;
import org.eclipse.jnosql.lite.mapping.repository.metadata.RepositoryIntrospector;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Processes Jakarta Data repository interfaces to generate reflection-free
 * repository and repository-method metadata.
 *
 * <p>The resulting aggregate metadata is consumed by generated repository
 * implementations when resolving and invoking repository operations.</p>
 */
@SupportedAnnotationTypes("jakarta.data.repository.Repository")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class RepositoryMetadataProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(RepositoryMetadataProcessor.class.getName());
    private static final String TEMPLATE = "repositories_metadata.mustache";

    private final Mustache template;

    public RepositoryMetadataProcessor() {
        this.template = createTemplate();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        try {
            final List<MappingResult> mappingResults = new ArrayList<>();
            final List<String> references = new ArrayList<>();
            for (TypeElement annotation : annotations) {
                roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .filter(e -> !references.contains(e.toString()))
                        .peek(e -> references.add(e.toString()))
                        .map(e -> new RepositoryIntrospector(e, processingEnv))
                        .map(RepositoryIntrospector::get)
                        .filter(MappingResult::isNotEmpty)
                        .forEach(mappingResults::add);
            }

            if (!mappingResults.isEmpty()) {
                createRepository(mappingResults);
            }
        } catch (IOException exception) {
            error(exception);
        }
        return false;
    }

    private void createRepository(List<MappingResult> repositories) throws IOException {
        LOGGER.info("Creating the default repository class, with repositories: " + repositories.size());
        RepositoriesMetadataModel metadata = new RepositoriesMetadataModel(repositories);
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified());
        try (Writer writer = fileObject.openWriter()) {
            template.execute(writer, metadata);
        }
    }

    private Mustache createTemplate() {
        MustacheFactory factory = new DefaultMustacheFactory();
        return factory.compile(TEMPLATE);
    }

    private void error(Exception exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }

}
