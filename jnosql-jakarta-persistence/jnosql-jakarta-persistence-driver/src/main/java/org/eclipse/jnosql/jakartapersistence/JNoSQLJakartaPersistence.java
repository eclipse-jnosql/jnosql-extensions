/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.jakartapersistence;

/**
 * Metadata about the Eclipse JNoSQL Jakarta Persistence extension.
 *
 * <p>This interface only holds constants; it is not meant to be implemented.</p>
 *
 * @author Ondro Mihalyi
 */
public interface JNoSQLJakartaPersistence {

    /**
     * The name that identifies this extension as the provider of Jakarta Data repositories backed by
     * Jakarta Persistence. Compare against it when a component needs to know which Jakarta Data provider is
     * handling a repository.
     */
    public static final String PROVIDER = "Eclipse_JNoSQL_Jakarta_Persistence";
}
