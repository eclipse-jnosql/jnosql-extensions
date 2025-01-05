/*
 *  Copyright (c) 2022 Eclipse Contribuitor
 * All rights reserved. This program and the accompanying materials
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *    You may elect to redistribute this code under either of these licenses.
 */

package org.eclipse.jnosql.mapping.graph.connections;

import java.util.function.Supplier;

/**
 * An enumeration to show the available options to connect to the ArangoDB database.
 * It implements {@link Supplier}, where its it returns the property name that might be
 * overwritten by the system environment using Eclipse Microprofile or Jakarta Config API.
 *
 * @see org.eclipse.jnosql.communication.Settings
 */
public enum ArangoDBGraphConfigurations implements Supplier<String> {

    /**
     * The edge collection. It uses as a prefix. E.g.:jnosql.arangodb.graph.edge.1=edge
     */
    EDGE("jnosql.arangodb.graph.edge"),
    /**
     * Edge collection, the source vertex collection and the target vertex collection split by pipe.
     * It hou,It uses as a prefix.
     * E.g.: jnosql.arangodb.graph.relationship.1=Person|knows|Person
     */
    EDGE_RELATIONSHIP("jnosql.arangodb.graph.relationship"),
    /**
     * The vertex collection. It uses as a prefix. E.g.: jnosql.arangodb.graph.vertex.1=vertex
     */
    VERTEX("jnosql.arangodb.graph.vertex"),
    /**
     * Name of the graph to use.
     */
    GRAPH("jnosql.arangodb.graph.graph"),
    /**
     * The database host.
     */
    HOST("jnosql.arangodb.graph.host"),
    /**
     * The user's credential.
     */
    USER("jnosql.arangodb.graph.user"),
    /**
     * The password's credential.
     */
    PASSWORD("jnosql.arangodb.graph.password");

    private final String value;

    ArangoDBGraphConfigurations(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }
}
