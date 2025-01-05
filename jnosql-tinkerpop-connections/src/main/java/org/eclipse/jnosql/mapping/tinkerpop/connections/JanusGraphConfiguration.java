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
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.databases.tinkerpop.communication.GraphConfiguration;
import org.janusgraph.core.JanusGraphFactory;

import java.util.Objects;

/**
 * An implementation of {@link GraphConfiguration} for creating and managing a connection
 * to a {@link org.apache.tinkerpop.gremlin.structure.Graph} using JanusGraph.
 * <p>
 * This class leverages JanusGraph's {@link org.janusgraph.core.JanusGraphFactory} to create
 * a {@link Graph} instance based on the configuration provided through {@link org.eclipse.jnosql.communication.Settings}.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * JanusGraphConfiguration configuration = new JanusGraphConfiguration();
 * Settings settings = new Settings();
 * settings.put("storage.backend", "cassandra");
 * settings.put("storage.hostname", "127.0.0.1");
 * Graph graph = configuration.apply(settings);
 * }
 * </pre>
 * <p>
 * The {@link Settings} object should contain the necessary configuration properties required
 * by JanusGraph, such as:
 * <ul>
 *   <li><b>storage.backend</b>: Specifies the storage backend (e.g., cassandra, hbase, inmemory).</li>
 *   <li><b>storage.hostname</b>: Specifies the hostname of the storage backend.</li>
 *   <li>Additional properties as required by the JanusGraph configuration.</li>
 * </ul>
 * </p>
 * <p>
 * Ensure that all required properties are included to successfully connect to the desired storage backend.
 * </p>
 *
 * @see Graph
 * @see GraphConfiguration
 * @see org.janusgraph.core.JanusGraphFactory
 */

public class JanusGraphConfiguration implements GraphConfiguration {

    @Override
    public Graph apply(Settings settings) {
        Objects.requireNonNull(settings, "settings is required");
        Configuration configuration = new BaseConfiguration();
        for (String key : settings.keySet()) {
            settings.get(key, String.class).ifPresent(v -> configuration.addProperty(key, v));
        }
        return JanusGraphFactory.open(configuration);
    }
}
