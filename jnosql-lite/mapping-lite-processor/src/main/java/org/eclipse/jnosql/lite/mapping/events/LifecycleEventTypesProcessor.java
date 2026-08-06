/*
 *  Copyright (c) 2026 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.events;

import org.eclipse.jnosql.lite.mapping.metadata.LifecycleEventTypes;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes("jakarta.nosql.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class LifecycleEventTypesProcessor  extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(LifecycleEventTypesProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final List<String> references = new ArrayList<>();

        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation)
                    .stream()
                    .filter(e -> !references.contains(e.toString()))
                    .peek(e -> references.add(e.toString()))
                    .map(e -> new LifecycleEventTypesIntrospect(e, processingEnv))
                    .map(LifecycleEventTypesIntrospect::get)
                    .forEach(references::add);
        }

        if (!references.isEmpty()) {
            LOGGER.info("LifecycleEventTypesProcessor: " + references.size() + " references found.");
            try {
                createResource(references);
            } catch (IOException e) {
                LOGGER.severe("Failed to create resource: " + e.getMessage());
            }
        }
        return false;
    }

    private void createResource(List<String> implementations) throws IOException {
        Filer filer = processingEnv.getFiler();
        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + LifecycleEventTypes.class.getName());
        try (var printWriter = new PrintWriter(new OutputStreamWriter(file.openOutputStream(), StandardCharsets.UTF_8))) {
            for (String implementation : implementations) {
                printWriter.println(implementation);
            }
        }
    }
}
