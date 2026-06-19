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

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Converter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes("jakarta.nosql.Converter")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoApplyConverterProcessor extends AbstractProcessor {


    private static final Logger LOGGER =
            Logger.getLogger(AutoApplyConverterProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        List<ConverterEntryType> converterTypes = new ArrayList<>();
        List<ConverterEntryInstance> converterInstances = new ArrayList<>();

        roundEnv.getElementsAnnotatedWith(Converter.class)
                .stream()
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .filter(this::isAutoApply)
                .forEach(converter -> {

                    DeclaredType attributeConverter =
                            attributeConverter(converter);

                    if (attributeConverter == null) {
                        LOGGER.warning(() -> "Ignoring converter " + converter.getQualifiedName() + " because it does not implement "
                                + AttributeConverter.class.getName());
                        return;
                    }

                    String attributeType = attributeType(attributeConverter);
                    String converterType = converter.getQualifiedName().toString();

                    converterTypes.add(new ConverterEntryType(attributeType + ".class", converterType + ".class"));
                    converterInstances.add(new ConverterEntryInstance(attributeType + ".class", "new " + converterType + "()"));
                });

        AutoApplyConverterModel model = new AutoApplyConverterModel(converterTypes, converterInstances);

        LOGGER.fine(() -> "Found " + converterTypes.size() + " auto-apply converters");
        // TODO generate source file using model
        return false;
    }

    private boolean isAutoApply(TypeElement converter) {
        Converter annotation = converter.getAnnotation(Converter.class);
        return annotation != null && annotation.autoApply();
    }

    private DeclaredType attributeConverter(TypeElement converter) {

        for (TypeMirror mirror : converter.getInterfaces()) {

            if (!(mirror instanceof DeclaredType declaredType)) {
                continue;
            }

            Element element = declaredType.asElement();

            if (element instanceof TypeElement typeElement
                    && typeElement.getQualifiedName()
                    .contentEquals(AttributeConverter.class.getName())) {
                return declaredType;
            }
        }

        return null;
    }

    private String attributeType(DeclaredType converter) {
        return processingEnv.getTypeUtils()
                .erasure(converter.getTypeArguments().get(0))
                .toString();
    }


}
