/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest.processor;

import org.eclipse.jnosql.rest.RestExposure;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a Jakarta REST resource for every Jakarta Data repository annotated with
 * {@code @RestRepository}, plus the DTO records and the MapStruct mapper interface when {@code @RestDTO} is used.
 * <p>
 * The processor works in three steps: analysing the repositories, preparing generation models and rendering them
 * with Mustache templates. It does not depend on the order of other processors: MapStruct implements the generated
 * mapper interface in a later round, and the resources only refer to the mapper interface.
 */
@SupportedAnnotationTypes("org.eclipse.jnosql.rest.RestRepository")
public class RestProcessor extends AbstractProcessor {

    private static final String MAPSTRUCT_MAPPER = "org.mapstruct.Mapper";
    private static final String CDI_REQUEST_SCOPED = "jakarta.enterprise.context.RequestScoped";

    private final Set<String> generatedMappers = new LinkedHashSet<>();
    private Diagnostics diagnostics;
    private boolean mapStructReported;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (diagnostics == null) {
            diagnostics = new Diagnostics(processingEnv.getMessager());
        }
        if (roundEnv.processingOver()) {
            verifyMapperImplementations();
            return false;
        }
        Set<TypeElement> repositories = new LinkedHashSet<>();
        for (TypeElement annotation : annotations) {
            repositories.addAll(ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation)));
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(org.eclipse.jnosql.rest.RestRepository.class)) {
            if (!(element instanceof TypeElement)) {
                diagnostics.error(element, "@RestRepository can only be used on an interface.");
            }
        }
        if (!repositories.isEmpty()) {
            generate(repositories);
        }
        return false;
    }

    private void generate(Set<TypeElement> repositories) {
        var elements = processingEnv.getElementUtils();
        var types = processingEnv.getTypeUtils();
        RepositoryResolver resolver = new RepositoryResolver(elements, types, diagnostics);
        DtoPlanner planner = new DtoPlanner(elements, types, diagnostics);
        OpenApiPropagator openApi = new OpenApiPropagator(elements);
        SourceWriter writer = new SourceWriter(processingEnv.getFiler());
        List<ResourceModel> resources = new ArrayList<>();
        List<Element> origins = new ArrayList<>();
        for (TypeElement element : repositories) {
            resolver.resolve(element).ifPresent(repository -> {
                DtoPlanner.EntityPlan plan = null;
                if (repository.dto()) {
                    if (!mapStructAvailable(repository)) {
                        return;
                    }
                    plan = planner.plan(repository);
                    if (plan == null) {
                        return;
                    }
                }
                ImportRegistry imports = new ImportRegistry(repository.packageName());
                EndpointFactory endpoints = new EndpointFactory(elements, types, diagnostics, openApi, imports,
                        repository, planner, plan);
                List<MethodModel> methods = endpoints.build();
                if (repository.exposure() == RestExposure.NONE) {
                    return;
                }
                if (methods.isEmpty()) {
                    diagnostics.warning(element, "No REST endpoint was generated for " + element.getSimpleName()
                            + " because none of its operations is exposed.");
                    return;
                }
                resources.add(resource(repository, plan, imports, openApi, methods));
                origins.add(element);
            });
        }
        if (diagnostics.errorCount() > 0) {
            return;
        }
        try {
            for (RecordSpec record : planner.records()) {
                writer.write(record);
            }
            for (MapperSpec mapper : planner.mappers()) {
                writer.write(mapper);
                generatedMappers.add(mapper.qualifiedName());
            }
            for (int index = 0; index < resources.size(); index++) {
                writer.write(resources.get(index), origins.get(index));
            }
        } catch (IOException exception) {
            diagnostics.error(repositories.iterator().next(), "Could not write generated REST sources: "
                    + exception.getMessage());
        }
    }

    private ResourceModel resource(RepositoryInfo repository, DtoPlanner.EntityPlan plan, ImportRegistry imports,
                                   OpenApiPropagator openApi, List<MethodModel> methods) {
        String repositoryType = imports.name(repository.element());
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(repositoryType, "repository"));
        if (plan != null) {
            dependencies.add(new Dependency(imports.name(plan.qualified(plan.mapperName())), "mapper"));
        }
        List<String> annotations = new ArrayList<>();
        String media = imports.name("jakarta.ws.rs.core.MediaType");
        annotations.add("@" + imports.name("jakarta.ws.rs.Path") + "(" + AnnotationSource.quote(repository.path())
                + ")");
        annotations.add("@" + imports.name("jakarta.ws.rs.Produces") + "(" + media + ".APPLICATION_JSON)");
        annotations.add("@" + imports.name("jakarta.ws.rs.Consumes") + "(" + media + ".APPLICATION_JSON)");
        annotations.add("@" + imports.name(CDI_REQUEST_SCOPED));
        AnnotationSource source = new AnnotationSource(imports);
        openApi.forType(repository.element()).forEach(mirror -> annotations.add(source.write(mirror)));
        imports.name("jakarta.inject.Inject");
        String className = repository.element().getSimpleName() + "Resource";
        List<String> parameters = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            parameters.add(dependency.type() + " " + dependency.name());
        }
        String constructorParameters = parameters.size() == 1 ? parameters.get(0)
                : "\n            " + String.join(",\n            ", parameters);
        return new ResourceModel(repository.packageName(), className, repositoryType, repository.path(),
                imports.imports(), annotations, dependencies, constructorParameters, methods);
    }

    private boolean mapStructAvailable(RepositoryInfo repository) {
        if (processingEnv.getElementUtils().getTypeElement(MAPSTRUCT_MAPPER) != null) {
            return true;
        }
        if (!mapStructReported) {
            mapStructReported = true;
            diagnostics.error(repository.element(), "@RestDTO requires MapStruct, but org.mapstruct.Mapper is not "
                    + "on the compilation classpath. Add org.mapstruct:mapstruct as a dependency and "
                    + "org.mapstruct:mapstruct-processor to the annotation processor path of the compiler.");
        }
        return false;
    }

    private void verifyMapperImplementations() {
        for (String mapper : generatedMappers) {
            if (processingEnv.getElementUtils().getTypeElement(mapper + "Impl") == null) {
                diagnostics.error(null, "MapStruct did not generate " + mapper + "Impl. If MapStruct reported no other error, add "
                        + "org.mapstruct:mapstruct-processor to the annotation processor path of the compiler "
                        + "(maven-compiler-plugin annotationProcessorPaths) next to jnosql-rest-processor.");
            }
        }
    }
}
