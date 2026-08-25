/*
 *  Copyright (c) 2023 Otávio Santana and others
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

enum MethodMetadataOperationType {
    METHOD_QUERY, ANNOTATION_QUERY, EXIST_BY, COUNT_BY, DELETE_BY, NOT_SUPPORTED, INSERT, UPDATE, DELETE, SAVE, PARAMETER_BASED;

    static MethodMetadataOperationType of(MethodMetadata metadata) {
        var methodName = metadata.getMethodName();

        if (metadata.hasQuery()) {
            return ANNOTATION_QUERY;
        } else if (metadata.isInsert()) {
            return INSERT;
        } else if (metadata.isDelete()) {
            return DELETE;
        } else if (metadata.isUpdate()) {
            return UPDATE;
        } else if (metadata.isSave()) {
            return SAVE;
        } else if (metadata.isFind()) {
            return PARAMETER_BASED;
        } else if (methodName.startsWith("findBy")) {
            return METHOD_QUERY;
        } else if (methodName.startsWith("countBy")) {
            return COUNT_BY;
        } else if (methodName.startsWith("existsBy")) {
            return EXIST_BY;
        } else if (methodName.startsWith("deleteBy")) {
            return DELETE_BY;
        }
        return NOT_SUPPORTED;
    }
}
