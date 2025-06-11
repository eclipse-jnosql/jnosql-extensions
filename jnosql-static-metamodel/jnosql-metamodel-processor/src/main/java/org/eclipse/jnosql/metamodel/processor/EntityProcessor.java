/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.metamodel.processor;

import jakarta.nosql.Column;
import jakarta.nosql.Id;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;


@SupportedAnnotationTypes({"jakarta.nosql.Entity", "jakarta.nosql.MappedSuperclass"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class EntityProcessor extends AbstractProcessor {

    private static final Logger LOGGER = Logger.getLogger(EntityProcessor.class.getName());
    static final Predicate<String> IS_BLANK = String::isBlank;
    static final Predicate<String> IS_NOT_BLANK = IS_BLANK.negate();
    static final Predicate<Element> HAS_COLUMN_ANNOTATION = el -> el.getAnnotation(Column.class) != null;
    static final Predicate<Element> HAS_ID_ANNOTATION = el -> el.getAnnotation(Id.class) != null;
    static final Predicate<Element> HAS_ANNOTATION = HAS_COLUMN_ANNOTATION.or(HAS_ID_ANNOTATION);
    static final Predicate<Element> IS_FIELD = el -> el.getKind() == ElementKind.FIELD;


    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        final List<String> entities = new ArrayList<>();
        final List<String> references = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            roundEnv.getElementsAnnotatedWith(annotation)
                    .stream()
                    .filter(e -> !references.contains(e.toString()))
                    .peek(e -> references.add(e.toString()))
                    .map(e -> new ClassAnalyzer(e, processingEnv))
                    .map(ClassAnalyzer::get)
                    .filter(IS_NOT_BLANK).forEach(entities::add);
        }
        LOGGER.info("Entities generated: " + entities);
        return false;
    }
}
