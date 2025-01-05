/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.tinkerpop.connections;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import org.eclipse.jnosql.communication.Settings;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.databases.tinkerpop.communication.GraphConfiguration;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.Objects;

import static org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JGraphConfigurations.HOST;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JGraphConfigurations.PASSWORD;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JGraphConfigurations.USER;


/**
 * An implementation of {@link GraphConfiguration} for creating and managing a connection
 * to a {@link org.apache.tinkerpop.gremlin.structure.Graph} using a Neo4J remote database.
 * <p>
 * This class utilizes Neo4J's {@link org.neo4j.driver.GraphDatabase} and associated components
 * to establish a connection and configure the graph instance.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * Neo4JGraphConfiguration configuration = new Neo4JGraphConfiguration();
 * Settings settings = new Settings();
 * settings.put(Neo4JGraphConfigurations.HOST.get(), "bolt://localhost:7687");
 * settings.put(Neo4JGraphConfigurations.USER.get(), "neo4j");
 * settings.put(Neo4JGraphConfigurations.PASSWORD.get(), "password");
 * Graph graph = configuration.apply(settings);
 * }
 * </pre>
 * <p>
 * The {@link Settings} object should provide valid connection details, including
 * the host URL, user credentials, and password. Default values are:
 * <ul>
 *   <li>Host: "bolt://localhost:7687"</li>
 *   <li>User: "neo4j"</li>
 *   <li>Password: "neo4j"</li>
 * </ul>
 * </p>
 *
 * @see Graph
 * @see GraphConfiguration
 * @see org.neo4j.driver.GraphDatabase
 * @see org.neo4j.driver.AuthTokens
 */
public class Neo4JGraphConfiguration implements GraphConfiguration {

    @Override
    public Graph apply(Settings settings) {
        Objects.requireNonNull(settings, "settings is required");

        String url = settings.getOrDefault(HOST.get(), "bolt://localhost:7687").toString();
        String user = settings.getOrDefault(USER.get(), "neo4j").toString();
        String password = settings.getOrDefault(PASSWORD.get(), "neo4j").toString();
        AuthToken basic = AuthTokens.basic(user, password);
        Driver driver = GraphDatabase.driver(url, basic);
        Neo4JElementIdProvider<Long> vertexIdProvider = new Neo4JNativeElementIdProvider();
        Neo4JElementIdProvider<Long> edgeIdProvider = new Neo4JNativeElementIdProvider();
        return new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider);
    }
}
