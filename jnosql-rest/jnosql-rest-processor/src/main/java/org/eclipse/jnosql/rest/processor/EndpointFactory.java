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

import org.eclipse.jnosql.rest.RestExclude;
import org.eclipse.jnosql.rest.RestExpose;
import org.eclipse.jnosql.rest.RestExposure;
import org.eclipse.jnosql.rest.RestOperation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the operations of one repository into Jakarta REST resource methods, applying the exposure policy and
 * reporting every unsupported or contradictory configuration through diagnostics.
 */
final class EndpointFactory {

    private static final String DATA_PACKAGE = "jakarta.data.repository.";
    private static final String QUERY = DATA_PACKAGE + "Query";
    private static final String FIND = DATA_PACKAGE + "Find";
    private static final String INSERT = DATA_PACKAGE + "Insert";
    private static final String SAVE = DATA_PACKAGE + "Save";
    private static final String UPDATE = DATA_PACKAGE + "Update";
    private static final String DELETE = DATA_PACKAGE + "Delete";
    private static final Set<String> RESERVED = Set.of("repository", "mapper", "entity", "request", "result",
            "stream", "created", "response", "page", "offset", "size", "cursor", "includeTotal", "cursorContext");
    private static final String PAGE_PACKAGE = "jakarta.data.page.";
    private static final String PAGE = PAGE_PACKAGE + "Page";
    private static final String CURSORED_PAGE = PAGE_PACKAGE + "CursoredPage";
    private static final String PAGE_REQUEST = PAGE_PACKAGE + "PageRequest";
    private static final String PAGINATION = "org.eclipse.jnosql.rest.pagination.Pagination";
    private static final List<String> PAGINATION_PARAMETERS = List.of("offset", "size", "includeTotal", "cursor");
    private static final Pattern TEMPLATE = Pattern.compile("\\{([^}/]+)}");
    private static final String INDENT = "    ";

    private final Elements elements;
    private final Types types;
    private final Diagnostics diagnostics;
    private final OpenApiPropagator openApi;
    private final ImportRegistry imports;
    private final RepositoryInfo repository;
    private final DtoPlanner planner;
    private final DtoPlanner.EntityPlan dto;
    private final AnnotationSource annotationSource;
    private final SearchPlanner searchPlanner;
    private final Map<String, String> routes = new HashMap<>();
    private final List<MethodModel> methods = new ArrayList<>();
    private SearchSpec searchRequest;
    private boolean searchRequestResolved;

    EndpointFactory(Elements elements, Types types, Diagnostics diagnostics, OpenApiPropagator openApi,
                    ImportRegistry imports, RepositoryInfo repository, DtoPlanner planner,
                    DtoPlanner.EntityPlan dto) {
        this.types = types;
        this.diagnostics = diagnostics;
        this.openApi = openApi;
        this.imports = imports;
        this.repository = repository;
        this.planner = planner;
        this.dto = dto;
        this.annotationSource = new AnnotationSource(imports);
        this.elements = elements;
        this.searchPlanner = new SearchPlanner(elements, types, diagnostics);
    }

    /**
     * The search request to generate, available after {@link #build()} when a search endpoint was created.
     *
     * @return the specification, or {@code null} when none must be generated
     */
    SearchSpec searchRequest() {
        return searchRequest;
    }

    List<MethodModel> build() {
        List<ExecutableElement> members = ElementFilter.methodsIn(elements.getAllMembers(repository.element()));
        members.sort(Comparator.comparing((ExecutableElement method) -> method.getSimpleName().toString())
                .thenComparing(method -> method.getParameters().toString())
                .thenComparing(method -> method.asType().toString()));
        for (ExecutableElement method : members) {
            if (isCandidate(method)) {
                handle(method);
            }
        }
        return methods;
    }

    private boolean isCandidate(ExecutableElement method) {
        Element owner = method.getEnclosingElement();
        if (((TypeElement) owner).getQualifiedName().contentEquals("java.lang.Object")) {
            return false;
        }
        return !method.getModifiers().contains(Modifier.STATIC) && !method.getModifiers().contains(Modifier.DEFAULT)
                && !method.getModifiers().contains(Modifier.PRIVATE);
    }

    private enum Kind { FIND_BY_ID, FIND_ALL, INSERT, UPDATE, SAVE, DELETE_BY_ID, DELETE, READ, COUNT, EXISTS,
        QUERY_DELETE, QUERY_UPDATE, SEARCH, LIFECYCLE_INSERT, LIFECYCLE_SAVE, LIFECYCLE_UPDATE, LIFECYCLE_DELETE }

    private record Classified(Kind kind, RestOperation operation, boolean explicitOnly, String unsupported) {

        static Classified of(Kind kind, RestOperation operation) {
            return new Classified(kind, operation, false, null);
        }

        static Classified explicit(Kind kind, RestOperation operation) {
            return new Classified(kind, operation, true, null);
        }

        static Classified unsupported(String reason) {
            return new Classified(null, null, false, reason);
        }
    }

    private void handle(ExecutableElement method) {
        boolean standardOwner = ((TypeElement) method.getEnclosingElement()).getQualifiedName().toString()
                .startsWith(DATA_PACKAGE);
        Classified classified = classify(method, standardOwner);
        RestExpose expose = method.getAnnotation(RestExpose.class);
        boolean explicit = expose != null;
        Element target = standardOwner ? repository.element() : method;
        if (explicit && method.getAnnotation(RestExclude.class) != null) {
            diagnostics.error(method, "@RestExpose and @RestExclude cannot be combined on "
                    + method.getSimpleName() + ".");
            return;
        }
        if (method.getAnnotation(RestExclude.class) != null) {
            return;
        }
        if (repository.exposure() == RestExposure.NONE) {
            if (explicit) {
                diagnostics.error(method, "@RestExpose on " + method.getSimpleName() + " contradicts "
                        + "@RestRepository(exposure = NONE) of " + repository.element().getSimpleName() + ".");
            }
            return;
        }
        if (classified.unsupported() != null) {
            if (explicit) {
                diagnostics.error(method, "@RestExpose cannot expose " + method.getSimpleName() + ": "
                        + classified.unsupported());
            } else if (repository.exposure() == RestExposure.ALL) {
                report(standardOwner, target, "Not exposed over REST: " + method.getSimpleName() + " - "
                        + classified.unsupported());
            }
            return;
        }
        if (repository.excluded().contains(classified.operation())) {
            if (explicit) {
                diagnostics.error(method, "@RestExpose on " + method.getSimpleName() + " contradicts "
                        + "@RestRepository(exclude = " + classified.operation() + ").");
            }
            return;
        }
        if (repository.exposure() == RestExposure.EXPLICIT && !explicit) {
            return;
        }
        if (classified.explicitOnly() && !explicit) {
            diagnostics.note(method, "Not exposed over REST: " + method.getSimpleName()
                    + (classified.kind() == Kind.SEARCH
                    ? " accepts a dynamic Restriction; add @RestExpose to publish it as a QUERY search endpoint."
                    : " changes data through a query; add @RestExpose to publish it."));
            return;
        }
        String path = explicit ? expose.path() : "";
        if (explicit && !path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }
        generate(method, classified, path, standardOwner);
    }

    private void report(boolean standardOwner, Element target, String message) {
        if (standardOwner) {
            diagnostics.note(target, message);
        } else {
            diagnostics.warning(target, message);
        }
    }

    private Classified classify(ExecutableElement method, boolean standardOwner) {
        Classified classified = classifyOperation(method, standardOwner);
        if (classified.unsupported() != null || restrictionParameters(method) == 0) {
            return classified;
        }
        boolean readOnly = classified.kind() == Kind.READ || classified.kind() == Kind.COUNT
                || classified.kind() == Kind.EXISTS;
        if (!readOnly) {
            return Classified.unsupported("a dynamic restriction can only be used by read-only operations, because "
                    + "it is served through HTTP QUERY");
        }
        if (nonPagingParameters(method).size() != 1 || !isEntityRestriction(nonPagingParameters(method).get(0))) {
            return Classified.unsupported("a dynamic restriction must be the only parameter, besides an optional "
                    + "PageRequest, and be a Restriction of the repository entity");
        }
        return Classified.explicit(Kind.SEARCH, classified.operation());
    }

    private List<TypeMirror> nonPagingParameters(ExecutableElement method) {
        return parameterTypes(method).stream().filter(type -> !isType(type, PAGE_REQUEST))
                .map(type -> (TypeMirror) type).toList();
    }

    private static boolean isType(TypeMirror type, String qualifiedName) {
        return type.getKind() == TypeKind.DECLARED
                && ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().contentEquals(qualifiedName);
    }

    private enum PageMode { NONE, OFFSET, CURSOR }

    private record Paging(PageMode mode, String problem) {
    }

    private Paging paging(ExecutableElement method) {
        TypeMirror result = returnType(method);
        PageMode mode = isType(result, PAGE) ? PageMode.OFFSET
                : isType(result, CURSORED_PAGE) ? PageMode.CURSOR : PageMode.NONE;
        long requests = parameterTypes(method).stream().filter(type -> isType(type, PAGE_REQUEST)).count();
        if (mode == PageMode.NONE) {
            return new Paging(mode, requests == 0 ? null
                    : "a PageRequest parameter needs a Page or CursoredPage return type");
        }
        if (requests != 1) {
            return new Paging(mode, "a method returning a page needs exactly one PageRequest parameter");
        }
        if (mode == PageMode.CURSOR) {
            return new Paging(mode, CursorOrdering.problem(method, identifier()));
        }
        return new Paging(mode, null);
    }

    private String identifier() {
        return new PropertyScanner(elements, types).scan((TypeElement) types.asElement(repository.entity())).stream()
                .filter(Property::identifier).map(Property::name).findFirst().orElse(null);
    }

    private long restrictionParameters(ExecutableElement method) {
        return parameterTypes(method).stream().filter(type -> type.getKind() == TypeKind.DECLARED
                && ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName()
                .contentEquals("jakarta.data.restrict.Restriction")).count();
    }

    private boolean isEntityRestriction(TypeMirror type) {
        return type.getKind() == TypeKind.DECLARED
                && ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName()
                .contentEquals("jakarta.data.restrict.Restriction")
                && ((DeclaredType) type).getTypeArguments().size() == 1
                && types.isSameType(((DeclaredType) type).getTypeArguments().get(0), repository.entity());
    }

    private Classified classifyOperation(ExecutableElement method, boolean standardOwner) {
        String name = method.getSimpleName().toString();
        int arity = method.getParameters().size();
        boolean dataAnnotated = hasDataAnnotation(method);
        if (standardOwner || !dataAnnotated) {
            Classified standard = standard(name, arity, standardOwner);
            if (standard != null) {
                return standard;
            }
            if (standardOwner) {
                return Classified.unsupported("bulk, paginated and other repository operations are not supported "
                        + "by the generated REST resource");
            }
        }
        if (Annotations.has(method, QUERY)) {
            String text = Annotations.stringValue(method, QUERY).orElse("").stripLeading().toUpperCase();
            if (text.startsWith("DELETE")) {
                return Classified.explicit(Kind.QUERY_DELETE, RestOperation.DELETE);
            }
            if (text.startsWith("UPDATE")) {
                return Classified.explicit(Kind.QUERY_UPDATE, RestOperation.UPDATE);
            }
            return Classified.of(Kind.READ, RestOperation.FIND);
        }
        if (Annotations.has(method, FIND)) {
            return Classified.of(Kind.READ, RestOperation.FIND);
        }
        if (Annotations.has(method, INSERT)) {
            return Classified.of(Kind.LIFECYCLE_INSERT, RestOperation.INSERT);
        }
        if (Annotations.has(method, SAVE)) {
            return Classified.of(Kind.LIFECYCLE_SAVE, RestOperation.SAVE);
        }
        if (Annotations.has(method, UPDATE)) {
            return Classified.of(Kind.LIFECYCLE_UPDATE, RestOperation.UPDATE);
        }
        if (Annotations.has(method, DELETE)) {
            return hasEntityParameter(method) ? Classified.of(Kind.LIFECYCLE_DELETE, RestOperation.DELETE)
                    : Classified.explicit(Kind.QUERY_DELETE, RestOperation.DELETE);
        }
        if (name.startsWith("findBy")) {
            return Classified.of(Kind.READ, RestOperation.FIND);
        }
        if (name.startsWith("countBy")) {
            return Classified.of(Kind.COUNT, RestOperation.COUNT);
        }
        if (name.startsWith("existsBy")) {
            return Classified.of(Kind.EXISTS, RestOperation.EXISTS);
        }
        if (name.startsWith("deleteBy")) {
            return Classified.explicit(Kind.QUERY_DELETE, RestOperation.DELETE);
        }
        return Classified.unsupported("it is neither annotated with a Jakarta Data operation annotation nor "
                + "named as a findBy, countBy, existsBy or deleteBy query");
    }

    private Classified standard(String name, int arity, boolean standardOwner) {
        return switch (name + "/" + arity) {
            case "findById/1" -> standardOwner || repository.hasFindById()
                    ? Classified.of(Kind.FIND_BY_ID, RestOperation.FIND) : null;
            case "findAll/0" -> Classified.of(Kind.FIND_ALL, RestOperation.FIND);
            case "insert/1" -> Classified.of(Kind.INSERT, RestOperation.INSERT);
            case "update/1" -> Classified.of(Kind.UPDATE, RestOperation.UPDATE);
            case "save/1" -> Classified.of(Kind.SAVE, RestOperation.SAVE);
            case "deleteById/1" -> Classified.of(Kind.DELETE_BY_ID, RestOperation.DELETE);
            case "delete/1" -> Classified.of(Kind.DELETE, RestOperation.DELETE);
            default -> null;
        };
    }

    private boolean hasDataAnnotation(ExecutableElement method) {
        return List.of(QUERY, FIND, INSERT, SAVE, UPDATE, DELETE).stream()
                .anyMatch(annotation -> Annotations.has(method, annotation));
    }

    private boolean hasEntityParameter(ExecutableElement method) {
        return method.getParameters().size() == 1 && types.isSameType(
                parameterTypes(method).get(0), repository.entity());
    }

    private List<? extends TypeMirror> parameterTypes(ExecutableElement method) {
        return ((ExecutableType) types.asMemberOf(repository.type(), method)).getParameterTypes();
    }

    private TypeMirror returnType(ExecutableElement method) {
        return ((ExecutableType) types.asMemberOf(repository.type(), method)).getReturnType();
    }

    private String repositoryName() {
        return imports.name(repository.element());
    }

    private String entityType() {
        return imports.render(repository.entity());
    }

    private String dtoType() {
        return imports.name(dto.qualified(dto.dtoName()));
    }

    private String representation() {
        return dto == null ? entityType() : dtoType();
    }

    private String idType() {
        return imports.render(repository.id());
    }

    private void generate(ExecutableElement method, Classified classified, String pathOverride,
                          boolean standardOwner) {
        String name = method.getSimpleName().toString();
        Spec spec = switch (classified.kind()) {
            case FIND_BY_ID -> findById();
            case FIND_ALL -> findAll();
            case INSERT -> insert("insert", "POST", "", method, standardOwner);
            case UPDATE -> update();
            case SAVE -> save("save", "/save", method, standardOwner);
            case DELETE_BY_ID -> deleteById();
            case DELETE -> deleteEntity("delete", "/delete", method);
            case LIFECYCLE_INSERT -> insert(name, "POST", pathOr(pathOverride, name), method, false);
            case LIFECYCLE_SAVE -> save(name, pathOr(pathOverride, name), method, false);
            case LIFECYCLE_UPDATE -> lifecycleUpdate(method, pathOr(pathOverride, name));
            case LIFECYCLE_DELETE -> deleteEntity(name, pathOr(pathOverride, name), method);
            case READ, COUNT, EXISTS -> read(method, classified, pathOr(pathOverride, name));
            case QUERY_DELETE -> mutation(method, "DELETE", pathOr(pathOverride, name));
            case QUERY_UPDATE -> mutation(method, "POST", pathOr(pathOverride, name));
            case SEARCH -> search(method, pathOr(pathOverride, name));
        };
        if (spec == null) {
            return;
        }
        register(method, standardOwner, spec);
    }

    private String pathOr(String override, String name) {
        return override.isEmpty() ? "/" + name : override;
    }

    private void register(ExecutableElement method, boolean standardOwner, Spec spec) {
        String route = spec.http + " " + normalize(spec.path);
        String previous = routes.putIfAbsent(route, method.getSimpleName().toString());
        if (previous != null) {
            diagnostics.error(standardOwner ? repository.element() : method, "Both " + previous + " and "
                    + method.getSimpleName() + " would handle " + spec.http + " " + repository.path() + spec.path
                    + ". Use @RestExpose(path = ...) or @RestExclude to resolve the conflict.");
            return;
        }
        List<String> annotations = new ArrayList<>();
        annotations.add("@" + imports.name((spec.http.equals("QUERY") ? "org.eclipse.jnosql.rest." : "jakarta.ws.rs.")
                + spec.http));
        if (!spec.path.isEmpty()) {
            annotations.add("@" + imports.name("jakarta.ws.rs.Path") + "(" + AnnotationSource.quote(spec.path) + ")");
        }
        for (var mirror : openApi.forMethod(method)) {
            annotations.add(annotationSource.write(mirror));
        }
        String javadoc = "Handles {@code " + spec.http + " " + repository.path() + spec.path
                + "} by delegating to {@link " + repositoryName() + "#" + method.getSimpleName() + "}.";
        methods.add(new MethodModel(javadoc, annotations, spec.returnType, spec.name, renderParameters(spec.parameters),
                spec.body));
    }

    private static String normalize(String path) {
        String normalized = TEMPLATE.matcher(path).replaceAll("{}");
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private static String renderParameters(List<String> parameters) {
        if (parameters.isEmpty() || parameters.size() == 1 && parameters.get(0).length() <= 70) {
            return String.join("", parameters);
        }
        return "\n" + String.join(",\n", parameters.stream().map(parameter -> INDENT.repeat(3) + parameter).toList())
;
    }

    private static final class Spec {
        private final String http;
        private final String path;
        private String returnType;
        private final String name;
        private final List<String> parameters = new ArrayList<>();
        private final List<String> body = new ArrayList<>();

        private Spec(String http, String path, String returnType, String name) {
            this.http = http;
            this.path = path;
            this.returnType = returnType;
            this.name = name;
        }

        private void returnType(String type) {
            this.returnType = type;
        }

        private Spec parameter(String text) {
            parameters.add(text);
            return this;
        }

        private Spec line(String text) {
            body.add(text);
            return this;
        }
    }

    private Spec findById() {
        Spec spec = new Spec("GET", "/{id}", representation(), "findById")
                .parameter("@" + imports.name("jakarta.ws.rs.PathParam") + "(\"id\") " + idType() + " id");
        spec.line("return repository.findById(id)");
        if (dto != null) {
            spec.line(INDENT + INDENT + ".map(entity -> mapper.toDTO(entity))");
        }
        return spec.line(INDENT + INDENT + ".orElseThrow(" + imports.name("jakarta.ws.rs.NotFoundException")
                + "::new);");
    }

    private Spec findAll() {
        Spec spec = new Spec("GET", "", imports.name("java.util.List") + "<" + representation() + ">", "findAll");
        String stream = imports.name("java.util.stream.Stream");
        spec.line("try (" + stream + "<" + entityType() + "> stream = repository.findAll()) {");
        spec.line(INDENT + (dto == null ? "return stream.toList();" : "return mapper.toDTO(stream.toList());"));
        return spec.line("}");
    }

    private Spec deleteById() {
        return new Spec("DELETE", "/{id}", "void", "deleteById")
                .parameter("@" + imports.name("jakarta.ws.rs.PathParam") + "(\"id\") " + idType() + " id")
                .line("repository.deleteById(id);");
    }

    private void checkBody(Spec spec, String name) {
        spec.line("if (" + name + " == null) {");
        spec.line(INDENT + "throw new " + imports.name("jakarta.ws.rs.BadRequestException")
                + "(\"The request body is required\");");
        spec.line("}");
    }

    private boolean createRequest() {
        if (dto != null && !planner.requireCreate(dto)) {
            diagnostics.error(repository.element(), "Entity " + dto.entity().getQualifiedName()
                    + " has no writable attributes, so no create request DTO can be generated.");
            return false;
        }
        return true;
    }

    private String createType() {
        return imports.name(dto.qualified(dto.createName()));
    }

    private Spec insert(String name, String http, String path, ExecutableElement method, boolean standardOwner) {
        if (!standardOwner && (!returnsVoidOrEntity(method) || !hasEntityParameter(method)) || !createRequest()) {
            return unsupportedSignature(method, "an insert must take the entity and return it or nothing");
        }
        Spec spec = new Spec(http, path, imports.name("jakarta.ws.rs.core.Response"), name);
        String call = "repository." + method.getSimpleName() + "(entity)";
        String entityType = entityType();
        if (dto == null) {
            spec.parameter(openApiBody(method) + entityType + " entity");
            checkBody(spec, "entity");
        } else {
            spec.parameter(openApiBody(method) + createType() + " request");
            checkBody(spec, "request");
            spec.line(entityType + " entity = mapper.toEntity(request);");
        }
        boolean returns = method.getReturnType().getKind() != TypeKind.VOID;
        spec.line(returns ? entityType + " created = " + call + ";" : call + ";");
        String created = returns ? "created" : "entity";
        return spec.line("return " + imports.name("jakarta.ws.rs.core.Response") + ".status("
                + imports.name("jakarta.ws.rs.core.Response") + ".Status.CREATED)")
                .line(INDENT + INDENT + ".entity(" + (dto == null ? created : "mapper.toDTO(" + created + ")") + ")")
                .line(INDENT + INDENT + ".build();");
    }

    private Spec update() {
        if (dto == null) {
            return new Spec("PUT", "", entityType(), "update")
                    .parameter(entityType() + " entity")
                    .line("if (entity == null) {")
                    .line(INDENT + "throw new " + imports.name("jakarta.ws.rs.BadRequestException")
                            + "(\"The request body is required\");")
                    .line("}")
                    .line("return repository.update(entity);");
        }
        if (!repository.hasFindById() || !planner.requireUpdate(dto)) {
            diagnostics.warning(repository.element(), "Not exposed over REST: update - DTO updates need "
                    + "BasicRepository.findById and an entity with writable, non-identifier attributes that is not "
                    + "a record.");
            return null;
        }
        String updateType = imports.name(dto.qualified(dto.updateName()));
        Spec spec = new Spec("PUT", "/{id}", dtoType(), "update")
                .parameter("@" + imports.name("jakarta.ws.rs.PathParam") + "(\"id\") " + idType() + " id")
                .parameter(updateType + " request");
        checkBody(spec, "request");
        return spec
                .line(entityType() + " entity = repository.findById(id)")
                .line(INDENT + INDENT + ".orElseThrow(" + imports.name("jakarta.ws.rs.NotFoundException") + "::new);")
                .line("mapper.updateEntity(request, entity);")
                .line("return mapper.toDTO(repository.update(entity));");
    }

    private Spec lifecycleUpdate(ExecutableElement method, String path) {
        if (dto != null || !returnsVoidOrEntity(method) || !hasEntityParameter(method)) {
            return unsupportedSignature(method, "an update with an entity parameter is only available without "
                    + "@RestDTO and must return the entity or nothing");
        }
        boolean returns = method.getReturnType().getKind() != TypeKind.VOID;
        Spec spec = new Spec("PUT", path, returns ? entityType() : "void", method.getSimpleName().toString())
                .parameter(openApiBody(method) + entityType() + " entity");
        checkBody(spec, "entity");
        return spec.line((returns ? "return " : "") + "repository." + method.getSimpleName() + "(entity);");
    }

    private Spec save(String name, String path, ExecutableElement method, boolean standardOwner) {
        if (!standardOwner && (!returnsVoidOrEntity(method) || !hasEntityParameter(method)) || !createRequest()) {
            return unsupportedSignature(method, "a save must take the entity and return it or nothing");
        }
        boolean returns = standardOwner || method.getReturnType().getKind() != TypeKind.VOID;
        Spec spec = new Spec("PUT", path, returns ? representation() : "void", name);
        String call = "repository." + method.getSimpleName() + "(entity)";
        if (dto == null) {
            spec.parameter(openApiBody(method) + entityType() + " entity");
            checkBody(spec, "entity");
            return spec.line((returns ? "return " : "") + call + ";");
        }
        spec.parameter(openApiBody(method) + createType() + " request");
        checkBody(spec, "request");
        spec.line(entityType() + " entity = mapper.toEntity(request);");
        return returns ? spec.line("return mapper.toDTO(" + call + ");") : spec.line(call + ";");
    }

    private Spec deleteEntity(String name, String path, ExecutableElement method) {
        if (dto != null || !hasEntityParameter(method) || method.getReturnType().getKind() != TypeKind.VOID) {
            return unsupportedSignature(method, "deleting by entity needs the entity in the request body, which is "
                    + "only available without @RestDTO; use deleteById instead");
        }
        Spec spec = new Spec("DELETE", path, "void", name).parameter(openApiBody(method) + entityType() + " entity");
        checkBody(spec, "entity");
        return spec.line("repository." + method.getSimpleName() + "(entity);");
    }

    private String openApiBody(ExecutableElement method) {
        if (method.getParameters().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (var mirror : openApi.forBody(method.getParameters().get(0))) {
            builder.append(annotationSource.write(mirror)).append(' ');
        }
        return builder.toString();
    }

    private boolean returnsVoidOrEntity(ExecutableElement method) {
        TypeMirror type = returnType(method);
        return type.getKind() == TypeKind.VOID || types.isSameType(type, repository.entity())
                || method.getSimpleName().contentEquals("save") && type.getKind() == TypeKind.TYPEVAR;
    }

    private Spec unsupportedSignature(ExecutableElement method, String reason) {
        boolean explicit = method.getAnnotation(RestExpose.class) != null;
        String message = "Not exposed over REST: " + method.getSimpleName() + " - " + reason + ".";
        if (explicit) {
            diagnostics.error(method, message);
        } else {
            diagnostics.warning(method, message);
        }
        return null;
    }

    private Spec read(ExecutableElement method, Classified classified, String path) {
        Paging paging = paging(method);
        if (paging.problem() != null) {
            return unsupportedSignature(method, paging.problem());
        }
        Spec spec = parameters(method, path, "GET", paging);
        if (spec == null) {
            return null;
        }
        String call = "repository." + method.getSimpleName() + "(" + arguments(method) + ")";
        TypeMirror type = returnType(method);
        if (!readBody(spec, type, call, method)) {
            return null;
        }
        return spec;
    }

    private boolean searchPaginated;

    private Spec search(ExecutableElement method, String path) {
        if (TEMPLATE.matcher(path).find()) {
            return unsupportedSignature(method, "a search path cannot contain path variables because the criteria "
                    + "travel in the request body");
        }
        Paging paging = paging(method);
        if (paging.problem() != null) {
            return unsupportedSignature(method, paging.problem());
        }
        boolean paged = paging.mode() != PageMode.NONE;
        if (searchRequestResolved && searchPaginated != paged) {
            diagnostics.error(method, "All dynamic searches of " + repository.element().getSimpleName()
                    + " must either paginate or not, because they share one " + searchRequestName() + ".");
            return null;
        }
        searchPaginated = paged;
        Spec spec = new Spec("QUERY", path, "void", method.getSimpleName().toString());
        String call = "repository." + method.getSimpleName() + "("
                + method.getParameters().stream().map(parameter -> isType(parameter.asType(), PAGE_REQUEST)
                        ? variableName(parameter) : "request.toRestriction()").collect(
                        java.util.stream.Collectors.joining(", ")) + ")";
        if (paged) {
            String pagination = imports.name(PAGINATION);
            VariableElement pageParameter = method.getParameters().stream()
                    .filter(parameter -> isType(parameter.asType(), PAGE_REQUEST)).findFirst().orElseThrow();
            if (paging.mode() == PageMode.OFFSET) {
                spec.line(imports.name(PAGE_REQUEST) + " " + variableName(pageParameter) + " = " + pagination
                        + ".offset(request.pagination());");
            } else {
                spec.line("String cursorContext = " + pagination + ".context(\"" + method.getSimpleName()
                        + "\", request.criteria());");
                spec.line(imports.name(PAGE_REQUEST) + " " + variableName(pageParameter) + " = " + pagination
                        + ".cursor(request.pagination(), cursorContext);");
            }
        }
        List<String> prelude = new ArrayList<>(spec.body);
        spec.body.clear();
        if (!readBody(spec, returnType(method), call, method) || !resolveSearchRequest(method)) {
            return null;
        }
        spec.body.addAll(0, prelude);
        Spec guard = new Spec("QUERY", path, "void", spec.name);
        guard.parameter(openApiBody(method) + imports.name(searchRequestName()) + " request");
        checkBody(guard, "request");
        spec.parameters.addAll(guard.parameters);
        spec.body.addAll(0, guard.body);
        return spec;
    }

    private String searchRequestName() {
        String simpleName = SearchPlanner.requestName(repository);
        return repository.packageName().isEmpty() ? simpleName : repository.packageName() + "." + simpleName;
    }

    private boolean resolveSearchRequest(ExecutableElement method) {
        if (searchRequestResolved) {
            return true;
        }
        TypeElement existing = elements.getTypeElement(searchRequestName());
        if (existing == null) {
            searchRequest = searchPlanner.plan(repository, searchPaginated);
            searchRequestResolved = searchRequest != null;
            return searchRequestResolved;
        }
        boolean convertible = ElementFilter.methodsIn(existing.getEnclosedElements()).stream()
                .anyMatch(candidate -> candidate.getSimpleName().contentEquals("toRestriction")
                        && candidate.getParameters().isEmpty() && candidate.getModifiers().contains(Modifier.PUBLIC));
        if (!convertible) {
            diagnostics.error(method, "The developer-defined " + existing.getQualifiedName() + " must declare a "
                    + "public toRestriction() method returning the Jakarta Data restriction.");
        }
        if (convertible && searchPaginated) {
            Set<String> declared = ElementFilter.methodsIn(existing.getEnclosedElements()).stream()
                    .map(candidate -> candidate.getSimpleName().toString()).collect(
                            java.util.stream.Collectors.toSet());
            if (!declared.containsAll(Set.of("pagination", "criteria"))) {
                diagnostics.error(method, "The developer-defined " + existing.getQualifiedName() + " must also "
                        + "declare pagination() returning PaginationRequest and criteria() returning a String.");
                convertible = false;
            }
        }
        searchRequestResolved = convertible;
        return convertible;
    }

    private Spec mutation(ExecutableElement method, String http, String path) {
        Paging paging = paging(method);
        if (paging.problem() != null) {
            return unsupportedSignature(method, paging.problem());
        }
        Spec spec = parameters(method, path, http, paging);
        if (spec == null) {
            return null;
        }
        TypeMirror type = returnType(method);
        String call = "repository." + method.getSimpleName() + "(" + arguments(method) + ")";
        if (type.getKind() != TypeKind.VOID && !ScalarTypes.isScalar(type)) {
            return unsupportedSignature(method, "a data-changing query must return void or a number");
        }
        spec.returnType(imports.render(type));
        return spec.line((type.getKind() == TypeKind.VOID ? "" : "return ") + call + ";");
    }

    private boolean readBody(Spec spec, TypeMirror type, String call, ExecutableElement method) {
        String list = imports.name("java.util.List");
        String notFound = imports.name("jakarta.ws.rs.NotFoundException");
        if (type.getKind() == TypeKind.VOID) {
            unsupportedSignature(method, "a read operation must return a value");
            return false;
        }
        if (type.getKind().isPrimitive() || ScalarTypes.isScalar(type)) {
            spec.returnType(imports.render(type));
            spec.line("return " + call + ";");
            return true;
        }
        if (type.getKind() != TypeKind.DECLARED) {
            unsupportedSignature(method, "unsupported return type " + type);
            return false;
        }
        DeclaredType declared = (DeclaredType) type;
        String name = ((TypeElement) declared.asElement()).getQualifiedName().toString();
        List<? extends TypeMirror> arguments = declared.getTypeArguments();
        if (arguments.size() == 1 && (name.equals(PAGE) || name.equals(CURSORED_PAGE))) {
            TypeMirror element = arguments.get(0);
            boolean mapped = dto != null && types.isSameType(element, repository.entity());
            boolean cursor = name.equals(CURSORED_PAGE);
            String response = imports.name("org.eclipse.jnosql.rest.pagination."
                    + (cursor ? "CursorPageResponse" : "PageResponse"));
            spec.returnType(response + "<" + (mapped ? dtoType() : imports.render(element)) + ">");
            spec.line(imports.name(name) + "<" + imports.render(element) + "> page = " + call + ";");
            spec.line("return " + response + ".of(page" + (cursor ? ", cursorContext" : "")
                    + (mapped ? ", content -> mapper.toDTO(content)" : "") + ");");
            return true;
        }
        if (arguments.size() == 1 && Set.of("java.util.Optional", "java.util.List", "java.util.stream.Stream")
                .contains(name)) {
            TypeMirror element = arguments.get(0);
            boolean mapped = dto != null && types.isSameType(element, repository.entity());
            String shown = mapped ? dtoType() : imports.render(element);
            if (name.equals("java.util.Optional")) {
                spec.returnType(shown);
                spec.line("return " + call);
                if (mapped) {
                    spec.line(INDENT + INDENT + ".map(entity -> mapper.toDTO(entity))");
                }
                spec.line(INDENT + INDENT + ".orElseThrow(" + notFound + "::new);");
            } else if (name.equals("java.util.List")) {
                spec.returnType(list + "<" + shown + ">");
                spec.line(mapped ? "return mapper.toDTO(" + call + ");" : "return " + call + ";");
            } else {
                spec.returnType(list + "<" + shown + ">");
                spec.line("try (" + imports.name("java.util.stream.Stream") + "<" + imports.render(element)
                        + "> stream = " + call + ") {");
                spec.line(INDENT + (mapped ? "return mapper.toDTO(stream.toList());" : "return stream.toList();"));
                spec.line("}");
            }
            return true;
        }
        if (arguments.isEmpty() && !name.startsWith("java.")) {
            boolean mapped = dto != null && types.isSameType(type, repository.entity());
            spec.returnType(mapped ? dtoType() : imports.render(type));
            spec.line((mapped ? entityType() : imports.render(type)) + " result = " + call + ";");
            spec.line("if (result == null) {");
            spec.line(INDENT + "throw new " + notFound + "();");
            spec.line("}");
            spec.line(mapped ? "return mapper.toDTO(result);" : "return result;");
            return true;
        }
        unsupportedSignature(method, "the return type " + type + " is not supported; use a single value, "
                + "Optional, List, Stream, Page or CursoredPage");
        return false;
    }

    private String arguments(ExecutableElement method) {
        return String.join(", ", method.getParameters().stream().map(this::variableName).toList());
    }

    private String variableName(VariableElement parameter) {
        String name = parameter.getSimpleName().toString();
        return RESERVED.contains(name) ? name + "Value" : name;
    }

    private Spec parameters(ExecutableElement method, String path, String http, Paging paging) {
        Matcher matcher = TEMPLATE.matcher(path);
        Set<String> templated = new java.util.HashSet<>();
        while (matcher.find()) {
            templated.add(matcher.group(1));
        }
        List<? extends TypeMirror> parameterTypes = parameterTypes(method);
        Spec spec = new Spec(http, path, "void", method.getSimpleName().toString());
        List<String> checks = new ArrayList<>();
        Set<String> seen = new java.util.HashSet<>();
        String pageVariable = null;
        for (int index = 0; index < parameterTypes.size(); index++) {
            VariableElement parameter = method.getParameters().get(index);
            String original = parameter.getSimpleName().toString();
            TypeMirror type = parameterTypes.get(index);
            if (isType(type, PAGE_REQUEST)) {
                pageVariable = variableName(parameter);
                continue;
            }
            String problem = parameterProblem(type);
            if (problem != null) {
                return unsupportedSignature(method, "parameter '" + original + "' " + problem);
            }
            if (type.getKind().isPrimitive()) {
                type = types.boxedClass((PrimitiveType) type).asType();
            }
            seen.add(original);
            StringBuilder text = new StringBuilder();
            for (var mirror : openApi.forQueryParameter(parameter)) {
                text.append(annotationSource.write(mirror)).append(' ');
            }
            boolean pathParameter = templated.contains(original);
            text.append('@').append(imports.name(pathParameter ? "jakarta.ws.rs.PathParam" : "jakarta.ws.rs.QueryParam"))
                    .append("(\"").append(original).append("\") ")
                    .append(imports.render(type)).append(' ').append(variableName(parameter));
            spec.parameter(text.toString());
            if (!pathParameter) {
                checks.add(variableName(parameter));
            }
        }
        for (String name : templated) {
            if (!seen.contains(name)) {
                diagnostics.error(method, "The path of " + method.getSimpleName() + " refers to {" + name
                        + "}, but the method has no parameter with that name.");
                return null;
            }
        }
        for (String check : checks) {
            spec.line("if (" + check + " == null) {");
            spec.line(INDENT + "throw new " + imports.name("jakarta.ws.rs.BadRequestException")
                    + "(\"Query parameter '" + check.replaceAll("Value$", "") + "' is required\");");
            spec.line("}");
        }
        if (pageVariable != null && !addPagination(spec, method, paging, pageVariable, seen)) {
            return null;
        }
        return spec;
    }

    private boolean addPagination(Spec spec, ExecutableElement method, Paging paging, String variable,
                                  Set<String> seen) {
        for (String name : PAGINATION_PARAMETERS) {
            if (seen.contains(name)) {
                diagnostics.error(method, "The query parameter '" + name + "' of " + method.getSimpleName()
                        + " clashes with the pagination parameters of the generated endpoint.");
                return false;
            }
        }
        String pagination = imports.name(PAGINATION);
        String pageRequest = imports.name(PAGE_REQUEST);
        spec.parameter(pageParameter("size", "Maximum number of elements per page, between 1 and 100; default 20"));
        if (paging.mode() == PageMode.OFFSET) {
            spec.parameter(pageParameter("offset", "Zero-based page offset (0 is the first page); default 0"));
            spec.parameter(pageParameter("includeTotal", "Set to true to include totalElements and totalPages; "
                    + "default false"));
            spec.line(pageRequest + " " + variable + " = " + pagination + ".offset(offset, size, includeTotal);");
        } else {
            spec.parameter(pageParameter("cursor", "Opaque continuation token from nextCursor or previousCursor; "
                    + "omit it for the first page"));
            List<String> values = method.getParameters().stream()
                    .filter(parameter -> !isType(parameter.asType(), PAGE_REQUEST)).map(this::variableName).toList();
            spec.line("String cursorContext = " + pagination + ".context(\"" + method.getSimpleName() + "\""
                    + values.stream().map(value -> ", " + value).collect(java.util.stream.Collectors.joining())
                    + ");");
            spec.line(pageRequest + " " + variable + " = " + pagination + ".cursor(cursor, size, cursorContext);");
        }
        return true;
    }

    private String pageParameter(String name, String description) {
        String documentation = openApi.available()
                ? "@" + imports.name("org.eclipse.microprofile.openapi.annotations.parameters.Parameter")
                + "(description = \"" + description + "\") " : "";
        return documentation + "@" + imports.name("jakarta.ws.rs.QueryParam") + "(\"" + name + "\") String " + name;
    }

    private String parameterProblem(TypeMirror type) {
        if (type.getKind().isPrimitive() || ScalarTypes.isScalar(type)) {
            return null;
        }
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declared = (DeclaredType) type;
            String name = ((TypeElement) declared.asElement()).getQualifiedName().toString();
            if (name.startsWith("jakarta.data.restrict.")) {
                return "is a restriction, which is only supported as the single parameter of a search method";
            }
            if (name.startsWith("jakarta.data.page.") || name.equals("jakarta.data.Limit")
                    || name.equals("jakarta.data.Sort") || name.equals("jakarta.data.Order")) {
                return "is a pagination or sorting parameter (" + name + "), which is not supported yet";
            }
            if (Set.of("java.util.List", "java.util.Set", "java.util.Collection").contains(name)
                    && declared.getTypeArguments().size() == 1
                    && ScalarTypes.isScalar(declared.getTypeArguments().get(0))) {
                return null;
            }
        }
        return "has type " + type + ", which cannot be bound to a query parameter";
    }
}
