/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.extensions.sql.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.Objects;

import static jakarta.persistence.GenerationType.AUTO;

@Entity
public class Computer {

    @Id
    @GeneratedValue(strategy = AUTO)
    private long id;

    @Column
    private String model;

    @Column
    private long release;


    public Computer() {
    }

    private Computer(String model, long release) {
        this.model = model;
        this.release = release;
    }

    public long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public long getRelease() {
        return release;
    }

    public Computer setId(long id) {
        this.id = id;
        return this;
    }

    public Computer setModel(String model) {
        this.model = model;
        return this;
    }

    public Computer setRelease(long release) {
        this.release = release;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Computer computer)) {
            return false;
        }
        return id == computer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Computer{" +
                "id=" + id +
                ", model='" + model + '\'' +
                ", release=" + release +
                '}';
    }

    public static Computer of(String model, long release) {
        return new Computer(model, release);
    }
}
