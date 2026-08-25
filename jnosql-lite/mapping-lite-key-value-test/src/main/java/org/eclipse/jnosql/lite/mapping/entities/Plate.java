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

import java.util.Objects;

public final class Plate {

    private final int prefix;

    private final String sufix;


    private Plate(int prefix, String sufix) {
        this.prefix = prefix;
        this.sufix = sufix;
    }

    public int getPrefix() {
        return prefix;
    }

    public String getSufix() {
        return sufix;
    }

    @Override
    public String toString() {
        return Integer.toString(prefix) + '-' + sufix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Plate plate = (Plate) o;
        return prefix == plate.prefix &&
                Objects.equals(sufix, plate.sufix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, sufix);
    }

    public static Plate of(String value) {
        String[] values = value.split("-");
        return new Plate(Integer.parseInt(values[0]), values[1]);
    }


}
