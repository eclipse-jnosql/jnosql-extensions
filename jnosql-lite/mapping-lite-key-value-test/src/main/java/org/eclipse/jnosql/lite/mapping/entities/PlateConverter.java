/*
 *   Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License 2.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *    and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.entities;


import jakarta.nosql.AttributeConverter;

public class PlateConverter implements AttributeConverter<Plate, String> {

    @Override
    public String convertToDatabaseColumn(Plate attribute) {
        return attribute.toString();
    }

    @Override
    public Plate convertToEntityAttribute(String dbData) {
        return Plate.of(dbData);
    }
}
