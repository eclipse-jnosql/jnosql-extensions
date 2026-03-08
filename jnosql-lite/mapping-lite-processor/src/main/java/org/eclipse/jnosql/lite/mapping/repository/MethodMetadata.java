/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.mapping.DatabaseType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

class MethodMetadata {

    private static final Predicate<Parameter> IS_SPECIAL_PARAM = p ->
            p.type() != null &&
                    (p.type().getQualifiedName().toString().equals(Limit.class.getName()) ||
                            p.type().getQualifiedName().toString().equals(PageRequest.class.getName()) ||
                            p.type().getQualifiedName().toString().equals(Sort.class.getName()) ||
                            p.type().getQualifiedName().toString().equals(Order.class.getName()));
    public static final int DEFAULT_NEWLINE_SPACING = 30;
    private final String methodName;

    private final TypeElement returnElement;

    private final String returnType;

    private final List<Parameter> parameters;

    private final Insert insert;
    private final Update update;
    private final Delete delete;
    private final Save save;
    private final Query query;
    private final Find find;
    private final OrderBy[] orders;
    private final String constantName;

    private final DatabaseType type;

    private MethodGenerator generator;

    private final String entityType;

    private MethodMetadata(String methodName, TypeElement returnElement, String returnType,
                           List<Parameter> parameters, DatabaseType type, String entityType,
                           Query query, Insert insert, Update update, Delete delete, Save save, Find find, OrderBy[] orders,
                           String constantName) {

        this.methodName = methodName;
        this.returnElement = returnElement;
        this.returnType = returnType;
        this.parameters = parameters;
        this.query = query;
        this.type = type;
        this.entityType = entityType;
        this.insert = insert;
        this.update = update;
        this.delete = delete;
        this.save = save;
        this.find = find;
        this.orders = orders;
        this.constantName = constantName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getParametersSignature() {
        return parameters.stream().map(Parameter::parameterName)
                .collect(joining(", \n" + " ".repeat(DEFAULT_NEWLINE_SPACING)));
    }

    void update(Function<MethodMetadata, MethodGenerator> methodGeneratorFactory) {
        this.generator = methodGeneratorFactory.apply(this);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Parameter> getQueryParams() {
        return parameters.stream().filter(IS_SPECIAL_PARAM.negate()).toList();
    }

    public boolean hasSpecialParameter() {
        return parameters.stream().anyMatch(IS_SPECIAL_PARAM);
    }

    public String getSpecialParameter() {
        return parameters.stream().filter(IS_SPECIAL_PARAM)
                .map(Parameter::name).collect(joining(", "));
    }


    public List<String> getSourceCode() {
        return this.generator.getLines();
    }

    public boolean hasReturn() {
        return this.generator.hasReturn();
    }

    public String getReturnValue() {
        return "resultJNoSQL";
    }

    public DatabaseType getType() {
        return type;
    }

    public TypeElement getReturnElement() {
        return returnElement;
    }

    public Query getQuery() {
        return query;
    }

    public boolean hasQuery() {
        return query != null;
    }

    public String getEntityType() {
        return entityType;
    }

    public Optional<Parameter> findPageRequest() {
        for (Parameter parameter : this.parameters) {
            TypeElement element = parameter.type();
            if (element != null && PageRequest.class.getName().equals(element.getQualifiedName().toString())) {
                return Optional.of(parameter);
            }
        }
        return Optional.empty();
    }

    public boolean isInsert() {
        return Objects.nonNull(insert);
    }

    public boolean isDelete() {
        return Objects.nonNull(delete);
    }

    public boolean isUpdate() {
        return Objects.nonNull(update);
    }

    public boolean isSave() {
        return Objects.nonNull(save);
    }

    public boolean isFind() {
        return Objects.nonNull(find);
    }

    public String getConstantName() {
        return constantName;
    }

    public String getParametersAsObjectArray() {
        if (parameters.isEmpty()) {
            return "new Object[]{}";
        }

        String joined = parameters.stream()
                .map(Parameter::name)
                .collect(Collectors.joining(", "));
        return "new Object[]{" + joined + "}";
    }

    public OrderBy[] orders() {
        return orders;
    }

    public static MethodMetadata of(Element element, String entityType, DatabaseType type, ProcessingEnvironment processingEnv) {
        ElementKind kind = element.getKind();
        if (ElementKind.METHOD.equals(kind) && !isDefaultMethod((ExecutableElement) element)) {
            ExecutableElement method = (ExecutableElement) element;
            String methodName = method.getSimpleName().toString();
            TypeElement returnElement = (TypeElement) processingEnv.getTypeUtils().asElement(method.getReturnType());
            String returnType = method.getReturnType().toString();
            List<Parameter> parameters = method.getParameters()
                    .stream()
                    .map(e -> Parameter.of(e, processingEnv))
                    .collect(Collectors.toList());

            Query query = method.getAnnotation(Query.class);
            Insert insert = method.getAnnotation(Insert.class);
            Update update = method.getAnnotation(Update.class);
            Delete delete = method.getAnnotation(Delete.class);
            Save save = method.getAnnotation(Save.class);
            Find find = method.getAnnotation(Find.class);
            OrderBy[] orders = method.getAnnotationsByType(OrderBy.class);
            String constantName = MethodSignatureKeyExtractor.buildConstantName(method);
            return new MethodMetadata(methodName, returnElement, returnType, parameters, type, entityType, query,
                    insert, update, delete, save, find, orders, constantName);
        }
        return null;
    }

    private static boolean isDefaultMethod(ExecutableElement methodElement) {
        return methodElement.getModifiers().contains(Modifier.DEFAULT);
    }


}
