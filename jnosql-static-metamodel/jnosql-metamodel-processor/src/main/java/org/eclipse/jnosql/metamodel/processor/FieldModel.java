/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.metamodel.processor;


final class FieldModel extends BaseMappingModel {

    private String entityName;
    private String className;
    private String fieldName;
    private String name;
    private String constantName;
    private AttributeElementType type;

    private FieldModel() {
    }

    public String getEntityName() {
        return entityName;
    }

    public AttributeElementType getType() {
        return type;
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

    public boolean isTextAttribute() {
        return AttributeElementType.TEXT_ATTRIBUTE.equals(type);
    }

    public boolean isSortableAttribute() {
        return AttributeElementType.SORTABLE_ATTRIBUTE.equals(type);
    }

    public boolean isComparableAttribute() {
        return AttributeElementType.COMPARABLE_ATTRIBUTE.equals(type);
    }

    public boolean isNumericAttribute() {
        return AttributeElementType.NUMERIC_ATTRIBUTE.equals(type);
    }

    public boolean isTemporalAttribute() {
        return AttributeElementType.TEMPORAL_ATTRIBUTE.equals(type);
    }

    public boolean isBasicAttribute() {
        return AttributeElementType.BASIC_ATTRIBUTE.equals(type);
    }

    public boolean isNavigableAttribute() {
        return AttributeElementType.NAVIGABLE_ATTRIBUTE.equals(type);
    }

    public String getNewInstance(){
        return type.newInstance(this);
    }
    public String getAttribute() {
        return type.attribute(this);
    }

    @Override
    public String toString() {
        return "FieldModel{" +
                "className='" + className + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", name='" + name + '\'' +
                ", constantName='" + constantName + '\'' +
                ", type=" + type +
                '}';
    }

    static FieldMetaDataBuilder builder() {
        return new FieldMetaDataBuilder();
    }

    static class FieldMetaDataBuilder {

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

        public FieldMetaDataBuilder type(AttributeElementType type) {
            this.fieldModel.type = type;
            return this;
        }

        public FieldMetaDataBuilder entityName(String entityName) {
            this.fieldModel.entityName = entityName;
            return this;
        }

        FieldModel build() {
           return fieldModel;
        }
    }
}
