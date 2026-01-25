/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.nosql.DiscriminatorColumn;
import jakarta.nosql.DiscriminatorValue;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;
import jakarta.nosql.Inheritance;
import jakarta.nosql.MappedSuperclass;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.jnosql.lite.mapping.ParameterAnalyzer.INJECT_CONSTRUCTOR;

class MappingIntrospector implements Supplier<String> {

    private static final Logger LOGGER = Logger.getLogger(MappingIntrospector.class.getName());
    private static final String NEW_INSTANCE = "entity_metadata.mustache";
    private static final String INJECTABLE_CONSTRUCTOR = "constructor_metadata.mustache";

    private final Element entity;

    private final ProcessingEnvironment processingEnv;

    private final Mustache template;
    private final Mustache constructorTemplate;

    MappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(NEW_INSTANCE);
        this.constructorTemplate = factory.compile(INJECTABLE_CONSTRUCTOR);
    }

    @Override
    public String get() {
        if (ProcessorUtil.isTypeElement(entity)) {
            TypeElement typeElement = (TypeElement) entity;
            LOGGER.info("Processing the class: " + typeElement);
            boolean hasValidConstructor = processingEnv.getElementUtils().getAllMembers(typeElement)
                    .stream()
                    .filter(EntityProcessor.IS_CONSTRUCTOR.and(EntityProcessor.HAS_ACCESS))
                    .anyMatch(EntityProcessor.IS_CONSTRUCTOR.and(EntityProcessor.HAS_ACCESS));
            if (hasValidConstructor) {
                try {
                    return analyze(typeElement);
                } catch (IOException exception) {
                    error(exception);
                }
            } else {
                throw new ValidationException("The class " + ProcessorUtil.getSimpleNameAsString(entity)
                        + " must have at least an either public or default constructor");
            }
        }

        return "";
    }

    private String analyze(TypeElement typeElement) throws IOException {

        TypeElement superclass =
                (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
        Stream<? extends Element> superElements = Stream.empty();
        if (Objects.nonNull(superclass.getAnnotation(MappedSuperclass.class))) {
            superElements = processingEnv.getElementUtils()
                    .getAllMembers(superclass).stream();
        }
        Stream<? extends Element> elements = processingEnv.getElementUtils()
                .getAllMembers(typeElement).stream();

        final List<String> fields = Stream.concat(elements, superElements)
                .filter(EntityProcessor.IS_FIELD.and(EntityProcessor.HAS_ANNOTATION))
                .map(f -> new FieldAnalyzer(f, processingEnv, typeElement))
                .map(FieldAnalyzer::get)
                .collect(Collectors.toList());

        var constructor = processingEnv.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(EntityProcessor.IS_CONSTRUCTOR.and(EntityProcessor.HAS_ACCESS)
                        .and(INJECT_CONSTRUCTOR)).findFirst();
        String constructorClassName = null;

        if (constructor.isPresent()) {
            ExecutableElement executableElement = (ExecutableElement) constructor.get();
            var parameters = executableElement.getParameters().stream()
                    .map(p -> new ParameterAnalyzer(p, processingEnv, typeElement))
                    .map(ParameterAnalyzer::get)
                    .toList();
           LOGGER.finest("Found the parameters: " + parameters);
            var constructorMetamodel = ConstructorMetamodel.of(ProcessorUtil.getPackageName(typeElement),
                    ProcessorUtil.getSimpleNameAsString(typeElement), parameters,
                    ProcessorUtil.getSimpleNameAsString(typeElement));

            createConstructors(entity, constructorMetamodel);
            constructorClassName = constructorMetamodel.getQualified();
        }
        EntityModel metadata = getMetadata(typeElement, fields, constructorClassName);
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

    private void createConstructors(Element entity, ConstructorMetamodel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            constructorTemplate.execute(writer, metadata);
        }
    }

    private EntityModel getMetadata(TypeElement element, List<String> fields, String constructorClassName) {

        TypeElement superclass =
                (TypeElement) ((DeclaredType) element.getSuperclass()).asElement();
        final Entity annotation = element.getAnnotation(Entity.class);
        final boolean entityAnnotation = Objects.nonNull(annotation);
        final boolean embedded = Objects.nonNull(element.getAnnotation(Embeddable.class));
        final boolean hasInheritanceAnnotation = Objects.nonNull(element.getAnnotation(Inheritance.class));
        String packageName = ProcessorUtil.getPackageName(element);
        String sourceClassName = ProcessorUtil.getSimpleNameAsString(element);

        String entityName = Optional.ofNullable(annotation)
                .map(Entity::value)
                .filter(v -> !v.isBlank())
                .orElse(sourceClassName);
        var mappingName = Optional.ofNullable(annotation)
                .map(Entity::name)
                .filter(v -> !v.isBlank())
                .orElse(sourceClassName);
        String inheritanceParameter = null;
        boolean notConcrete = element.getModifiers().contains(Modifier.ABSTRACT)
                || !element.getRecordComponents().isEmpty();
        if (superclass.getAnnotation(Inheritance.class) != null) {
            inheritanceParameter = getInheritanceParameter(element, superclass);
            Entity superEntity = superclass.getAnnotation(Entity.class);
            entityName = superEntity.value().isBlank() ? ProcessorUtil.getSimpleNameAsString(superclass) : annotation.value();
        } else if (element.getAnnotation(Inheritance.class) != null) {
            inheritanceParameter = getInheritanceParameter(element, element);
        }
        return new EntityModel(packageName, sourceClassName, entityName, mappingName, fields, embedded, notConcrete,
                inheritanceParameter, entityAnnotation, hasInheritanceAnnotation, constructorClassName);
    }

    private String getInheritanceParameter(TypeElement element, TypeElement superclass) {
        String discriminatorColumn = Optional
                .ofNullable(superclass.getAnnotation(DiscriminatorColumn.class))
                .map(DiscriminatorColumn::value)
                .orElse(DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN);

        String discriminatorValue = Optional
                .ofNullable(element.getAnnotation(DiscriminatorValue.class))
                .map(DiscriminatorValue::value)
                .orElse(element.getSimpleName().toString());

        return new StringJoiner(",\n")
                .add("\"" + discriminatorValue + "\"")
                .add("\"" + discriminatorColumn + "\"")
                .add(superclass.getQualifiedName().toString() + ".class")
                .add(element.getQualifiedName().toString() + ".class")
                .toString();
    }

    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }
}
