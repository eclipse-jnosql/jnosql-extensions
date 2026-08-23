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
package org.eclipse.jnosql.lite.mapping.projection;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import jakarta.nosql.Projection;
import org.eclipse.jnosql.lite.mapping.constructor.ConstructorMetadataModel;
import org.eclipse.jnosql.lite.mapping.processing.ElementPredicates;
import org.eclipse.jnosql.lite.mapping.processing.MappingCategory;
import org.eclipse.jnosql.lite.mapping.processing.MappingResult;
import org.eclipse.jnosql.lite.mapping.processing.ProcessorUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

public final class ProjectionMappingIntrospector  {

    private static final Logger LOGGER = Logger.getLogger(ProjectionMappingIntrospector.class.getName());
    private static final String NEW_INSTANCE = "projector_metadata.mustache";
    private static final String INJECTABLE_CONSTRUCTOR = "projector_constructor_metadata.mustache";
    private static final Mustache PROJECTOR_TEMPLATE;
    private static final Mustache CONSTRUCTOR_TEMPLATE;

    static {
        MustacheFactory factory = new DefaultMustacheFactory();
        PROJECTOR_TEMPLATE = factory.compile(NEW_INSTANCE);
        CONSTRUCTOR_TEMPLATE = factory.compile(INJECTABLE_CONSTRUCTOR);
    }

    private final Element entity;
    private final ProcessingEnvironment processingEnv;

    public ProjectionMappingIntrospector(Element entity, ProcessingEnvironment processingEnv) {
        this.entity = entity;
        this.processingEnv = processingEnv;

    }

    public MappingResult buildMappingMetadata(TypeElement typeElement) throws IOException {

        String packageName = ProcessorUtils.packageName(typeElement);
        String className = ProcessorUtils.simpleName(typeElement);
        String type = ProcessorUtils.simpleName(typeElement);
        var projection = typeElement.getAnnotation(Projection.class).toString();
        var from = projection.substring(projection.indexOf("from=") +5, projection.lastIndexOf(")"));

        var constructor = processingEnv.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(ElementPredicates.IS_CONSTRUCTOR)
                .findFirst().orElseThrow();

        var executableElement = (ExecutableElement) constructor;
        var parameters = executableElement.getParameters().stream()
                .map(p -> new ProjectorParameterAnalyzer(p, processingEnv, typeElement))
                .map(ProjectorParameterAnalyzer::get)
                .toList();

        LOGGER.finest("Found the parameters: " + parameters);
        var constructorMetamodel = ConstructorMetadataModel.of(ProcessorUtils.packageName(typeElement),
                ProcessorUtils.simpleName(typeElement), parameters,
                ProcessorUtils.simpleName(typeElement));

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
            PROJECTOR_TEMPLATE.execute(writer, metadata);
        }
    }

    private void createConstructors(Element entity, ConstructorMetadataModel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            CONSTRUCTOR_TEMPLATE.execute(writer, metadata);
        }
    }

}
