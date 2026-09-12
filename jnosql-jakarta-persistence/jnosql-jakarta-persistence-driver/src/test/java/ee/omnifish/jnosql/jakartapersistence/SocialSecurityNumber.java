/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Renat R. Safiullin
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * An embeddable value type that deliberately does not implement {@link Comparable},
 * to cover equality comparisons against values with no natural ordering.
 */
@Embeddable
public class SocialSecurityNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "SSN")
    private String number;

    public SocialSecurityNumber() {
    }

    public SocialSecurityNumber(String number) {
        this.number = Objects.requireNonNull(number, "number is required");
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SocialSecurityNumber other)) {
            return false;
        }
        return Objects.equals(number, other.number);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number);
    }

    @Override
    public String toString() {
        return "SocialSecurityNumber{" + number + '}';
    }
}
