/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import jakarta.data.exceptions.EntityExistsException;
import jakarta.data.exceptions.OptimisticLockingFailureException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Create or convert to exceptions that Jakarta Data expects
 * @author Ondro Mihalyi
 */
public final class DataExceptions {

    private DataExceptions() {
    }

    public static <T> EntityExistsException newEntityExistsException(T entity, Object identifier) {
         return new EntityExistsException("Entity of type " + entity.getClass() + " with id=" + identifier + " already exists");
    }

    public static Optional<OptimisticLockingFailureException> asOptimisticLockingFailureException(OptimisticLockException e, Object entity) {
        if (e.getEntity() == null || e.getEntity().equals(entity)) {
            return Optional.of(new OptimisticLockingFailureException(e.getMessage(), e));
        }
        return Optional.empty();
    }

    public static OptimisticLockingFailureException asOptimisticLockingFailureException(PersistenceException e) {
        return new OptimisticLockingFailureException(e.getMessage(), e);
    }

    public static Object handlePersistenceException(Callable<Object> callable) throws Exception {
        try {
            return callable.call();
        } catch (NonUniqueResultException e) {
            throw new jakarta.data.exceptions.NonUniqueResultException(e);
        } catch (PersistenceException e) {
            if (e.getCause() != null) {
                final Throwable cause = e.getCause();
                if (cause instanceof ConstraintViolationException cve) {
                    throw cve;
                }
            }
            throw e;
        }
    }

}
