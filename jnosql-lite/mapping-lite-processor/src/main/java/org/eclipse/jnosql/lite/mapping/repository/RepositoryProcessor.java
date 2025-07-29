/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;


import org.eclipse.jnosql.mapping.DatabaseType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes("jakarta.data.repository.Repository")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RepositoryProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(RepositoryProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        final List<String> repositories = new ArrayList<>();
        try {
            Set<DatabaseType> types = DatabaseSupport.types();
            for (TypeElement annotation : annotations) {
                for (DatabaseType type : types) {
                    roundEnv.getElementsAnnotatedWith(annotation)
                            .stream().map(e -> new RepositoryAnalyzer(e, processingEnv, type))
                            .map(RepositoryAnalyzer::get)
                            .forEach(repositories::add);
                }
            }
        } catch (Exception exception) {
            error(exception);
        }
        if (!repositories.isEmpty()) {
            LOGGER.info("Repository processor has finished with those classes generated: " + repositories.size());
        }
        return false;
    }

    private void error(Exception exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }


}
