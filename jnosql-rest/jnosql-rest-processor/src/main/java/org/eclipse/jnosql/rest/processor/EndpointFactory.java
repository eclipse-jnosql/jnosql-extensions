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
            "stream", "created", "response", "page", "offset", "size", "cursor", "includeTotal", "cursorContext", "limit", "startAt",
            "sort");
    private static final String BY = DATA_PACKAGE + "By";
    private static final String WS_PACKAGE = "jakarta.ws.rs.";
    private static final String PATH = WS_PACKAGE + "Path";
    private static final String PATH_PARAM = WS_PACKAGE + "PathParam";
    private static final String QUERY_PARAM = WS_PACKAGE + "QueryParam";
    private static final Set<String> UNSUPPORTED_BINDINGS = Set.of(WS_PACKAGE + "HeaderParam",
            WS_PACKAGE + "CookieParam", WS_PACKAGE + "MatrixParam", WS_PACKAGE + "FormParam",
            WS_PACKAGE + "BeanParam", WS_PACKAGE + "core.Context");
    private static final String LIMIT = "jakarta.data.Limit";
    private static final String SORT = "jakarta.data.Sort";
    private static final String ORDER = "jakarta.data.Order";
    private static final String SORTING = "org.eclipse.jnosql.rest.pagination.Sorting";
    private static final String LIMITS = "org.eclipse.jnosql.rest.pagination.Limits";
    private static final String SIZE_HELP = "Maximum number of elements per page, between 1 and 100; default 20";
    private static final String OFFSET_HELP = "Zero-based page offset (0 is the first page); default 0";
    private static final String TOTAL_HELP = "Set to true to include totalElements and totalPages; default false";
    private static final String SORT_HELP = "Comma-separated attribute:direction criteria, such as name:asc,price:desc";
    private static final String LIMIT_HELP = "Maximum number of results, between 1 and 100; default 20";
    private static final String START_HELP = "One-based position of the first result; default 1";
    private static final String PAGE_PACKAGE = "jakarta.data.page.";
    private static final String PAGE = PAGE_PACKAGE + "Page";
    private static final String CURSORED_PAGE = PAGE_PACKAGE + "CursoredPage";
    private static final String PAGE_REQUEST = PAGE_PACKAGE + "PageRequest";
    private static final String PAGINATION = "org.eclipse.jnosql.rest.pagination.Pagination";
    private static final List<String> PAGINATION_PARAMETERS = List.of("offset", "size", "includeTotal", "cursor");
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
        boolean explicit = Annotations.has(method, PATH);
        Element target = standardOwner ? repository.element() : method;
        if (method.getAnnotation(RestExclude.class) != null || repository.exposure() == RestExposure.NONE) {
            return;
        }
        if (classified.unsupported() != null) {
            if (explicit) {
                diagnostics.error(method, "@Path cannot expose " + method.getSimpleName() + ": "
                        + classified.unsupported());
            } else if (repository.exposure() == RestExposure.ALL) {
                report(standardOwner, target, "Not exposed over REST: " + method.getSimpleName() + " - "
                        + classified.unsupported());
            }
            return;
        }
        if (repository.excluded().contains(classified.operation())) {
            if (explicit) {
                diagnostics.error(method, "@Path on " + method.getSimpleName() + " contradicts "
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
                    ? " accepts a dynamic Restriction; add a method-level @Path to publish it as a QUERY search "
                    + "endpoint."
                    : " changes data through a query; add a method-level @Path to publish it."));
            return;
        }
        UriTemplate declared = null;
        if (explicit) {
            declared = UriTemplate.of(Annotations.rawStringValue(method, PATH).orElse(""));
            if (declared.problem() != null) {
                diagnostics.error(method, "The @Path of " + method.getSimpleName() + " is not a valid URI template: "
                        + declared.problem() + ".");
                return;
            }
        }
        generate(method, classified, declared, standardOwner);
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
        if (classified.unsupported() == null && classified.kind() != Kind.FIND_ALL && sortingProblem(method) != null) {
            return Classified.unsupported(sortingProblem(method));
        }
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
                    + "PageRequest and an optional Sort or Order of the entity, and be a Restriction of the "
                    + "repository entity");
        }
        if (parameterTypes(method).stream().anyMatch(type -> isType(type, LIMIT))) {
            return Classified.unsupported("a dynamic restriction search cannot take a Limit; use a PageRequest");
        }
        return Classified.explicit(Kind.SEARCH, classified.operation());
    }

    private List<TypeMirror> nonPagingParameters(ExecutableElement method) {
        return parameterTypes(method).stream()
                .filter(type -> !isType(type, PAGE_REQUEST) && !isType(type, SORT) && !isType(type, ORDER)
                        && !isType(type, LIMIT))
                .map(type -> (TypeMirror) type).toList();
    }

    private boolean hasType(ExecutableElement method, String qualifiedName) {
        return parameterTypes(method).stream().anyMatch(type -> isType(type, qualifiedName));
    }

    private boolean sorted(ExecutableElement method) {
        return hasType(method, SORT) || hasType(method, ORDER);
    }

    private boolean isEntityOf(TypeMirror type) {
        return type.getKind() == TypeKind.DECLARED && ((DeclaredType) type).getTypeArguments().size() == 1
                && types.isSameType(((DeclaredType) type).getTypeArguments().get(0), repository.entity());
    }

    private String sortingProblem(ExecutableElement method) {
        long sorts = parameterTypes(method).stream().filter(type -> isType(type, SORT) || isType(type, ORDER)).count();
        long limits = parameterTypes(method).stream().filter(type -> isType(type, LIMIT)).count();
        if (sorts > 1) {
            return "a method takes at most one Sort or Order parameter, because the sort query parameter feeds it";
        }
        if (limits > 1) {
            return "a method takes at most one Limit parameter";
        }
        if (parameterTypes(method).stream().filter(type -> isType(type, SORT) || isType(type, ORDER))
                .anyMatch(type -> !isEntityOf(type))) {
            return "the type argument of Sort and Order must be the repository entity "
                    + types.asElement(repository.entity()).getSimpleName();
        }
        if (limits == 1 && (hasType(method, PAGE_REQUEST) || isType(returnType(method), PAGE)
                || isType(returnType(method), CURSORED_PAGE))) {
            return "a Limit cannot be combined with pagination; use a PageRequest alone";
        }
        return null;
    }

    private Set<String> sortable() {
        TypeElement entity = (TypeElement) types.asElement(repository.entity());
        Set<String> names = new java.util.LinkedHashSet<>();
        for (Property property : new PropertyScanner(elements, types).scan(entity)) {
            if (!repository.dtoExclusions().contains(property.name()) && ScalarTypes.isScalar(property.type())) {
                names.add(property.name());
            }
        }
        if (dto != null) {
            TypeElement representation = elements.getTypeElement(dto.qualified(dto.dtoName()));
            if (representation != null) {
                names.retainAll(new PropertyScanner(elements, types).scan(representation).stream()
                        .map(Property::name).collect(java.util.stream.Collectors.toSet()));
            }
        }
        return names;
    }

    private String sortableSet() {
        return imports.name("java.util.Set") + ".of(" + sortable().stream().map(AnnotationSource::quote)
                .collect(java.util.stream.Collectors.joining(", ")) + ")";
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
            if (hasType(method, ORDER)) {
                String id = identifier();
                return new Paging(mode, id != null && sortable().contains(id) ? null
                        : "cursor pagination with an Order needs the identifier among the sortable attributes");
            }
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
        if (name.equals("findAll") && (standardOwner || !dataAnnotated)) {
            if (arity == 0) {
                return Classified.unsupported("an unbounded findAll() would load the whole collection; the "
                        + "collection root is served by the paginated findAll(PageRequest, Order) instead");
            }
            if (arity == 2 && hasType(method, PAGE_REQUEST) && hasType(method, ORDER)
                    && isType(returnType(method), PAGE)) {
                return sortingProblem(method) == null ? Classified.of(Kind.FIND_ALL, RestOperation.FIND)
                        : Classified.unsupported(sortingProblem(method));
            }
        }
        if (standardOwner || !dataAnnotated) {
            Classified standard = standard(name, arity, standardOwner);
            if (standard != null) {
                return standard;
            }
            if (standardOwner) {
                return Classified.unsupported("bulk and other repository operations are not supported "
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
            if (hasEntityParameter(method)) {
                return Classified.of(Kind.LIFECYCLE_DELETE, RestOperation.DELETE);
            }
            return deletesByIdentifier(method) ? Classified.of(Kind.QUERY_DELETE, RestOperation.DELETE)
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
            return deletesByIdentifier(method) ? Classified.of(Kind.QUERY_DELETE, RestOperation.DELETE)
                    : Classified.explicit(Kind.QUERY_DELETE, RestOperation.DELETE);
        }
        return Classified.unsupported("it is neither annotated with a Jakarta Data operation annotation nor "
                + "named as a findBy, countBy, existsBy or deleteBy query");
    }

    /**
     * Whether the only parameter of the method is proven to be the identifier of the entity, through its
     * Jakarta Data metadata: {@code @By}, the parameter name of a {@code @Find} or {@code @Delete}, or the
     * attribute named by a derived {@code findBy} or {@code deleteBy} method.
     */
    private boolean identifiesEntity(ExecutableElement method, String derivedPrefix, String annotation) {
        String identifier = identifier();
        if (identifier == null || method.getParameters().size() != 1) {
            return false;
        }
        VariableElement parameter = method.getParameters().get(0);
        TypeMirror type = parameterTypes(method).get(0);
        if (!ScalarTypes.isHttpBindable(type) || !sameBoxed(type, identifierType())) {
            return false;
        }
        var by = Annotations.rawStringValue(parameter, BY);
        if (by.isPresent()) {
            return by.get().equalsIgnoreCase("id(this)") || by.get().equals(identifier);
        }
        String name = method.getSimpleName().toString();
        if (Annotations.has(method, annotation)) {
            return parameter.getSimpleName().contentEquals(identifier);
        }
        if (hasDataAnnotation(method) || !name.startsWith(derivedPrefix)) {
            return false;
        }
        String attribute = name.substring(derivedPrefix.length());
        return attribute.equals("Id") || attribute.equals(Character.toUpperCase(identifier.charAt(0))
                + identifier.substring(1));
    }

    private boolean deletesByIdentifier(ExecutableElement method) {
        return identifiesEntity(method, "deleteBy", DELETE);
    }

    private boolean findsByIdentifier(ExecutableElement method) {
        if (!identifiesEntity(method, "findBy", FIND)) {
            return false;
        }
        TypeMirror result = returnType(method);
        if (types.isSameType(result, repository.entity())) {
            return true;
        }
        return isType(result, "java.util.Optional") && ((DeclaredType) result).getTypeArguments().size() == 1
                && types.isSameType(((DeclaredType) result).getTypeArguments().get(0), repository.entity());
    }

    private TypeMirror identifierType() {
        return new PropertyScanner(elements, types).scan((TypeElement) types.asElement(repository.entity())).stream()
                .filter(Property::identifier).map(Property::type).findFirst().orElse(repository.id());
    }

    private boolean sameBoxed(TypeMirror first, TypeMirror second) {
        return second != null && types.isSameType(box(first), box(second));
    }

    private TypeMirror box(TypeMirror type) {
        return type.getKind().isPrimitive() ? types.boxedClass((PrimitiveType) type).asType() : type;
    }

    private Classified standard(String name, int arity, boolean standardOwner) {
        return switch (name + "/" + arity) {
            case "findById/1" -> standardOwner || repository.hasFindById()
                    ? Classified.of(Kind.FIND_BY_ID, RestOperation.FIND) : null;
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

    private void generate(ExecutableElement method, Classified classified, UriTemplate declared,
                          boolean standardOwner) {
        String name = method.getSimpleName().toString();
        String override = declared == null ? null : declared.path();
        if (declared != null && !declared.variables().isEmpty() && bodyOperation(classified.kind())) {
            diagnostics.error(method, "The @Path of " + name + " must not declare path variables, because the "
                    + "operation receives the entity in the request body.");
            return;
        }
        String identifierVariable = declared != null && declared.variables().size() == 1
                ? declared.variables().get(0) : identifierName();
        Spec spec = switch (classified.kind()) {
            case FIND_BY_ID -> findById(identifierVariable);
            case FIND_ALL -> findAll(method);
            case INSERT -> insert("insert", "POST", "", method, standardOwner);
            case UPDATE -> update(identifierVariable);
            case SAVE -> save("save", "/save", method, standardOwner);
            case DELETE_BY_ID -> deleteById(identifierVariable);
            case DELETE -> deleteEntity("delete", "/delete", method);
            case LIFECYCLE_INSERT -> insert(name, "POST", pathOr(override, ""), method, false);
            case LIFECYCLE_SAVE -> save(name, pathOr(override, "/" + name), method, false);
            case LIFECYCLE_UPDATE -> lifecycleUpdate(method, pathOr(override, ""));
            case LIFECYCLE_DELETE -> deleteEntity(name, pathOr(override, "/" + name), method);
            case READ, COUNT, EXISTS -> read(method, classified,
                    pathOr(override, findsByIdentifier(method) ? "/{" + method.getParameters().get(0).getSimpleName()
                            + "}" : "/" + name));
            case QUERY_DELETE -> mutation(method, "DELETE", pathOr(override, deletesByIdentifier(method)
                    ? "/{" + method.getParameters().get(0).getSimpleName() + "}" : "/" + name));
            case QUERY_UPDATE -> mutation(method, "POST", pathOr(override, "/" + name));
            case SEARCH -> search(method, pathOr(override, "/" + name));
        };
        if (spec == null || !overridePath(method, classified, spec, override)) {
            return;
        }
        register(method, standardOwner, spec);
    }

    private static boolean bodyOperation(Kind kind) {
        return switch (kind) {
            case LIFECYCLE_INSERT, LIFECYCLE_SAVE, LIFECYCLE_UPDATE, LIFECYCLE_DELETE -> true;
            default -> false;
        };
    }

    private boolean overridePath(ExecutableElement method, Classified classified, Spec spec, String override) {
        boolean standard = switch (classified.kind()) {
            case FIND_BY_ID, FIND_ALL, INSERT, UPDATE, SAVE, DELETE_BY_ID, DELETE -> true;
            default -> false;
        };
        if (!standard || override == null) {
            return true;
        }
        List<String> expected = UriTemplate.of(spec.path).variables();
        List<String> actual = UriTemplate.of(override).variables();
        if (expected.size() != actual.size()) {
            diagnostics.error(method, "The @Path(\"" + override + "\") of " + method.getSimpleName()
                    + (expected.isEmpty() ? " must not declare path variables"
                    : " must declare exactly " + expected.size() + " path variable") + ".");
            return false;
        }
        spec.path = UriTemplate.of(override).path();
        return true;
    }

    private static String pathOr(String override, String fallback) {
        return override != null ? override : fallback;
    }

    private String identifierName() {
        String identifier = identifier();
        return identifier == null ? "id" : identifier;
    }

    private String javaName(String name) {
        String safe = name.replaceAll("[^A-Za-z0-9_]", "_");
        return RESERVED.contains(safe) ? safe + "Value" : safe;
    }

    private void register(ExecutableElement method, boolean standardOwner, Spec spec) {
        String route = spec.http + " " + UriTemplate.of(spec.path).shape();
        String previous = routes.putIfAbsent(route, method.getSimpleName().toString());
        if (previous != null) {
            diagnostics.error(standardOwner ? repository.element() : method, "Both " + previous + " and "
                    + method.getSimpleName() + " would handle " + spec.http + " " + repository.path() + spec.path
                    + ". Routes that differ only by variable names or query parameters are the same route; "
                    + "use a distinct @Path or @RestExclude to resolve the conflict.");
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
        if (spec.createdType != null && openApi.available() && !declaresResponses(method)) {
            annotations.add(createdResponse(spec.createdType));
        }
        String javadoc = "Handles {@code " + spec.http + " " + repository.path() + spec.path
                + "} by delegating to {@link " + repositoryName() + "#" + method.getSimpleName() + "}.";
        if (!spec.docs.isEmpty()) {
            javadoc += "\n     *\n     * " + String.join("\n     * ", spec.docs)
                    + "\n     * @throws jakarta.ws.rs.BadRequestException if a pagination, limit or sort value is "
                    + "invalid";
        }
        methods.add(new MethodModel(javadoc, annotations, spec.returnType, spec.name, renderParameters(spec.parameters),
                spec.body));
    }

    private static boolean declaresResponses(ExecutableElement method) {
        return Annotations.has(method, "org.eclipse.microprofile.openapi.annotations.responses.APIResponse")
                || Annotations.has(method, "org.eclipse.microprofile.openapi.annotations.responses.APIResponses");
    }

    private String createdResponse(String type) {
        String base = "org.eclipse.microprofile.openapi.annotations.";
        return "@" + imports.name(base + "responses.APIResponse") + "(responseCode = \"201\", "
                + "description = \"Created\", content = @" + imports.name(base + "media.Content")
                + "(mediaType = \"application/json\", schema = @" + imports.name(base + "media.Schema")
                + "(implementation = " + type + ".class)))";
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
        private String path;
        private String returnType;
        private final String name;
        private final List<String> parameters = new ArrayList<>();
        private final List<String> body = new ArrayList<>();
        private final List<String> docs = new ArrayList<>();
        private String createdType;

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

    private Spec findById(String variable) {
        String id = javaName(variable);
        Spec spec = new Spec("GET", "/{" + variable + "}", representation(), "findById")
                .parameter("@" + imports.name("jakarta.ws.rs.PathParam") + "(\"" + variable + "\") " + idType() + " "
                        + id);
        spec.line("return repository.findById(" + id + ")");
        if (dto != null) {
            spec.line(INDENT + INDENT + ".map(entity -> mapper.toDTO(entity))");
        }
        return spec.line(INDENT + INDENT + ".orElseThrow(" + imports.name("jakarta.ws.rs.NotFoundException")
                + "::new);");
    }

    private Spec findAll(ExecutableElement method) {
        Spec spec = new Spec("GET", "", "void", "findAll");
        addQuery(spec, "size", SIZE_HELP);
        addQuery(spec, "offset", OFFSET_HELP);
        addQuery(spec, "includeTotal", TOTAL_HELP);
        addQuery(spec, "sort", SORT_HELP);
        spec.line(imports.name(PAGE_REQUEST) + " pageRequest = " + imports.name(PAGINATION)
                + ".offset(offset, size, includeTotal);");
        spec.line(imports.name(ORDER) + "<" + entityType() + "> order = " + imports.name(SORTING) + ".order(sort, "
                + sortableSet() + ");");
        return readBody(spec, returnType(method), "repository.findAll(pageRequest, order)", method) ? spec : null;
    }

    private Spec deleteById(String variable) {
        String id = javaName(variable);
        return new Spec("DELETE", "/{" + variable + "}", "void", "deleteById")
                .parameter("@" + imports.name("jakarta.ws.rs.PathParam") + "(\"" + variable + "\") " + idType() + " "
                        + id)
                .line("repository.deleteById(" + id + ");");
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
        spec.createdType = representation();
        boolean returns = method.getReturnType().getKind() != TypeKind.VOID;
        spec.line(returns ? entityType + " created = " + call + ";" : call + ";");
        String created = returns ? "created" : "entity";
        return spec.line("return " + imports.name("jakarta.ws.rs.core.Response") + ".status("
                + imports.name("jakarta.ws.rs.core.Response") + ".Status.CREATED)")
                .line(INDENT + INDENT + ".entity(" + (dto == null ? created : "mapper.toDTO(" + created + ")") + ")")
                .line(INDENT + INDENT + ".build();");
    }

    private Spec update(String variable) {
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
        String id = javaName(variable);
        Spec spec = new Spec("PUT", "/{" + variable + "}", dtoType(), "update")
                .parameter("@" + imports.name("jakarta.ws.rs.PathParam") + "(\"" + variable + "\") " + idType() + " "
                        + id)
                .parameter(updateType + " request");
        checkBody(spec, "request");
        return spec
                .line(entityType() + " entity = repository.findById(" + id + ")")
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
        boolean explicit = Annotations.has(method, PATH);
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
    private boolean searchSorted;

    private Spec search(ExecutableElement method, String path) {
        if (!UriTemplate.of(path).variables().isEmpty()) {
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
        boolean sortedSearch = sorted(method);
        if (searchRequestResolved && searchSorted != sortedSearch) {
            diagnostics.error(method, "All dynamic searches of " + repository.element().getSimpleName()
                    + " must either accept a Sort or Order or not, because they share one " + searchRequestName()
                    + ".");
            return null;
        }
        searchSorted = sortedSearch;
        Spec spec = new Spec("QUERY", path, "void", method.getSimpleName().toString());
        String call = "repository." + method.getSimpleName() + "("
                + method.getParameters().stream().map(parameter -> isType(parameter.asType(), PAGE_REQUEST)
                        || isType(parameter.asType(), SORT) || isType(parameter.asType(), ORDER)
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
        for (VariableElement parameter : method.getParameters()) {
            boolean order = isType(parameter.asType(), ORDER);
            if (order || isType(parameter.asType(), SORT)) {
                String unique = order && paging.mode() == PageMode.CURSOR
                        ? ", " + AnnotationSource.quote(identifier()) : "";
                spec.line(imports.name(order ? ORDER : SORT) + "<" + entityType() + "> " + variableName(parameter)
                        + " = " + imports.name(SORTING) + "." + (order ? "order" : "sort") + "(request.sort(), "
                        + sortableSet() + (order ? (unique.isEmpty() ? ", null" : unique) : "") + ");");
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
            searchRequest = searchPlanner.plan(repository, searchPaginated, searchSorted);
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
        if (convertible && searchSorted && ElementFilter.methodsIn(existing.getEnclosedElements()).stream()
                .noneMatch(candidate -> candidate.getSimpleName().contentEquals("sort"))) {
            diagnostics.error(method, "The developer-defined " + existing.getQualifiedName() + " must also "
                    + "declare sort() returning a List of SortCriterion.");
            convertible = false;
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
        List<String> templated = UriTemplate.of(path).variables();
        List<? extends TypeMirror> parameterTypes = parameterTypes(method);
        Spec spec = new Spec(http, path, "void", method.getSimpleName().toString());
        Map<String, String> checks = new java.util.LinkedHashMap<>();
        Set<String> seen = new java.util.HashSet<>();
        Set<String> boundVariables = new java.util.HashSet<>();
        String pageVariable = null;
        List<String> conversions = new ArrayList<>();
        Set<String> reserved = new java.util.HashSet<>();
        for (int index = 0; index < parameterTypes.size(); index++) {
            VariableElement parameter = method.getParameters().get(index);
            String original = parameter.getSimpleName().toString();
            TypeMirror type = parameterTypes.get(index);
            if (isType(type, PAGE_REQUEST)) {
                pageVariable = variableName(parameter);
                continue;
            }
            if (isType(type, SORT) || isType(type, ORDER) || isType(type, LIMIT)) {
                String variable = variableName(parameter);
                if (isType(type, LIMIT)) {
                    reserved.addAll(List.of("limit", "startAt"));
                    addQuery(spec, "limit", LIMIT_HELP);
                    addQuery(spec, "startAt", START_HELP);
                    conversions.add(imports.name(LIMIT) + " " + variable + " = " + imports.name(LIMITS)
                            + ".of(limit, startAt);");
                } else {
                    reserved.add("sort");
                    addQuery(spec, "sort", SORT_HELP);
                    boolean order = isType(type, ORDER);
                    String unique = order && paging.mode() == PageMode.CURSOR
                            ? ", " + AnnotationSource.quote(identifier()) : "";
                    conversions.add(imports.name(order ? ORDER : SORT) + "<" + entityType() + "> " + variable
                            + " = " + imports.name(SORTING) + "." + (order ? "order" : "sort") + "(sort, "
                            + sortableSet() + unique + ");");
                }
                continue;
            }
            String problem = parameterProblem(type);
            if (problem != null) {
                return unsupportedSignature(method, "parameter '" + original + "' " + problem);
            }
            Binding binding = binding(method, parameter, templated);
            if (binding == null) {
                return null;
            }
            if (binding.path() && type.getKind() == TypeKind.DECLARED
                    && !((DeclaredType) type).getTypeArguments().isEmpty()) {
                diagnostics.error(parameter, "The path variable {" + binding.name() + "} of "
                        + method.getSimpleName() + " cannot be bound to the collection parameter '" + original
                        + "'.");
                return null;
            }
            if (!(binding.path() ? boundVariables : seen).add(binding.name())) {
                diagnostics.error(parameter, "The " + (binding.path() ? "path variable {" : "query parameter '")
                        + binding.name() + (binding.path() ? "}" : "'") + " is bound by more than one parameter of "
                        + method.getSimpleName() + ".");
                return null;
            }
            if (type.getKind().isPrimitive()) {
                type = types.boxedClass((PrimitiveType) type).asType();
            }
            StringBuilder text = new StringBuilder();
            for (var mirror : openApi.forQueryParameter(parameter)) {
                if (!openApiLocationMatches(mirror, binding.path())) {
                    diagnostics.error(parameter, "The @Parameter location of '" + original + "' contradicts its "
                            + (binding.path() ? "path" : "query") + " binding.");
                    return null;
                }
                text.append(annotationSource.write(mirror)).append(' ');
            }
            String parser = ScalarTypes.parser(type);
            String received = parser == null ? variableName(parameter) : variableName(parameter) + "Text";
            text.append('@').append(imports.name(binding.path() ? PATH_PARAM : QUERY_PARAM))
                    .append("(\"").append(binding.name()).append("\") ")
                    .append(parser == null ? imports.render(type) : "String").append(' ').append(received);
            spec.parameter(text.toString());
            if (parser != null) {
                conversions.add(imports.render(type) + " " + variableName(parameter) + " = "
                        + imports.name("org.eclipse.jnosql.rest.pagination.QueryValues") + ".parse(\""
                        + binding.name() + "\", " + received + ", " + imports.render(type) + "::" + parser + ");");
            }
            if (!binding.path()) {
                checks.put(received, binding.name());
            }
        }
        for (String name : templated) {
            if (!boundVariables.contains(name)) {
                diagnostics.error(method, "The path of " + method.getSimpleName() + " refers to {" + name
                        + "}, but no parameter is bound to it. Name a parameter '" + name + "' or annotate one with "
                        + "@PathParam(\"" + name + "\").");
                return null;
            }
        }
        for (Map.Entry<String, String> check : checks.entrySet()) {
            spec.line("if (" + check.getKey() + " == null) {");
            spec.line(INDENT + "throw new " + imports.name("jakarta.ws.rs.BadRequestException")
                    + "(\"Query parameter '" + check.getValue() + "' is required\");");
            spec.line("}");
        }
        for (String name : reserved) {
            if (seen.contains(name)) {
                diagnostics.error(method, "The query parameter '" + name + "' of " + method.getSimpleName()
                        + " clashes with the parameter the generated endpoint adds for Limit, Sort or Order.");
                return null;
            }
        }
        conversions.forEach(spec::line);
        if (pageVariable != null && !addPagination(spec, method, paging, pageVariable, seen)) {
            return null;
        }
        return spec;
    }

    private record Binding(boolean path, String name) {
    }

    /**
     * Decides how an ordinary parameter is bound to HTTP: an explicit {@code @PathParam} or {@code @QueryParam}
     * wins, then a path variable with the name of the Java parameter, and otherwise a query parameter named after
     * the Java parameter. Jakarta Data's {@code @By} does not take part.
     */
    private Binding binding(ExecutableElement method, VariableElement parameter, List<String> templated) {
        String original = parameter.getSimpleName().toString();
        for (var mirror : parameter.getAnnotationMirrors()) {
            if (UNSUPPORTED_BINDINGS.contains(Annotations.qualifiedName(mirror))) {
                diagnostics.error(parameter, "The parameter '" + original + "' of " + method.getSimpleName()
                        + " uses @" + mirror.getAnnotationType().asElement().getSimpleName() + ", which cannot "
                        + "bind a repository argument; use @PathParam or @QueryParam.");
                return null;
            }
        }
        boolean explicitPath = Annotations.has(parameter, PATH_PARAM);
        boolean explicitQuery = Annotations.has(parameter, QUERY_PARAM);
        if (explicitPath && explicitQuery) {
            diagnostics.error(parameter, "The parameter '" + original + "' of " + method.getSimpleName()
                    + " declares both @PathParam and @QueryParam.");
            return null;
        }
        if (!explicitPath && !explicitQuery) {
            return new Binding(templated.contains(original), original);
        }
        String name = Annotations.rawStringValue(parameter, explicitPath ? PATH_PARAM : QUERY_PARAM).orElse("").trim();
        if (name.isEmpty()) {
            diagnostics.error(parameter, "The @" + (explicitPath ? "PathParam" : "QueryParam") + " of '" + original
                    + "' in " + method.getSimpleName() + " needs a name.");
            return null;
        }
        if (explicitPath && !templated.contains(name)) {
            diagnostics.error(parameter, "@PathParam(\"" + name + "\") on '" + original + "' in "
                    + method.getSimpleName() + " does not match any variable of the path.");
            return null;
        }
        if (explicitQuery && templated.contains(name)) {
            diagnostics.error(parameter, "@QueryParam(\"" + name + "\") on '" + original + "' in "
                    + method.getSimpleName() + " conflicts with the path variable {" + name + "}; use @PathParam.");
            return null;
        }
        return new Binding(explicitPath, name);
    }

    private static boolean openApiLocationMatches(javax.lang.model.element.AnnotationMirror mirror, boolean path) {
        for (var entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals("in")) {
                String location = entry.getValue().getValue().toString().toUpperCase();
                return location.endsWith("DEFAULT") || location.endsWith(path ? "PATH" : "QUERY");
            }
        }
        return true;
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
        addQuery(spec, "size", SIZE_HELP);
        if (paging.mode() == PageMode.OFFSET) {
            addQuery(spec, "offset", OFFSET_HELP);
            addQuery(spec, "includeTotal", TOTAL_HELP);
            spec.line(pageRequest + " " + variable + " = " + pagination + ".offset(offset, size, includeTotal);");
        } else {
            addQuery(spec, "cursor", "Opaque continuation token from nextCursor or previousCursor; "
                    + "omit it for the first page");
            List<String> values = method.getParameters().stream()
                    .filter(parameter -> !isType(parameter.asType(), PAGE_REQUEST)).map(this::variableName).toList();
            spec.line("String cursorContext = " + pagination + ".context(\"" + method.getSimpleName() + "\""
                    + values.stream().map(value -> ", " + value).collect(java.util.stream.Collectors.joining())
                    + ");");
            spec.line(pageRequest + " " + variable + " = " + pagination + ".cursor(cursor, size, cursorContext);");
        }
        return true;
    }

    private void addQuery(Spec spec, String name, String description) {
        spec.parameter(pageParameter(name, description));
        spec.docs.add("@param " + name + " " + Character.toLowerCase(description.charAt(0))
                + description.substring(1));
    }

    private String pageParameter(String name, String description) {
        String documentation = openApi.available()
                ? "@" + imports.name("org.eclipse.microprofile.openapi.annotations.parameters.Parameter")
                + "(description = \"" + description + "\"" + schema(name) + ") " : "";
        return documentation + "@" + imports.name("jakarta.ws.rs.QueryParam") + "(\"" + name + "\") String " + name;
    }

    /**
     * The parameters stay {@code String} in Java so that the runtime validation and defaults are unchanged; the
     * OpenAPI schema describes the real contract.
     */
    private String schema(String name) {
        String type = switch (name) {
            case "offset" -> "INTEGER, minimum = \"0\", defaultValue = \"0\"";
            case "size", "limit" -> "INTEGER, minimum = \"1\", maximum = \"100\", defaultValue = \"20\"";
            case "startAt" -> "INTEGER, minimum = \"1\", defaultValue = \"1\"";
            case "includeTotal" -> "BOOLEAN, defaultValue = \"false\"";
            default -> null;
        };
        if (type == null) {
            return "";
        }
        return ", schema = @" + imports.name("org.eclipse.microprofile.openapi.annotations.media.Schema")
                + "(type = " + imports.name("org.eclipse.microprofile.openapi.annotations.enums.SchemaType") + "."
                + type + ")";
    }

    private String parameterProblem(TypeMirror type) {
        if (ScalarTypes.isHttpBindable(type)) {
            return null;
        }
        if (ScalarTypes.isScalar(type)) {
            return "has type " + type + ", which cannot be converted from an HTTP parameter";
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
                    && ScalarTypes.isHttpBindable(declared.getTypeArguments().get(0))
                    && ScalarTypes.parser(declared.getTypeArguments().get(0)) == null) {
                return null;
            }
        }
        return "has type " + type + ", which cannot be bound to a query parameter";
    }
}
