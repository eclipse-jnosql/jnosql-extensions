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

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RepositoryIntrospector implements Supplier<MappingResult> {

    private static final String NEW_INSTANCE = "repository_metadata.mustache";

    private static final Set<String> JAKARTA_DATA_REPOSITORIES = Set.of(
            "jakarta.data.repository.DataRepository",
            "jakarta.data.repository.BasicRepository",
            "jakarta.data.repository.CrudRepository"
    );

    private static final Logger LOGGER = Logger.getLogger(RepositoryIntrospector.class.getName());

    private final Element element;

    private final ProcessingEnvironment processingEnv;
    private final EntityMappingIntrospector entityMappingIntrospector;
    private final ProjectionMappingIntrospector projectionMappingIntrospector;
    private final Mustache template;

    RepositoryIntrospector(Element element, ProcessingEnvironment processingEnv) {
        this.element = element;
        this.processingEnv = processingEnv;
        this.entityMappingIntrospector = new EntityMappingIntrospector(element, processingEnv);
        this.projectionMappingIntrospector = new ProjectionMappingIntrospector(element, processingEnv);
        MustacheFactory factory = new DefaultMustacheFactory();
        this.template = factory.compile(NEW_INSTANCE);

    }

    @Override
    public MappingResult get() {
        if(element instanceof TypeElement repository){
            return generateMappingInterface(repository);
        }
        return new MappingResult(MappingCategory.PROJECTION, "");
    }

    private MappingResult generateMappingInterface(TypeElement repository) {
        LOGGER.info("Processing the repository: " + repository);
        String packageName = ProcessorUtil.getPackageName(repository);
        String entity = entityOptionalLiteral(repository);
        String type = ProcessorUtil.getSimpleNameAsString(repository);
        var metadata = new RepositoryMetaModel(packageName, entity, type, Collections.emptyList());
        try {
            createClass(element, metadata);
        } catch (IOException exception) {
            error(exception);
        }
        return new MappingResult(MappingCategory.ENTITY, metadata.getQualified());
    }

    private String entityOptionalLiteral(TypeElement repository) {

        return repository.getInterfaces().stream()
                .filter(t -> t.getKind() == TypeKind.DECLARED)
                .map(t -> (DeclaredType) t)
                .filter(dt -> {
                    TypeElement interfaceElement = (TypeElement) dt.asElement();
                    return JAKARTA_DATA_REPOSITORIES.contains(
                            interfaceElement.getQualifiedName().toString()
                    );
                })
                .filter(dt -> !dt.getTypeArguments().isEmpty())
                .map(dt -> dt.getTypeArguments().getFirst())
                .filter(t -> t.getKind() == TypeKind.DECLARED)
                .map(t -> (DeclaredType) t)
                .map(t -> (TypeElement) t.asElement())
                .map(te -> "Optional.of(" + te.getQualifiedName() + ".class)")
                .findFirst()
                .orElse("Optional.empty()");
    }

    private void createClass(Element entity, RepositoryMetaModel metadata) throws IOException {
        Filer filer = processingEnv.getFiler();
        JavaFileObject fileObject = filer.createSourceFile(metadata.getQualified(), entity);
        try (Writer writer = fileObject.openWriter()) {
            template.execute(writer, metadata);
        }
    }

    private void error(IOException exception) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: "
                + exception.getMessage());
    }
}
