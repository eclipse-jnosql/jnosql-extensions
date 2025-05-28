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

enum AttributeElementType {

    TEXT_ATTRIBUTE("TextAttribute"){
        @Override
        String newInstance(FieldModel fieldModel) {
            return "TextAttribute.of(" + fieldModel.getEntityName() + ".class, \"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "TextAttribute.of(" + fieldModel.getEntityName() + ".class, \"" + fieldModel.getName() + "\")";
        }
    },
    SORTABLE_ATTRIBUTE("SortableAttribute"){
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new SortableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new SortableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    COMPARABLE_ATTRIBUTE("ComparableAttribute"){
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new ComparableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new ComparableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    NUMERIC_ATTRIBUTE("NumericAttribute"){
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new NumericAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new NumericAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    NAVIGABLE_ATTRIBUTE("NavigableAttribute"){
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new NavigableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new NavigableAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    TEMPORAL_ATTRIBUTE("TemporalAttribute"){
        @Override
        String newInstance(FieldModel fieldModel) {
            return "new TemporalAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }

        @Override
        String attribute(FieldModel fieldModel) {
            return "new TemporalAttributeRecord<>(\"" + fieldModel.getName() + "\")";
        }
    },
    BASIC_ATTRIBUTE("BasicAttribute"){
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
}
