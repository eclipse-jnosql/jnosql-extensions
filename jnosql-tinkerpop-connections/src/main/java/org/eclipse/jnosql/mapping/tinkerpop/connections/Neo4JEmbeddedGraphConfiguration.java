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

import org.eclipse.jnosql.communication.Settings;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.databases.tinkerpop.communication.GraphConfiguration;

import java.util.Objects;

import static org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JGraphConfigurations.HOST;

/**
 * Creates the connection to {@link Graph} using Neo4J Embedded.
 */
public class Neo4JEmbeddedGraphConfiguration implements GraphConfiguration {

    private static final String HOST_KEY = "gremlin.neo4j.directory";

    @Override
    public Graph apply(Settings settings) {
        Objects.requireNonNull(settings, "settings is required");
        Configuration config = new BaseConfiguration();
        for (String key : settings.keySet()) {
            settings.get(key, String.class).ifPresent(v -> config.addProperty(key, v));
        }
        settings.get(HOST.get())
                .map(Object::toString)
                .ifPresent(h -> config.addProperty(HOST_KEY, h));
        return Neo4jGraph.open(config);
    }
}
