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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;

enum AttributeElementType {

    TEXT_ATTRIBUTE("TextAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "TextAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, \"" + fieldModel.getConstantName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "TextAttribute<" + fieldModel.getEntitySimpleName() + ">";
        }
    },
    SORTABLE_ATTRIBUTE("SortableAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new SortableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new SortableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    COMPARABLE_ATTRIBUTE("ComparableAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new ComparableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new ComparableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    NUMERIC_ATTRIBUTE("NumericAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new NumericAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new NumericAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    NAVIGABLE_ATTRIBUTE("NavigableAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "NavigableAttribute.of(" + fieldModel.getEntitySimpleName() + ".class, \""
                    + fieldModel.getConstantName()
                    + "\", " + fieldModel.getType() + ".class)";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "NavigableAttribute<" + fieldModel.getEntitySimpleName() + ", " + fieldModel.getSimpleName() + ">";
        }
    },
    TEMPORAL_ATTRIBUTE("TemporalAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new TemporalAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new TemporalAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    BASIC_ATTRIBUTE("BasicAttribute") {
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new BasicAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new BasicAttributeRecord<>(\"" + fieldModel.getName() + "\")";
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

    public static AttributeElementType of(String className) {
        return switch (className) {
            case "java.lang.String", "java.lang.CharSequence" -> TEXT_ATTRIBUTE;
            case "java.lang.Integer", "java.lang.Long", "java.lang.Double", "java.lang.Float", "java.lang.Short",
                 "java.lang.Byte", "java.lang.Number", "java.math.BigDecimal", "java.math.BigInteger", "int", "long",
                 "double", "float", "short", "byte" -> NUMERIC_ATTRIBUTE;
            case "java.time.LocalDateTime", "java.time.LocalDate", "java.time.LocalTime",
                 "java.time.Instant", "java.time.Year", "java.time.YearMonth" -> TEMPORAL_ATTRIBUTE;
            case "java.lang.Boolean" -> SORTABLE_ATTRIBUTE;
            default -> BASIC_ATTRIBUTE;
        };
    }
}
