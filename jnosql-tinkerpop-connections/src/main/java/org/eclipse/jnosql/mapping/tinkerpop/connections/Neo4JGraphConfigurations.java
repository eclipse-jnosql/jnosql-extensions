/*
 *  Copyright (c) 2022 Eclipse Contribuitor
 * All rights reserved. This program and the accompanying materials
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *    You may elect to redistribute this code under either of these licenses.
 */

package org.eclipse.jnosql.mapping.tinkerpop.connections;

import java.util.function.Supplier;

/**
 * An enumeration to represent the available configuration options for connecting
 * to a Neo4J database. Each enum constant represents a property key that can be
 * used to configure the connection.
 *
 * This enumeration implements {@link Supplier}, enabling the retrieval of the
 * property key as a string. These keys can be overridden by the system environment
 * or a configuration source compliant with the Eclipse MicroProfile Config or Jakarta Config API.
 *
 * @see org.eclipse.jnosql.communication.Settings
 */
public enum Neo4JGraphConfigurations implements Supplier<String> {

    /**
     * The database host. Default: "bolt://localhost:7687"
     */
    HOST("jnosql.neo4j.host"),
    /**
     * The user's credential. Default: "neo4j"
     */
    USER("jnosql.neo4j.user"),
    /**
     * The password's credential. Default: "neo4j"
     */
    PASSWORD("jnosql.neo4j.password");

    private final String value;

    Neo4JGraphConfigurations(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }
}
