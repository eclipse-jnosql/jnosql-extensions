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
 * Creates the connection to {@link Graph} using ArangoDB.
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


    private static class EdgeConfiguration {

        private final String edge;

        private final String source;

        private final String target;

        private EdgeConfiguration(String source, String edge, String target) {
            this.edge = edge;
            this.source = source;
            this.target = target;
        }

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
