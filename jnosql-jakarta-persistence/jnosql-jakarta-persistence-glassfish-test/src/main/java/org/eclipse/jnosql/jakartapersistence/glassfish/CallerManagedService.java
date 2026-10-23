/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.jakartapersistence.glassfish;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import java.util.function.Consumer;

/**
 * Caller-managed transactions: the bean owns the {@link UserTransaction}, the repository must only join it.
 */
@ApplicationScoped
public class CallerManagedService {

    @Resource
    private UserTransaction userTransaction;

    @Inject
    private ItemRepository repository;

    public void commitAfter(Consumer<ItemRepository> work) throws Exception {
        userTransaction.begin();
        try {
            work.accept(repository);
        } catch (RuntimeException e) {
            userTransaction.rollback();
            throw e;
        }
        userTransaction.commit();
    }

    public void rollbackAfter(Consumer<ItemRepository> work) throws Exception {
        userTransaction.begin();
        try {
            work.accept(repository);
        } finally {
            userTransaction.rollback();
        }
    }

    public boolean exists(long id) {
        return repository.findById(id).isPresent();
    }
}
