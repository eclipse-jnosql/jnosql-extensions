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

import com.arangodb.tinkerpop.gremlin.utils.ArangoDBConfigurationBuilder;
import org.apache.commons.configuration.BaseConfiguration;
import org.eclipse.jnosql.communication.Settings;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.eclipse.jnosql.databases.tinkerpop.communication.GraphConfiguration;

import java.util.Objects;

import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.EDGE;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.EDGE_RELATIONSHIP;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.GRAPH;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.HOST;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.PASSWORD;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.USER;
import static org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations.VERTEX;

/**
 * An implementation of {@link GraphConfiguration} for creating and managing a connection
 * to a {@link org.apache.tinkerpop.gremlin.structure.Graph} using ArangoDB.
 * <p>
 * This class utilizes an {@link ArangoDBConfigurationBuilder} to configure the ArangoDB graph settings
 * based on the provided {@link org.eclipse.jnosql.communication.Settings}. It supports setting hosts,
 * user credentials, graph name, vertex collections, edge collections, and edge relationships.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * ArangoDBGraphConfiguration configuration = new ArangoDBGraphConfiguration();
 * Settings settings = new Settings();
 * settings.put(ArangoDBGraphConfigurations.HOST.get() + ".1", "localhost:8529");
 * settings.put(ArangoDBGraphConfigurations.USER.get(), "root");
 * settings.put(ArangoDBGraphConfigurations.PASSWORD.get(), "password");
 * settings.put(ArangoDBGraphConfigurations.GRAPH.get(), "my_graph");
 * Graph graph = configuration.apply(settings);
 * }
 * </pre>
 * <p>
 * The {@link Settings} object should include the required configuration properties to successfully
 * establish the connection, including:
 * <ul>
 *   <li><b>HOST</b>: Specifies the ArangoDB host(s).</li>
 *   <li><b>USER</b>: Specifies the username for authentication.</li>
 *   <li><b>PASSWORD</b>: Specifies the password for authentication.</li>
 *   <li><b>GRAPH</b>: Specifies the name of the graph to use.</li>
 *   <li><b>VERTEX</b>: Specifies the vertex collections.</li>
 *   <li><b>EDGE</b>: Specifies the edge collections.</li>
 *   <li><b>EDGE_RELATIONSHIP</b>: Specifies the edge relationships in the format: source|edge|target.</li>
 * </ul>
 * </p>
 *
 * @see Graph
 * @see GraphConfiguration
 * @see ArangoDBConfigurationBuilder
 */
public class ArangoDBGraphConfiguration implements GraphConfiguration {


    @Override
    public Graph apply(Settings settings) {
        Objects.requireNonNull(settings, "settings is required");
        ArangoDBConfigurationBuilder builder = new ArangoDBConfigurationBuilder();

        settings.prefix(HOST)
                .stream()
                .map(Object::toString)
                .forEach(builder::arangoHosts);

        settings.prefix(VERTEX)
                .stream()
                .map(Object::toString)
                .forEach(builder::withVertexCollection);

        settings.prefix(EDGE)
                .stream()
                .map(Object::toString)
                .forEach(builder::withEdgeCollection);


        settings.get(USER)
                .map(Object::toString)
                .ifPresent(builder::arangoUser);

        settings.get(PASSWORD)
                .map(Object::toString)
                .ifPresent(builder::arangoPassword);

        settings.get(GRAPH)
                .map(Object::toString)
                .ifPresent(builder::graph);

        settings.prefix(EDGE_RELATIONSHIP)
                .stream()
                .map(EdgeConfiguration::parse)
                .forEach(e -> e.add(builder));
        BaseConfiguration configuration = builder.build();

        var conf2 = new org.apache.commons.configuration2.BaseConfiguration();
        configuration.getKeys().forEachRemaining(k -> {
            conf2.addProperty(k, configuration.getProperty(k));
        });
        return GraphFactory.open(conf2);
    }


    private record EdgeConfiguration(String source, String edge, String target) {

        static EdgeConfiguration parse(Object value) {
                final String[] values = value.toString().split("\\|");
                if (values.length != 3) {
                    throw new IllegalArgumentException("The element is valid it must have" +
                            " three element split by pipe: " + value);
                }
                return new EdgeConfiguration(values[0], values[1], values[2]);
            }

            private void add(ArangoDBConfigurationBuilder builder) {
                builder.configureEdge(edge, source, target);
            }
        }
}
