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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.eclipse.jnosql.lite.mapping.ProcessorUtil.extractFromType;

enum MethodQueryRepositoryReturnType implements Function<MethodMetadata, List<String>> {
    STREAM {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            String line = "Stream<" + getEntity(metadata) + "> resultJNoSQL = this.template.select(queryJNoSQL)";
            return singletonList(line);
        }
    }, LIST {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            lines.add("java.util.List<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.toList()");
            return lines;
        }
    }, SET {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            lines.add("java.util.Set<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.collect(java.util.stream.Collectors.toSet())");
            return lines;
        }
    }, QUEUE {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            lines.add("java.util.Queue<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.collect(java.util.stream" +
                    ".Collectors.toCollection(java.util.LinkedList::new)");
            return lines;
        }
    }, SORTED_SET {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            lines.add("java.util.Queue<" + getEntity(metadata) + "> resultJNoSQL = entitiesJNoSQL.collect(java.util.stream" +
                    ".Collectors.toCollection(java.util.TreeSet::new)");
            return lines;
        }
    }, OPTIONAL {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("java.util.Optional<" + getEntity(metadata) + "> resultJNoSQL = this.template.singleResult(queryJNoSQL)");
            return lines;
        }
    },
    ENTITY_TYPE {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("java.util.Optional<" + getEntity(metadata) + "> entityResultJNoSQL = this.template.singleResult(queryJNoSQL)");
            lines.add(getEntity(metadata) + " resultJNoSQL = entityResultJNoSQL.orElse(null)");
            return lines;
        }
    }, PAGINATION {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            lines.add("Stream<" + getEntity(metadata) + "> entitiesJNoSQL = this.template.select(queryJNoSQL)");
            var pageRequest = metadata.findPageRequest()
                    .orElseThrow(() -> new ValidationException("The method " + metadata.getMethodName() + " from " +
                            metadata.getParametersSignature() + " does not have a Pageable parameter in a pagination method"));

            lines.add(Page.class.getName() + "<" + getEntity(metadata) + "> resultJNoSQL = \n      " +
                    "org.eclipse.jnosql.mapping.core.NoSQLPage.of(entitiesJNoSQL.toList(), " + pageRequest.name() + ")");
            return lines;
        }
    }, CURSOR_PAGINATION {
        @Override
        public List<String> apply(MethodMetadata metadata) {
            List<String> lines = new ArrayList<>();
            Parameter pageRequest = metadata.findPageRequest()
                    .orElseThrow(() -> new ValidationException("The method " + metadata.getMethodName() + " from " +
                            metadata.getParametersSignature() + " does not have a Pageable parameter in a pagination cursor method"));

            lines.add(CursoredPage.class.getName() + "<" + getEntity(metadata) + "> resultJNoSQL = this.template.selectCursor(queryJNoSQL, "
            + pageRequest.name() + ")");
            return lines;
        }
    };

    static String getEntity(MethodMetadata metadata) {
        return extractFromType(metadata.getReturnType());
    }

    static MethodQueryRepositoryReturnType of(MethodMetadata metadata) {
        String returnType = metadata.getReturnElement().getQualifiedName().toString();
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