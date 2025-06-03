/*
 *  Copyright (c) 2025 Otávio Santana and others
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

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

enum AttributeElementType {

    TEXT_ATTRIBUTE("TextAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "TextAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, " + fieldModel.getConstantName() + ")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "TextAttribute<" + fieldModel.getEntitySimpleName() + ">";
        }
    },
    SORTABLE_ATTRIBUTE("SortableAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "SortableAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, "
                    + fieldModel.getConstantName()
                    + ", " + fieldModel.getSimpleName() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "SortableAttribute<" + fieldModel.getEntitySimpleName() + ">";
        }
    },
    COMPARABLE_ATTRIBUTE("ComparableAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "ComparableAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, "
                    + fieldModel.getConstantName()
                    + ", " + fieldModel.getSimpleName() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "ComparableAttribute<" + fieldModel.getEntitySimpleName() + ", " + BasicPrimitiveNumber.INSTANCE.toWrapper(fieldModel.getSimpleName()) + ">";
        }
    },
    NUMERIC_ATTRIBUTE("NumericAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "NumericAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, "
                    + fieldModel.getConstantName()
                    + ", " + fieldModel.getSimpleName() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {

            return "NumericAttribute<" + fieldModel.getEntitySimpleName() + ", " + BasicPrimitiveNumber.INSTANCE.toWrapper(fieldModel.getSimpleName()) + ">";
        }
    },
    NAVIGABLE_ATTRIBUTE("NavigableAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "NavigableAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, "
                    + fieldModel.getConstantName()
                    + ", " + fieldModel.getSimpleName() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "NavigableAttribute<" + fieldModel.getEntitySimpleName() + ", " + fieldModel.getSimpleName() + ">";
        }
    },
    TEMPORAL_ATTRIBUTE("TemporalAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "TemporalAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, "
                    + fieldModel.getConstantName()
                    + ", " + fieldModel.getSimpleName() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "NavigableAttribute<" + fieldModel.getEntitySimpleName() + ", " + fieldModel.getSimpleName() + ">";
        }
    },
    BASIC_ATTRIBUTE("BasicAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "BasicAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, "
                    + fieldModel.getConstantName()
                    + ", " + fieldModel.getSimpleName() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "BasicAttribute<" + fieldModel.getEntitySimpleName() + ", " + fieldModel.getSimpleName() + ">";
        }
    };

    private final String type;

    AttributeElementType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    abstract String newInstance(FieldModel fieldModel);

    abstract String attribute(FieldModel fieldModel);


    public static AttributeElementType of(TypeMirror type, Types typeUtils, Elements elementUtils) {
        if (type.getKind().isPrimitive()) {
            switch (type.getKind()) {
                case INT, LONG, DOUBLE, FLOAT, SHORT, BYTE -> {
                    return NUMERIC_ATTRIBUTE;
                }
                case BOOLEAN -> {
                    return COMPARABLE_ATTRIBUTE;
                }
                default ->  {
                    return BASIC_ATTRIBUTE;
                }
            }
        }

        if (typeUtils.isAssignable(type, elementUtils.getTypeElement("java.lang.CharSequence").asType())) {
            return TEXT_ATTRIBUTE;
        }

        if (typeUtils.isAssignable(type, elementUtils.getTypeElement("java.lang.Number").asType())) {
            return NUMERIC_ATTRIBUTE;
        }

        if (typeUtils.isAssignable(type, elementUtils.getTypeElement("java.lang.Comparable").asType())) {
            return COMPARABLE_ATTRIBUTE;
        }

        if (type.toString().equals("byte[]")) {
            return SORTABLE_ATTRIBUTE;
        }

        return switch (type.toString()) {
            case "java.time.LocalDateTime", "java.time.LocalDate", "java.time.LocalTime",
                 "java.time.Instant", "java.time.Year", "java.time.YearMonth" -> TEMPORAL_ATTRIBUTE;
            case "java.lang.Boolean" -> COMPARABLE_ATTRIBUTE;
            default -> BASIC_ATTRIBUTE;
        };
    }
}
