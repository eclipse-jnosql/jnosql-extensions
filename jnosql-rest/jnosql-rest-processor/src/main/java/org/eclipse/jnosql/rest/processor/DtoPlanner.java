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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Plans the DTO records and the MapStruct mapper of every entity exposed in DTO mode.
 * <p>
 * Developer-defined types with the conventional names are reused and never overwritten. Missing ones are described
 * as {@link RecordSpec} and {@link MapperSpec} values and written after all repositories were analysed.
 */
final class DtoPlanner {

    private static final Set<String> COLLECTIONS = Set.of("java.util.List", "java.util.Set", "java.util.Collection");
    private static final String MAP = "java.util.Map";
    private static final String OPTIONAL = "java.util.Optional";

    private final Elements elements;
    private final Types types;
    private final Diagnostics diagnostics;
    private final PropertyScanner scanner;
    private final Map<String, EntityPlan> entities = new LinkedHashMap<>();
    private final Map<String, RecordSpec> records = new LinkedHashMap<>();

    DtoPlanner(Elements elements, Types types, Diagnostics diagnostics) {
        this.elements = elements;
        this.types = types;
        this.diagnostics = diagnostics;
        this.scanner = new PropertyScanner(elements, types);
    }

    /**
     * The DTO naming and request requirements of one entity.
     */
    final class EntityPlan {

        private final RepositoryInfo repository;
        private final TypeElement entity;
        private final List<Property> properties;
        private boolean createRequired;
        private boolean updateRequired;
        private boolean optionalUsed;

        private EntityPlan(RepositoryInfo repository, TypeElement entity, List<Property> properties) {
            this.repository = repository;
            this.entity = entity;
            this.properties = properties;
        }

        String packageName() {
            return repository.packageName();
        }

        String dtoName() {
            return entity.getSimpleName() + "DTO";
        }

        String createName() {
            return entity.getSimpleName() + "CreateRequest";
        }

        String updateName() {
            return entity.getSimpleName() + "UpdateRequest";
        }

        String mapperName() {
            return entity.getSimpleName() + "DTOMapper";
        }

        String qualified(String simpleName) {
            return packageName().isEmpty() ? simpleName : packageName() + "." + simpleName;
        }

        boolean entityIsRecord() {
            return entity.getKind() == ElementKind.RECORD;
        }

        TypeElement entity() {
            return entity;
        }
    }

    /**
     * Resolves the DTO plan of the repository entity, validating exclusions and mapping support.
     *
     * @return the plan, or {@code null} after an error was reported
     */
    EntityPlan plan(RepositoryInfo repository) {
        TypeElement entity = (TypeElement) ((DeclaredType) repository.entity()).asElement();
        String key = entity.getQualifiedName().toString();
        EntityPlan existing = entities.get(key);
        if (existing != null) {
            if (!existing.repository.dtoExclusions().equals(repository.dtoExclusions())) {
                diagnostics.error(repository.element(), "Entity " + key + " is already exposed by "
                        + existing.repository.qualifiedName() + " with @RestDTO(exclude = "
                        + existing.repository.dtoExclusions() + "); repositories sharing an entity must declare "
                        + "the same exclusions because the DTO and its mapper are shared.");
                return null;
            }
            return existing;
        }
        List<Property> properties = scanner.scan(entity);
        if (properties.isEmpty()) {
            diagnostics.error(repository.element(), "Entity " + key + " has no readable attributes, "
                    + "so no DTO can be generated for it.");
            return null;
        }
        for (String excluded : repository.dtoExclusions()) {
            if (properties.stream().noneMatch(property -> property.name().equals(excluded))) {
                diagnostics.error(repository.element(), "@RestDTO excludes the attribute '" + excluded
                        + "', but " + key + " has no such attribute. Available attributes: "
                        + properties.stream().map(Property::name).toList() + ".");
                return null;
            }
        }
        EntityPlan plan = new EntityPlan(repository, entity, properties);
        int errors = diagnostics.errorCount();
        planResponse(plan);
        if (diagnostics.errorCount() > errors) {
            return null;
        }
        entities.put(key, plan);
        return plan;
    }

    boolean requireCreate(EntityPlan plan) {
        if (plan.createRequired) {
            return true;
        }
        List<Property> writable = plan.properties.stream().filter(Property::writable).toList();
        if (writable.isEmpty()) {
            return false;
        }
        int errors = diagnostics.errorCount();
        planRequest(plan, plan.createName(), writable, false,
                "Request body to create a {@link " + plan.entity.getSimpleName() + "}.");
        plan.createRequired = diagnostics.errorCount() == errors;
        return plan.createRequired;
    }

    boolean requireUpdate(EntityPlan plan) {
        if (plan.updateRequired) {
            return true;
        }
        List<Property> writable = plan.properties.stream()
                .filter(Property::writable).filter(property -> !property.identifier()).toList();
        if (plan.entityIsRecord() || writable.isEmpty()) {
            return false;
        }
        int errors = diagnostics.errorCount();
        planRequest(plan, plan.updateName(), writable, true,
                "Request body to update a {@link " + plan.entity.getSimpleName()
                        + "}; absent or null attributes keep their current value.");
        plan.updateRequired = diagnostics.errorCount() == errors;
        return plan.updateRequired;
    }

    List<RecordSpec> records() {
        return new ArrayList<>(records.values());
    }

    List<MapperSpec> mappers() {
        List<MapperSpec> mappers = new ArrayList<>();
        for (EntityPlan plan : entities.values()) {
            if (elements.getTypeElement(plan.qualified(plan.mapperName())) != null) {
                continue;
            }
            mappers.add(mapper(plan));
        }
        return mappers;
    }

    private MapperSpec mapper(EntityPlan plan) {
        ImportRegistry imports = new ImportRegistry(plan.packageName());
        String mapper = imports.name("org.mapstruct.Mapper");
        String reporting = imports.name("org.mapstruct.ReportingPolicy");
        String entity = imports.name(plan.entity);
        String dto = imports.name(plan.qualified(plan.dtoName()));
        String list = imports.name("java.util.List");
        List<String> methods = new ArrayList<>();
        methods.add("    " + dto + " toDTO(" + entity + " entity);");
        methods.add("    " + list + "<" + dto + "> toDTO(" + list + "<" + entity + "> entities);");
        if (plan.createRequired) {
            methods.add("    " + entity + " toEntity(" + imports.name(plan.qualified(plan.createName()))
                    + " request);");
        }
        if (plan.updateRequired) {
            String target = imports.name("org.mapstruct.MappingTarget");
            String beanMapping = imports.name("org.mapstruct.BeanMapping");
            String strategy = imports.name("org.mapstruct.NullValuePropertyMappingStrategy");
            methods.add("    @" + beanMapping + "(nullValuePropertyMappingStrategy = " + strategy + ".IGNORE)\n"
                    + "    void updateEntity(" + imports.name(plan.qualified(plan.updateName()))
                    + " request, @" + target + " " + entity + " entity);");
        }
        if (plan.optionalUsed) {
            String optional = imports.name("java.util.Optional");
            methods.add("    default <T> T unwrap(" + optional + "<T> optional) {\n"
                    + "        return optional.orElse(null);\n    }");
        }
        String annotationHeader = "@" + mapper + "(componentModel = \"jakarta-cdi\", unmappedTargetPolicy = "
                + reporting + ".IGNORE)";
        return new MapperSpec(plan.packageName(), plan.mapperName(),
                "MapStruct mapper between {@link " + plan.entity.getSimpleName() + "} and its REST DTOs; the "
                        + "implementation is generated by MapStruct as a CDI bean.",
                annotationHeader, imports.imports(), methods, plan.repository.element());
    }

    private void planResponse(EntityPlan plan) {
        String qualified = plan.qualified(plan.dtoName());
        if (elements.getTypeElement(qualified) != null) {
            validateExisting(plan, qualified);
            return;
        }
        List<Property> visible = plan.properties.stream()
                .filter(property -> !plan.repository.dtoExclusions().contains(property.name())).toList();
        if (visible.isEmpty()) {
            diagnostics.error(plan.repository.element(), "@RestDTO excludes every attribute of "
                    + plan.entity.getQualifiedName() + ", leaving an empty response DTO.");
            return;
        }
        planRecord(plan, plan.dtoName(), visible, false,
                "REST representation of {@link " + plan.entity.getSimpleName() + "}.");
    }

    private void planRequest(EntityPlan plan, String name, List<Property> selected, boolean boxed, String javadoc) {
        if (elements.getTypeElement(plan.qualified(name)) != null) {
            return;
        }
        planRecord(plan, name, selected, boxed, javadoc);
    }

    private void planRecord(EntityPlan plan, String name, List<Property> selected, boolean boxed, String javadoc) {
        ImportRegistry imports = new ImportRegistry(plan.packageName());
        Deque<String> stack = new ArrayDeque<>();
        stack.push(plan.entity.getQualifiedName().toString());
        List<Component> components = new ArrayList<>();
        for (int index = 0; index < selected.size(); index++) {
            Property property = selected.get(index);
            TypeMirror type = property.type();
            if (boxed && type.getKind().isPrimitive()) {
                type = types.boxedClass((javax.lang.model.type.PrimitiveType) type).asType();
            }
            String rendered = dtoType(type, stack, imports, plan, property.name());
            if (rendered == null) {
                return;
            }
            components.add(new Component(rendered, property.name(), index == selected.size() - 1));
        }
        records.put(plan.qualified(name), new RecordSpec(plan.packageName(), name, javadoc, components,
                imports.imports(), plan.repository.element()));
    }

    private void validateExisting(EntityPlan plan, String qualified) {
        TypeElement dto = elements.getTypeElement(qualified);
        List<Property> dtoProperties = scanner.scan(dto);
        for (Property property : dtoProperties) {
            if (plan.repository.dtoExclusions().contains(property.name())) {
                diagnostics.error(plan.repository.element(), "The developer-defined " + qualified
                        + " exposes the excluded attribute '" + property.name() + "'.");
                continue;
            }
            Property source = plan.properties.stream()
                    .filter(candidate -> candidate.name().equals(property.name())).findFirst().orElse(null);
            if (source == null) {
                diagnostics.error(plan.repository.element(), "The developer-defined " + qualified
                        + " declares '" + property.name() + "', which " + plan.entity.getQualifiedName()
                        + " does not have, so MapStruct cannot map it.");
            } else if (ScalarTypes.isScalar(property.type()) && ScalarTypes.isScalar(source.type())
                    && !types.isSameType(types.erasure(property.type()), types.erasure(source.type()))) {
                diagnostics.error(plan.repository.element(), "The developer-defined " + qualified + " declares '"
                        + property.name() + "' as " + property.type() + " but the entity declares "
                        + source.type() + "; MapStruct would need a custom conversion.");
            }
        }
    }

    /**
     * Maps an entity attribute type to the type used in a DTO.
     *
     * @return the source of the DTO type, or {@code null} after an error was reported
     */
    private String dtoType(TypeMirror type, Deque<String> stack, ImportRegistry imports, EntityPlan plan,
                           String attribute) {
        if (ScalarTypes.isScalar(type)) {
            return imports.render(type);
        }
        if (type.getKind() == TypeKind.ARRAY) {
            String component = dtoType(((ArrayType) type).getComponentType(), stack, imports, plan, attribute);
            return component == null ? null : component + "[]";
        }
        if (type.getKind() != TypeKind.DECLARED) {
            return unsupported(plan, attribute, type, "type variables, wildcards and raw types cannot be mapped");
        }
        DeclaredType declared = (DeclaredType) type;
        TypeElement element = (TypeElement) declared.asElement();
        String name = element.getQualifiedName().toString();
        List<? extends TypeMirror> arguments = declared.getTypeArguments();
        if (name.equals(OPTIONAL) && arguments.size() == 1) {
            plan.optionalUsed = true;
            return dtoType(arguments.get(0), stack, imports, plan, attribute);
        }
        if (COLLECTIONS.contains(name) && arguments.size() == 1 || name.equals(MAP) && arguments.size() == 2) {
            List<String> mapped = new ArrayList<>();
            for (TypeMirror argument : arguments) {
                String rendered = dtoType(argument, stack, imports, plan, attribute);
                if (rendered == null) {
                    return null;
                }
                mapped.add(rendered);
            }
            return imports.name(name) + "<" + String.join(", ", mapped) + ">";
        }
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jakarta.")
                || !arguments.isEmpty() || element.getModifiers().contains(Modifier.ABSTRACT)
                || (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.RECORD)) {
            return unsupported(plan, attribute, type, "only concrete, non-generic classes and records, collections, "
                    + "maps, arrays, scalars and Optional are supported");
        }
        return nested(element, stack, imports, plan, attribute);
    }

    private String nested(TypeElement element, Deque<String> stack, ImportRegistry imports, EntityPlan plan,
                          String attribute) {
        String qualifiedEntity = element.getQualifiedName().toString();
        if (stack.contains(qualifiedEntity)) {
            return unsupported(plan, attribute, element.asType(), "recursive structure "
                    + String.join(" -> ", stack.reversed()) + " -> " + qualifiedEntity
                    + "; exclude the attribute with @RestDTO(exclude)");
        }
        String nestedName = element.getSimpleName() + "DTO";
        String nestedQualified = plan.qualified(nestedName);
        if (elements.getTypeElement(nestedQualified) == null && !records.containsKey(nestedQualified)) {
            List<Property> properties = scanner.scan(element);
            if (properties.isEmpty()) {
                return unsupported(plan, attribute, element.asType(), "it has no readable attributes");
            }
            stack.push(qualifiedEntity);
            ImportRegistry nestedImports = new ImportRegistry(plan.packageName());
            List<Component> components = new ArrayList<>();
            for (int index = 0; index < properties.size(); index++) {
                Property property = properties.get(index);
                String rendered = dtoType(property.type(), stack, nestedImports, plan, attribute + "." + property.name());
                if (rendered == null) {
                    stack.pop();
                    return null;
                }
                components.add(new Component(rendered, property.name(), index == properties.size() - 1));
            }
            stack.pop();
            records.put(nestedQualified, new RecordSpec(plan.packageName(), nestedName,
                    "REST representation of the nested {@link " + imports.name(element) + "}.", components,
                    nestedImports.imports(), plan.repository.element()));
        }
        return imports.name(nestedQualified);
    }

    private String unsupported(EntityPlan plan, String attribute, TypeMirror type, String reason) {
        diagnostics.error(plan.repository.element(), "Cannot map attribute '" + attribute + "' of type " + type
                + " of " + plan.entity.getQualifiedName() + " to a DTO: " + reason + ".");
        return null;
    }
}
