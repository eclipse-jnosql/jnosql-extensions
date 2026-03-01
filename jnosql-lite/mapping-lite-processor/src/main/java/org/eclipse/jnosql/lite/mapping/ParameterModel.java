/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping;

import jakarta.nosql.Convert;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

final class ParameterModel extends BaseMappingModel {

    private String packageName;
    private String name;
    private String type;
    private String entity;
    private String fieldName;
    private boolean id;
    private String converter;

    private String mappingType;

    private String typeConverter;

    private String elementType;
    private String valueType;
    private boolean embeddable;

    private String collectionInstance;

    private String supplierElement;

    private String udt;

    private String newArrayInstance;

    private String arrayElement;

    private ParameterModel() {
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getEntity() {
        return entity;
    }

    public String getQualified() {
        return packageName + "." + getClassName();
    }

    public String getClassName() {
        return entity + ProcessorUtil.capitalize(fieldName) + "ParameterMetaData";
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isId() {
        return id;
    }

    public String getValueType() {
        return valueType;
    }

    public String getConverter() {
        return converter;
    }

    public String getMappingType() {
        return mappingType;
    }

    public String getTypeConverter() {
        return typeConverter;
    }

    public String getElementType() {
        return elementType;
    }

    public boolean isEmbeddable() {
        return embeddable;
    }

    public String getCollectionInstance() {
        return collectionInstance;
    }

    public String getSupplierElement() {
        return supplierElement;
    }

    public String getUdt() {
        return udt;
    }

    public String getNewArrayInstance() {
        return newArrayInstance;
    }

    public String getArrayElement() {
        return arrayElement;
    }

    @Override
    public String toString() {
        return "ParameterModel{" +
                "packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", entity='" + entity + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", id=" + id +
                ", converter='" + converter + '\'' +
                ", mappingType='" + mappingType + '\'' +
                ", typeConverter='" + typeConverter + '\'' +
                ", elementType='" + elementType + '\'' +
                ", valueType='" + valueType + '\'' +
                ", embeddable=" + embeddable +
                ", collectionInstance='" + collectionInstance + '\'' +
                ", supplierElement='" + supplierElement + '\'' +
                ", udt='" + udt + '\'' +
                ", newArrayInstance='" + newArrayInstance + '\'' +
                '}';
    }

    public static ParameterMetaDataBuilder builder() {
        return new ParameterMetaDataBuilder();
    }


    public static class ParameterMetaDataBuilder {

        private final ParameterModel fieldModel;

        private ParameterMetaDataBuilder() {
            this.fieldModel = new ParameterModel();
            this.fieldModel.converter = "null";
            this.fieldModel.mappingType = "null";
            this.fieldModel.typeConverter = "null";
            this.fieldModel.udt = "Optional.empty()";
        }

        public ParameterMetaDataBuilder packageName(String packageName) {
            this.fieldModel.packageName = packageName;
            return this;
        }

        public ParameterMetaDataBuilder name(String name) {
            this.fieldModel.name = name;
            return this;
        }

        public ParameterMetaDataBuilder type(String type) {
            this.fieldModel.type = type;
            return this;
        }

        public ParameterMetaDataBuilder entity(String entity) {
            this.fieldModel.entity = entity;
            return this;
        }

        public ParameterMetaDataBuilder fieldName(String fieldName) {
            this.fieldModel.fieldName = fieldName;
            return this;
        }

        public ParameterMetaDataBuilder udt(String udt) {
            if(udt != null && !udt.isEmpty() && !udt.isBlank()) {
                this.fieldModel.udt = String.format("Optional.of(\"%s\")", udt);
            }
            return this;
        }

        public ParameterMetaDataBuilder id(boolean id) {
            this.fieldModel.id = id;
            return this;
        }

        public ParameterMetaDataBuilder converter(Convert converter) {
            if (Objects.nonNull(converter)) {
                try {
                    this.fieldModel.converter = String.format("new %s();", converter.value().getName());
                    this.fieldModel.typeConverter = converter.value().getName().concat(".class");
                } catch (MirroredTypeException exception) {
                    TypeMirror typeMirror = exception.getTypeMirror();
                    this.fieldModel.converter = String.format("new %s()", typeMirror);
                    this.fieldModel.typeConverter = typeMirror.toString().concat(".class");
                }

            }
            return this;
        }

        public ParameterMetaDataBuilder mappingType(String mappingType) {
            this.fieldModel.mappingType = mappingType;
            return this;
        }

        public ParameterMetaDataBuilder elementType(String elementType) {
            this.fieldModel.elementType = elementType;
            return this;
        }

        public ParameterMetaDataBuilder valueType(String valueType) {
            this.fieldModel.valueType = valueType;
            return this;
        }

        public ParameterMetaDataBuilder embeddable(boolean embeddable) {
            this.fieldModel.embeddable = embeddable;
            return this;
        }

        public ParameterMetaDataBuilder collectionInstance(String collectionInstance) {
            this.fieldModel.collectionInstance = collectionInstance;
            return this;
        }

        public ParameterMetaDataBuilder supplierElement(String supplierElement) {
            this.fieldModel.supplierElement = supplierElement;
            return this;
        }

        public ParameterMetaDataBuilder newArrayInstance(String newArrayInstance) {
            this.fieldModel.newArrayInstance = newArrayInstance;
            return this;
        }

        public ParameterMetaDataBuilder arrayElement(String arrayElement) {
            this.fieldModel.arrayElement = arrayElement;
            return this;
        }


        ParameterModel build() {
           return fieldModel;
        }
    }
}
