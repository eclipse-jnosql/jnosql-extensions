/*
 *  Copyright (c) 2020 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities;

import java.math.BigDecimal;

public record Money(String currency, BigDecimal bigDecimal) {

    public static Money of(String dbData) {
        String[] values = dbData.split(" ");
        String currency = values[0];
        BigDecimal value = BigDecimal.valueOf(Double.parseDouble(values[1]));
        return new Money(currency, value);
    }

    @Override
    public String toString() {
        return currency + " " + bigDecimal.toString();
    }
}
