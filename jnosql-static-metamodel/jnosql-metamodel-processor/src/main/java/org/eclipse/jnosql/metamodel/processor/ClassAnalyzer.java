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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.nosql.Entity;
import jakarta.nosql.MappedSuperclass;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

class ClassAnalyzer implements Supplier<String> {

    private static final Logger LOGGER = Logger.getLogger(ClassAnalyzer.class.getName());
    private static final String NEW_INSTANCE = "metamodel.mustache";

    private final Element entity;

    private final ProcessingEnvironment processingEnv;

    private final Mustache template;

    ClassAnalyzer(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(NEW_INSTANCE);
    }

    @Override
    public String get() {
        if (ProcessorUtil.isTypeElement(entity)) {
            TypeElement typeElement = (TypeElement) entity;
            LOGGER.info("Processing the class: " + typeElement);
                try {
                    return analyze(typeElement);
                } catch (IOException exception) {
                    error(exception);
                }
        }
        return "";
    }

    private String analyze(TypeElement typeElement) throws IOException {

        TypeElement superclass =
                (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        String superClassName = null;
        if (Objects.nonNull(superclass.getAnnotation(MappedSuperclass.class))
                ||
                Objects.nonNull(superclass.getAnnotation(Entity.class))) {
            var superQualifiedName = superclass.getQualifiedName().toString();
            int index = superQualifiedName.lastIndexOf(".");
            var qualifiedName = superQualifiedName.substring(0, index);
            superClassName = qualifiedName + "._" + superclass.getSimpleName().toString();
        }

        Stream<? extends Element> elements = processingEnv.getElementUtils()
                .getAllMembers(typeElement).stream();

        final List<FieldModel> fields = elements
                .filter(EntityProcessor.IS_FIELD.and(EntityProcessor.HAS_ANNOTATION))
                .map(f -> new FieldAnalyzer(f, processingEnv, typeElement,null,false))
                .map(FieldAnalyzer::get)
                .flatMap(List::stream)
                .toList();

        EntityModel metadata = getMetadata(typeElement, superClassName, fields);
        createClass(entity, metadata);
        LOGGER.info("Found the fields: " + fields);
        return metadata.getQualified();
    }

    private void createClass(Element entity, EntityModel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            template.execute(writer, metadata);
        }
    }

    private EntityModel getMetadata(TypeElement element, String superClassName, List<FieldModel> fields) {

        final Entity annotation = element.getAnnotation(Entity.class);
        String packageName = ProcessorUtil.getPackageName(element);
        String sourceClassName = ProcessorUtil.getSimpleNameAsString(element);


        String entityName = Optional.ofNullable(annotation)
                .map(Entity::value)
                .filter(v -> !v.isBlank())
                .orElse(sourceClassName);
        return new EntityModel(packageName, sourceClassName, entityName, fields, superClassName);
    }


    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }
}
