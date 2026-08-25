/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.metamodel.processor;


final class FieldModel extends BaseMappingModel {
    public static final String STRING_ATTRIBUTE = "StringAttribute";
    public static final String BOOLEAN_ATTRIBUTE = "BooleanAttribute";
    public static final String CRITERIA_ATTRIBUTE = "CriteriaAttribute";

    private String className;
    private String fieldName;
    private String name;

    private String constantName;

    private FieldModel() {
    }

    public String getClassName() {
        return className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getName() {
        return name;
    }

    public String getConstantName() {
        return constantName;
    }

    public boolean isStringAttribute() {
        return STRING_ATTRIBUTE.equals(className);
    }

    public boolean isBooleanAttribute() {
        return BOOLEAN_ATTRIBUTE.equals(className);
    }

    public boolean isCriteriaAttribute() {
        return CRITERIA_ATTRIBUTE.equals(className);
    }

    @Override
    public String toString() {
        return "FieldModel{" +
                "className='" + className + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", name='" + name + '\'' +
                ", constantName='" + constantName + '\'' +
                '}';
    }

    public static FieldMetaDataBuilder builder() {
        return new FieldMetaDataBuilder();
    }


    public static class FieldMetaDataBuilder {

        private final FieldModel fieldModel;

        private FieldMetaDataBuilder() {
            this.fieldModel = new FieldModel();
        }

        public FieldMetaDataBuilder className(String className) {
            this.fieldModel.className = className;
            return this;
        }

        public FieldMetaDataBuilder name(String name) {
            this.fieldModel.name = name;
            return this;
        }

        public FieldMetaDataBuilder fieldName(String fieldName) {
            this.fieldModel.fieldName = fieldName;
            return this;
        }

        public FieldMetaDataBuilder constantName(String constantName) {
            this.fieldModel.constantName = constantName;
            return this;
        }

        FieldModel build() {
           return fieldModel;
        }
    }
}
