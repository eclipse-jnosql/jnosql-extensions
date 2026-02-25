/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import jakarta.data.repository.By;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

enum SemiStructuredMethodBuilder implements Function<MethodMetadata, List<String>> {

    METHOD_QUERY {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            feedSelectQuery(metadata, lines);
            MethodQueryRepositoryReturnType returnType = MethodQueryRepositoryReturnType.of(metadata);
            lines.addAll(returnType.apply(metadata));
            return lines;
        }
    }, ANNOTATION_QUERY {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            Query query = metadata.getQuery();
            lines.add("var prepareJNoSQL = template.prepare(\"" + query.value() + "\", this.entityMetadata().name())");
            for (Parameter parameter : metadata.getParameters()) {
                if (parameter.hasParam()) {
                    Param param = parameter.param();
                    lines.add("prepareJNoSQL.bind(\"" + param.value() + "\", " + parameter.name() + ")");
                }
            }
            AnnotationQueryRepositoryReturnType returnType = AnnotationQueryRepositoryReturnType.of(metadata);
            lines.addAll(returnType.apply(metadata));
            return lines;
        }
    }, EXIST_BY {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            feedSelectQuery(metadata, lines);
            lines.add("Stream<" + metadata.getEntityType() + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            lines.add("boolean resultJNoSQL = entitiesJNoSQL.findAny().isPresent()");
            return lines;
        }
    },COUNT_BY {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            feedSelectQuery(metadata, lines);
            lines.add("Stream<" + metadata.getEntityType() + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            lines.add("long resultJNoSQL = entitiesJNoSQL.count()");
            return lines;
        }
    },DELETE_BY{
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("org.eclipse.jnosql.communication.query.method.DeleteMethodProvider deleteMethodFactoryJNoSQL = " + SPACE +
                    "org.eclipse.jnosql.communication.query.method.DeleteMethodProvider.INSTANCE");
            lines.add("org.eclipse.jnosql.communication.query.method.DeleteMethodProvider supplierJNoSQL = " + SPACE +
                    " org.eclipse.jnosql.communication.query.method.DeleteMethodProvider.INSTANCE");
            lines.add("org.eclipse.jnosql.communication.query.DeleteQuery deleteJNoSQL = supplierJNoSQL.apply(\"" +
                    metadata.getMethodName() + "\", metadata.name())");
            lines.add("org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser parserJNoSQL = " + SPACE +
                    "org.eclipse.jnosql.mapping.semistructured.query.RepositorySemiStructuredObserverParser.of(metadata)");
            lines.add("org.eclipse.jnosql.communication.semistructured.DeleteQueryParams queryParamsJNoSQL = " + SPACE +
                    "DELETE_PARSER.apply(deleteJNoSQL, parserJNoSQL)");
            lines.add("org.eclipse.jnosql.communication.Params paramsJNoSQL = queryParamsJNoSQL.params()");
            for (Parameter parameter : metadata.getParameters()) {
                lines.add("paramsJNoSQL.prefix(\"" + parameter.name() + "\", " + parameter.name() + ")");
            }
            lines.add("this.template.delete(queryParamsJNoSQL.query())");
            return lines;
        }
    },NOT_SUPPORTED {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            return List.of("throw new UnsupportedOperationException(\"There is no support for this method type yet.\")");
        }
    }, INSERT {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            return AnnotationOperationMethodBuilder.INSERT.apply(methodMetadata);
        }
    }, UPDATE {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            return AnnotationOperationMethodBuilder.UPDATE.apply(methodMetadata);
        }
    }, DELETE {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            return AnnotationOperationMethodBuilder.DELETE.apply(methodMetadata);
        }
    }, SAVE {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            return AnnotationOperationMethodBuilder.SAVE.apply(methodMetadata);
        }
    }, PARAMETER_BASED {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("java.util.Map<String, Object> parametersJNoSQL = new java.util.HashMap<>()");
            for (Parameter parameter : metadata.getParameters()) {
                By by = parameter.by();
                if(by != null) {
                    lines.add("parametersJNoSQL.put(\"" + by.value() + "\", " + parameter.name() + ")");
                }
            }
            lines.add("java.util.List<Sort<?>> sortsJNoSQL = new java.util.ArrayList<>()");
            for (OrderBy order : metadata.orders()) {
                if(order.descending()){
                    lines.add("sortsJNoSQL.add(jakarta.data.Sort.desc(\"" + order.value() + "\"))");
                } else {
                    lines.add("sortsJNoSQL.add(jakarta.data.Sort.asc(\"" + order.value() + "\"))");
                }
            }

            var pageRequest = metadata.findPageRequest().map(Parameter::name).orElse("null");
            lines.add("var queryJNoSQL = org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredParameterBasedQuery.INSTANCE."+ SPACE +
                    "toQueryNative(parametersJNoSQL, sortsJNoSQL," + pageRequest +", entityMetadata())");

            if (metadata.hasSpecialParameter()) {
                lines.add("queryJNoSQL = " + SPACE +
                        " org.eclipse.jnosql.mapping.semistructured.query.DynamicQuery.of(new Object[]{" +
                        metadata.getSpecialParameter() +
                        "},  " + SPACE + "queryJNoSQL).get()");
            }
            MethodQueryRepositoryReturnType returnType = MethodQueryRepositoryReturnType.of(metadata);
            lines.addAll(returnType.apply(metadata));
            return lines;
        }
    };

    private static final String SPACE = "\n          ";

    private static void feedSelectQuery(MethodMetadata metadata, List<String> lines) {
        lines.add("org.eclipse.jnosql.communication.query.method.SelectMethodProvider supplierJNoSQL = " + SPACE +
                "org.eclipse.jnosql.communication.query.method.SelectMethodProvider.INSTANCE");
        lines.add("org.eclipse.jnosql.communication.query.SelectQuery selectQueryJNoSQL = " + SPACE +
                "supplierJNoSQL.apply(\"" + metadata.getMethodName() + "\", metadata.name())");
        lines.add("org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser parserJNoSQL = " + SPACE +
                "org.eclipse.jnosql.mapping.semistructured.query.RepositorySemiStructuredObserverParser.of(metadata)");
        lines.add("org.eclipse.jnosql.communication.semistructured.QueryParams queryParamsJNoSQL = " + SPACE +
                "SELECT_PARSER.apply(selectQueryJNoSQL, parserJNoSQL)");
        if (metadata.hasSpecialParameter()) {
            lines.add("SelectQuery queryJNoSQL = " + SPACE +
                    " org.eclipse.jnosql.mapping.semistructured.query.DynamicQuery.of(new Object[]{" +
                    metadata.getSpecialParameter() +
                    "},  " + SPACE + "queryParamsJNoSQL.query()).get()");
        } else {
            lines.add("SelectQuery queryJNoSQL = queryParamsJNoSQL.query()");
        }
        lines.add("org.eclipse.jnosql.communication.Params paramsJNoSQL = queryParamsJNoSQL.params()");
        for (Parameter parameter : metadata.getQueryParams()) {
            lines.add("paramsJNoSQL.prefix(\"" + parameter.name() + "\", " + parameter.name() + ")");
        }
    }

    static SemiStructuredMethodBuilder of(MethodMetadata metadata) {
        MethodMetadataOperationType operationType = MethodMetadataOperationType.of(metadata);
        return Arrays.stream(SemiStructuredMethodBuilder.values()).filter(c -> c.name().equals(operationType.name()))
                .findAny().orElse(NOT_SUPPORTED);
    }
}
