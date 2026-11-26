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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Fixture for the native function integration tests. The {@code otherName} attribute is mapped to a column whose
 * physical name differs from the Java attribute name.
 */
@Entity
public class Account {

    @Id
    private long id;

    @Column
    private String name;

    @Column(name = "OTHER_NAME_COLUMN")
    private String otherName;

    @Column
    private long balance;

    public Account() {
    }

    public static Account of(long id, String name, String otherName, long balance) {
        Account account = new Account();
        account.id = id;
        account.name = name;
        account.otherName = otherName;
        account.balance = balance;
        return account;
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
