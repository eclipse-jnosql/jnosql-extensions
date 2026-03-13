/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
/**
 * Provides integration between Eclipse JNoSQL and SQL-capable databases
 * through the Jakarta Persistence API.
 *
 * <p>This extension enables JNoSQL repositories to operate on databases that
 * expose SQL query capabilities. It assumes that the underlying database
 * provides a JDBC-compatible driver and that Jakarta Persistence is available
 * to manage entity mapping and persistence operations.</p>
 *
 * <p>In this module:</p>
 * <ul>
 *     <li><strong>JDBC</strong> is used as the communication layer with the database.</li>
 *     <li><strong>Jakarta Persistence</strong> provides the entity mapping,
 *     persistence context, and lifecycle management.</li>
 *     <li><strong>JNoSQL</strong> provides repository abstractions and integrates
 *     them with SQL-capable datastores.</li>
 * </ul>
 *
 * <p>This design allows JNoSQL repositories to interact with databases that
 * support SQL queries, including:</p>
 *
 * <ul>
 *     <li>Traditional relational databases</li>
 *     <li>NewSQL databases</li>
 *     <li>Document or multi-model databases that expose SQL-like query engines</li>
 * </ul>
 *
 * <p>Because database capabilities may vary, full compatibility cannot be
 * guaranteed across all providers. Some repository operations may not be
 * supported by certain databases or drivers. In such cases, implementations
 * may throw an {@link java.lang.UnsupportedOperationException}.</p>
 *
 * <p>This module acts as an extension to the core JNoSQL architecture,
 * complementing other data model implementations such as semistructured,
 * key-value, and column databases.</p>
 *
 * <p>The goal of this package is to enable JNoSQL repositories to leverage
 * SQL-based persistence infrastructures while preserving the JNoSQL programming
 * model.</p>
 */
package org.eclipse.jnosql.extensions.sql;