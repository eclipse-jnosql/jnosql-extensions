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
import jakarta.data.repository.Select;
import jakarta.nosql.Column;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

class ProjectorParameterAnalyzer implements Supplier<ParameterResult> {

    private static final Mustache MUSTACHE_DEFAULT_TEMPLATE;
    private static final Logger LOGGER = Logger.getLogger(ProjectorParameterAnalyzer.class.getName());
    private static final String DEFAULT_TEMPLATE = "projector_parameter_metadata.mustache";


    private final VariableElement parameter;
    private final ProcessingEnvironment processingEnv;
    private final TypeElement entity;

    static {
        MUSTACHE_DEFAULT_TEMPLATE = createTemplate();
    }

    ProjectorParameterAnalyzer(VariableElement parameter, ProcessingEnvironment processingEnv,
                               TypeElement entity) {
        this.parameter = parameter;
        this.processingEnv = processingEnv;
        this.entity = entity;
    }

    @Override
    public ParameterResult get() {
        var metadata = getMetaData();
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = getFileObject(metadata, filer);
        try (Writer writer = fileObject.openWriter()) {
                MUSTACHE_DEFAULT_TEMPLATE.execute(writer, metadata);
        } catch (IOException exception) {
            throw new ValidationException("An error to compile the class: " +
                    metadata.getQualified(), exception);
        }
        return new ParameterResult(metadata.getQualified(), metadata.getType());
    }

    private JavaFileObject getFileObject(ParameterModel metadata, Filer filer) {
        try {
            return filer.createSourceFile(metadata.getQualified(), entity);
        } catch (IOException exception) {
            throw new ValidationException("An error to create the class: " +
                    metadata.getQualified(), exception);
        }

    }

    private ParameterModel getMetaData() {
        final String fieldName = parameter.getSimpleName().toString();
        LOGGER.finest("Processing the parameter: " + fieldName);
        final String entityName = ProcessorUtil.getSimpleNameAsString(this.entity);
        final TypeMirror typeMirror = parameter.asType();
        String className;

        if (typeMirror instanceof DeclaredType declaredType) {
            Element element = declaredType.asElement();
            className = element.toString();
        } else {
            className = typeMirror.toString();
        }

        var column = parameter.getAnnotation(Column.class);
        var select = findSelectFromRecordComponent(entity, fieldName).orElse(null);

        final String packageName = ProcessorUtil.getPackageName(entity);
        final String name = getName(fieldName, column, select);

        return ParameterModel.builder()
                .packageName(packageName)
                .name(name)
                .type(className)
                .entity(entityName)
                .fieldName(fieldName)
                .build();
    }

    private Optional<Select> findSelectFromRecordComponent(
            TypeElement entity,
            String fieldName
    ) {
        if (entity.getKind() != ElementKind.RECORD) {
            return Optional.empty();
        }

        return entity.getRecordComponents().stream()
                .filter(rc -> rc.getSimpleName().contentEquals(fieldName))
                .map(rc -> rc.getAnnotation(Select.class))
                .filter(Objects::nonNull)
                .findFirst();
    }
    private String getName(String fieldName, Column column, Select select) {
        if (select != null && !select.value().isBlank()) {
            return select.value();
        }
        if (column != null && !column.value().isBlank()) {
            return column.value();
        }
        return fieldName;
    }

    private static Mustache createTemplate() {
        MustacheFactory factory = new DefaultMustacheFactory();
        return factory.compile(ProjectorParameterAnalyzer.DEFAULT_TEMPLATE);
    }

}
