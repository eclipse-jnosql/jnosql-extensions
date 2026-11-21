/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.rest;

/**
 * Categories of repository operations that can be excluded with {@link RestRepository#exclude()}.
 *
 * <p>The category is derived from the semantics of the operation, not only from its name. A Jakarta Data
 * {@code @Query} whose statement starts with {@code DELETE} belongs to {@link #DELETE}, one that starts with
 * {@code UPDATE} belongs to {@link #UPDATE}, and any other statement belongs to {@link #FIND}.</p>
 *
 * @see RestRepository#exclude()
 */
public enum RestOperation {

    /**
     * Read operations: {@code findById}, {@code findAll}, {@code findBy...}, {@code @Find} and select queries.
     */
    FIND,

    /**
     * Creation operations: {@code insert} and {@code @Insert}.
     */
    INSERT,

    /**
     * Replacement operations: {@code update}, {@code @Update} and update queries.
     */
    UPDATE,

    /**
     * Upsert operations: {@code save} and {@code @Save}.
     */
    SAVE,

    /**
     * Removal operations: {@code deleteById}, {@code deleteBy...}, {@code @Delete} and delete queries.
     */
    DELETE,

    /**
     * Counting operations: {@code countBy...}.
     */
    COUNT,

    /**
     * Existence operations: {@code existsBy...}.
     */
    EXISTS
}
