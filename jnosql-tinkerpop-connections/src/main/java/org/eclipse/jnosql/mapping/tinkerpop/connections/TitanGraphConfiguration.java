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

import com.thinkaurelius.titan.core.TitanFactory;
import org.eclipse.jnosql.communication.Settings;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.databases.tinkerpop.communication.GraphConfiguration;

import java.util.Objects;

/**
 * An implementation of {@link GraphConfiguration} for creating and managing a connection
 * to a {@link org.apache.tinkerpop.gremlin.structure.Graph} using Titan.
 * This class leverages Titan's {@link com.thinkaurelius.titan.core.TitanFactory} to create
 * a {@link Graph} instance based on the provided {@link Settings}.
 * Example usage:
 * <pre>
 * {@code
 * TitanGraphConfiguration configuration = new TitanGraphConfiguration();
 * Settings settings = new Settings();
 * settings.put("storage.backend", "cassandra");
 * settings.put("storage.hostname", "127.0.0.1");
 * Graph graph = configuration.apply(settings);
 * }
 * </pre>
 * Ensure the {@link Settings} object contains all required configuration
 * properties for the Titan storage backend and related settings.
 *
 * @see Graph
 * @see GraphConfiguration
 * @see com.thinkaurelius.titan.core.TitanFactory
 */
public class TitanGraphConfiguration implements GraphConfiguration {

    @Override
    public Graph apply(Settings settings) {
        Objects.requireNonNull(settings, "settings is required");
        var configuration = new BaseConfiguration();
        for (String key : settings.keySet()) {
            settings.get(key, String.class).ifPresent(v -> configuration.addProperty(key, v));
        }
        return TitanFactory.open(configuration);
    }
}
