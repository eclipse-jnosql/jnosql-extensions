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
package org.eclipse.jnosql.lite.mapping.converter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes("jakarta.nosql.Converter")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoApplyConverterProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(AutoApplyConverterProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(jakarta.nosql.Converter.class)
                .stream()
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .filter(this::isAutoApply)
                .forEach(converter -> LOGGER.fine(() ->
                        "Auto-apply converter found: " + converter.getQualifiedName()));


        return false;
    }

    private boolean isAutoApply(TypeElement converter) {
        var annotation = converter.getAnnotation(jakarta.nosql.Converter.class);
        return annotation != null && annotation.autoApply();
    }
}
