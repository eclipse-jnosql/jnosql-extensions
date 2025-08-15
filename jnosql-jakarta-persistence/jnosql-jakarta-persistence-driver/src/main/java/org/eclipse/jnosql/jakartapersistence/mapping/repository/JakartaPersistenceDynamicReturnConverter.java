/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistencePreparedStatement;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReflectionUtils;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;

/**
 * Copied from DynamicReturnConverter in JNoSQL and modified for Jakarta Persistence
 */
public enum JakartaPersistenceDynamicReturnConverter {

    INSTANCE;

    private final RepositoryReturn defaultReturn = new JakartaPersistenceDefaultRepositoryReturn();

    /**
     * Converts the entity from the Method return type.
     *
     * @param dynamic the information about the method and return source
     * @return the conversion result
     * @throws NullPointerException when the dynamic is null
     */
    public Object convert(DynamicReturn<?> dynamic) {

        Method method = dynamic.getMethod();
        Class<?> typeClass = dynamic.typeClass();
        Class<?> returnType = method.getReturnType();

        RepositoryReturn repositoryReturn = ServiceLoader.load(RepositoryReturn.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(RepositoryReturn.class::isInstance)
                .map(RepositoryReturn.class::cast)
                .filter(r -> r.isCompatible(typeClass, returnType))
                .findFirst().orElse(defaultReturn);

        if (dynamic.hasPagination()) {
            return repositoryReturn.convertPageRequest(dynamic);
        } else {
            return repositoryReturn.convert(dynamic);
        }
    }

    /**
     * Reads and execute JNoSQL query from the Method that has the {@link jakarta.data.repository.Query} annotation
     *
     * @return the result from the query annotation
     */
    @SuppressWarnings({"unchecked"})
    public Object convert(JakartaPersistenceDynamicQueryMethodReturn<?> dynamicQueryMethod) {
        Method method = dynamicQueryMethod.method();
        Object[] args = dynamicQueryMethod.args();
        Function<String, PersistencePreparedStatement> prepareConverter = dynamicQueryMethod.prepareConverter();
        Class<?> typeClass = dynamicQueryMethod.typeClass();

        String queryString = RepositoryReflectionUtils.INSTANCE.getQuery(method);

        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, args);
        boolean namedParameters = queryContainsNamedParameters(queryString);
        PersistencePreparedStatement prepare = prepareConverter.apply(queryString);
                    params.entrySet().stream()
                        .filter(namedParameters ?
                                        (parameter -> !isOrdinalParameter(parameter))
                                        : parameter -> isOrdinalParameter(parameter))
                        .forEach(param -> prepare.bind(param.getKey(), param.getValue()));

        if (prepare.isCount()) {
            return prepare.count();
        }

        var pageRequest = dynamicQueryMethod.pageRequest();

        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodSource(method)
                .result(() -> prepare.result())
                .singleResult(() -> prepare.singleResult())
                .pagination(pageRequest)
                .streamPagination(p -> prepare.result())
                .singleResultPagination(p -> prepare.singleResult())
                .page(p -> {
                    return prepare.selectOffset(pageRequest);
                }).build();

        return convert(dynamicReturn);
    }

    private static boolean queryContainsNamedParameters(String queryString) {
        final String ordinalParameterPattern = "\\?\\d+";
        final String identifierFirstCharacterPattern = "(\\p{Alpha}|_|$)";
        final String identifierAfterFirstCharacterpattern = "\\p{Alnum}|_|$";
        String namedParameterPattern = ":" + identifierFirstCharacterPattern
                + "(" + identifierAfterFirstCharacterpattern + ")*";
        Pattern p = Pattern.compile("(" + ordinalParameterPattern + ")|(" + namedParameterPattern + ")");
        Matcher m = p.matcher(queryString);
        return m.find() && m.group().startsWith(":");
    }

    private static boolean isOrdinalParameter(Map.Entry<String, Object> parameter) {
        return parameter.getKey().startsWith("?");
    }

}
