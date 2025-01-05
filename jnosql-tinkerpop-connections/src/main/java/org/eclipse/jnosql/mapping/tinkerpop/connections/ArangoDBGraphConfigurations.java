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
 * An enumeration to represent the available configuration options for connecting to an ArangoDB database
 * when using it as a graph database. Each enum constant corresponds to a specific property key that can
 * be used to configure the connection and graph settings.
 * <p>
 * This enumeration implements {@link Supplier}, allowing each constant to provide its property key
 * as a string value. These keys can be overridden by the system environment or a configuration source
 * compliant with the Eclipse MicroProfile Config or Jakarta Config API.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * String edgeCollectionKey = ArangoDBGraphConfigurations.EDGE.get();
 * String graphName = ArangoDBGraphConfigurations.GRAPH.get();
 * String host = ArangoDBGraphConfigurations.HOST.get();
 * }
 * </pre>
 * <p>
 * Default property keys and their descriptions:
 * <ul>
 *   <li><b>EDGE</b>: Specifies the edge collection prefix. Example: {@code jnosql.arangodb.graph.edge.1=edge}</li>
 *   <li><b>EDGE_RELATIONSHIP</b>: Specifies the edge collection, source vertex collection, and target vertex collection, separated by a pipe.
 *   Example: {@code jnosql.arangodb.graph.relationship.1=Person|knows|Person}</li>
 *   <li><b>VERTEX</b>: Specifies the vertex collection prefix. Example: {@code jnosql.arangodb.graph.vertex.1=vertex}</li>
 *   <li><b>GRAPH</b>: Specifies the name of the graph to use.</li>
 *   <li><b>HOST</b>: Specifies the database host.</li>
 *   <li><b>USER</b>: Specifies the username credential for the database connection.</li>
 *   <li><b>PASSWORD</b>: Specifies the password credential for the database connection.</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jnosql.communication.Settings
 */
public enum ArangoDBGraphConfigurations implements Supplier<String> {

    /**
     * The edge collection prefix. Example: {@code jnosql.arangodb.graph.edge.1=edge}.
     */
    EDGE("jnosql.arangodb.graph.edge"),

    /**
     * Specifies the edge collection, source vertex collection, and target vertex collection, separated by a pipe.
     * Example: {@code jnosql.arangodb.graph.relationship.1=Person|knows|Person}.
     */
    EDGE_RELATIONSHIP("jnosql.arangodb.graph.relationship"),

    /**
     * The vertex collection prefix. Example: {@code jnosql.arangodb.graph.vertex.1=vertex}.
     */
    VERTEX("jnosql.arangodb.graph.vertex"),

    /**
     * The name of the graph to use in the ArangoDB database.
     */
    GRAPH("jnosql.arangodb.graph.graph"),

    /**
     * The database host for the ArangoDB connection.
     */
    HOST("jnosql.arangodb.graph.host"),

    /**
     * The username credential for accessing the ArangoDB database.
     */
    USER("jnosql.arangodb.graph.user"),

    /**
     * The password credential for accessing the ArangoDB database.
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

