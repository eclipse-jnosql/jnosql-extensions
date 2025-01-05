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
 * Creates the connection to {@link Graph} using Neo4J remote.
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
