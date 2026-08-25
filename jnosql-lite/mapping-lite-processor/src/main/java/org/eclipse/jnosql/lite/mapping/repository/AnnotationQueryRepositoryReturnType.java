/*
 *  Copyright (c) 2021 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.repository;


import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import org.eclipse.jnosql.lite.mapping.ValidationException;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.eclipse.jnosql.lite.mapping.ProcessorUtil.extractFromType;

enum AnnotationQueryRepositoryReturnType implements Function<MethodMetadata, List<String>> {
    STREAM {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            String line = "Stream<" + getEntity(metadata) + "> resultJNoSQL = prepareJNoSQL.result()";
            return singletonList(line);
        }
    }, LIST {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = prepareJNoSQL.result()");
            lines.add("java.util.List<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.toList()");
            return lines;
        }
    }, SET {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = prepareJNoSQL.result()");
            lines.add("java.util.Set<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.collect(java.util.stream.Collectors.toSet())");
            return lines;
        }
    }, QUEUE {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = prepareJNoSQL.result()");
            lines.add("java.util.Queue<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.collect(java.util.stream" +
                    ".Collectors.toCollection(java.util.LinkedList::new)");
            return lines;
        }
    }, SORTED_SET {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = prepareJNoSQL.result()");
            lines.add("java.util.Queue<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.collect(java.util.stream" +
                    ".Collectors.toCollection(java.util.TreeSet::new)");
            return lines;
        }
    }, OPTIONAL {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("java.util.Optional<" + getEntity(metadata) + "> resultJNoSQL = prepareJNoSQL.singleResult()");
            return lines;
        }
    },
    ENTITY_TYPE {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("java.util.Optional<" + getEntity(metadata) + "> entityResult = prepare.singleResult()");
            lines.add(getEntity(metadata) + " resultJNoSQL = entityResult.orElse(null)");
            return lines;
        }
    }, CURSOR_PAGINATION{
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("var preparedStatementJNoSQL = (org.eclipse.jnosql.mapping.semistructured.PreparedStatement) prepareJNoSQL");
            lines.add("var selectJNoSQL = preparedStatementJNoSQL.selectQuery().orElseThrow(() ->" +
                    "\n         new jakarta.data.exceptions.MappingException(\"The select query is required\"))");
            Parameter pageRequest = metadata.findPageRequest().orElseThrow(() -> new IllegalStateException("The page request is required"));
            lines.add(CursoredPage.class.getName() + "<" + getEntity(metadata) + "> resultJNoSQL = template.selectCursor(selectJNoSQL, " + pageRequest.name() + ")");
            return lines;
        }
    }, PAGINATION{

        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();

            var pageRequest = metadata.findPageRequest()
                    .orElseThrow(() -> new ValidationException("The method " + metadata.getMethodName() + " from " +
                            metadata.getParametersSignature() + " does not have a Pageable parameter in a pagination method"));

            lines.add("java.util.stream.Stream<"+  getEntity(metadata) + "> entitiesJNoSQL = prepareJNoSQL.result()");
            lines.add(Page.class.getName()+ "<"+  getEntity(metadata) + ">  resultJNoSQL = \n      "
            +  "org.eclipse.jnosql.mapping.core.NoSQLPage.of(entitiesJNoSQL.toList(), " + pageRequest.name() + ")");
            return lines;
        }
    };

    private static String getEntity(MethodMetadata metadata) {
        return extractFromType(metadata.getReturnType());
    }

    static AnnotationQueryRepositoryReturnType of(MethodMetadata metadata) {
        TypeElement typeElement = metadata.getReturnElement();
        String returnType = typeElement.getQualifiedName().toString();

        if (returnType.equals(getEntity(metadata))) {
            return ENTITY_TYPE;
        }
        return switch (returnType) {
            case "java.util.stream.Stream" -> STREAM;
            case "java.util.List", "java.util.Collection", "java.lang.Iterable" -> LIST;
            case "java.util.Set" -> SET;
            case "java.util.Queue", "java.util.Deque" -> QUEUE;
            case "java.util.SortedSet", "java.util.TreeSet" -> SORTED_SET;
            case "java.util.Optional" -> OPTIONAL;
            case "jakarta.data.page.Page", "jakarta.data.page.Slice" -> PAGINATION;
            case "jakarta.data.page.CursoredPage" -> CURSOR_PAGINATION;
            default -> throw new UnsupportedOperationException("This return is not supported: " + returnType);
        };

    }
}