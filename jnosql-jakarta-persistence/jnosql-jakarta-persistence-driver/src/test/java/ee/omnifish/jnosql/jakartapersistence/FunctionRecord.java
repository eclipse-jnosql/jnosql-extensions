/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/Apache-2.0.
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** Fixture with a text pair and a numeric value, used to verify native functions. */
@Entity(name = "FunctionRecord")
public class FunctionRecord {

    @Id
    private long id;

    @Column
    private String name;

    @Column(name = "other_name")
    private String otherName;

    @Column
    private long balance;

    public FunctionRecord() {
    }

    public FunctionRecord(long id, String name, String otherName, long balance) {
        this.id = id;
        this.name = name;
        this.otherName = otherName;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOtherName() {
        return otherName;
    }

    public long getBalance() {
        return balance;
    }
}
