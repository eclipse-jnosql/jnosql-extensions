/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping.metadata;

import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.MetadataException;

import java.util.ServiceLoader;

/**
 * This is an extension of {@link ClassScanner} which is to be used for Jakarta Persistence entities.
 * It contains the same methods as {@link ClassScanner} but returns Jakarta Persistence entities and
 * embeddables instead of NoSQL ones.
 * @author Ondro Mihalyi
 */
public interface JakartaPersistenceClassScanner extends ClassScanner {

    JakartaPersistenceClassScanner INSTANCE =  ServiceLoader.load(JakartaPersistenceClassScanner.class)
            .findFirst()
            .orElseThrow(() ->   new MetadataException("No implementation of JakartaPersistenceClassScanner found via ServiceLoader"));

    /**
     * Loads and returns an instance of the {@link JakartaPersistenceClassScanner} implementation using the ServiceLoader mechanism.
     *
     * @return An instance of the loaded {@link JakartaPersistenceClassScanner} implementation.
     * @throws IllegalStateException If no suitable implementation is found.
     */
    static JakartaPersistenceClassScanner load() {
        return INSTANCE;
    }
}
