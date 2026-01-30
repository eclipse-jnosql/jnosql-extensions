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
package org.eclipse.jnosql.lite.mapping;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.nosql.Projection;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

final class ProjectionMappingIntrospector  {

    private static final Logger LOGGER = Logger.getLogger(ProjectionMappingIntrospector.class.getName());
    private static final String NEW_INSTANCE = "projector_metadata.mustache";
    private static final String INJECTABLE_CONSTRUCTOR = "projector_constructor_metadata.mustache";

    private final Element entity;

    private final ProcessingEnvironment processingEnv;

    private final Mustache template;
    private final Mustache constructorTemplate;

    ProjectionMappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(NEW_INSTANCE);
        this.constructorTemplate = factory.compile(INJECTABLE_CONSTRUCTOR);
    }

    MappingResult buildMappingMetadata(TypeElement typeElement) throws IOException {

        String packageName = ProcessorUtil.getPackageName(typeElement);
        String className = ProcessorUtil.getSimpleNameAsString(typeElement);
        String type = ProcessorUtil.getSimpleNameAsString(typeElement);
        var projection = typeElement.getAnnotation(Projection.class).toString();
        var from = projection.substring(projection.indexOf("from=") +5, projection.lastIndexOf(")"));

        var constructor = processingEnv.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(MappingProcessor.IS_CONSTRUCTOR)
                .findFirst().orElseThrow();

        var executableElement = (ExecutableElement) constructor;
        var parameters = executableElement.getParameters().stream()
                .map(p -> new ParameterAnalyzer(p, processingEnv, typeElement))
                .map(ParameterAnalyzer::get)
                .toList();

        LOGGER.finest("Found the parameters: " + parameters);
        var constructorMetamodel = ConstructorMetamodel.of(ProcessorUtil.getPackageName(typeElement),
                ProcessorUtil.getSimpleNameAsString(typeElement), parameters,
                ProcessorUtil.getSimpleNameAsString(typeElement));

        createConstructors(entity, constructorMetamodel);
        var constructorClassName = constructorMetamodel.getQualified();

        ProjectionModel metadata = new ProjectionModel(packageName, className, type, from, constructorClassName);
        createClass(typeElement, metadata);
        return new MappingResult(MappingCategory.PROJECTION, metadata.getQualified());
    }

    private void createClass(Element entity, ProjectionModel metadata) throws IOException {
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

}
