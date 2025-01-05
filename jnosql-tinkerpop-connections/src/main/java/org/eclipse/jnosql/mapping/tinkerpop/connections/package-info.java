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

/**
 * This package contains classes and configurations for integrating with graph databases
 * using the Apache TinkerPop framework, with support for various database backends such as Neo4J,
 * JanusGraph, ArangoDB, and Titan.
 * The primary purpose of this package is to provide implementations of the {@link org.apache.tinkerpop.gremlin.structure.Graph}
 * interface through database-specific configurations, enabling seamless connection and interaction with graph databases.
 * Key components in this package include:
 * <ul>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JGraphConfiguration}:
 *       Configures and connects to Neo4J in remote mode.</li>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JEmbeddedGraphConfiguration}:
 *       Configures and connects to Neo4J in embedded mode.</li>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.JanusGraphConfiguration}:
 *       Configures and connects to a JanusGraph instance.</li>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.TitanGraphConfiguration}:
 *       Configures and connects to a Titan graph database.</li>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfiguration}:
 *       Configures and connects to an ArangoDB graph database.</li>
 * </ul>
 * The package also includes enumeration classes, such as:
 * <ul>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.ArangoDBGraphConfigurations}:
 *       Provides configuration keys for ArangoDB-specific settings.</li>
 *   <li>{@link org.eclipse.jnosql.mapping.tinkerpop.connections.Neo4JGraphConfigurations}:
 *       Provides configuration keys for Neo4J-specific settings.</li>
 * </ul>
 * Example usage of a graph configuration:
 * <pre>
 * {@code
 * Settings settings = new Settings();
 * settings.put(ArangoDBGraphConfigurations.HOST.get() + ".1", "localhost:8529");
 * settings.put(ArangoDBGraphConfigurations.USER.get(), "root");
 * settings.put(ArangoDBGraphConfigurations.PASSWORD.get(), "password");
 * settings.put(ArangoDBGraphConfigurations.GRAPH.get(), "my_graph");
 *
 * GraphConfiguration configuration = new ArangoDBGraphConfiguration();
 * Graph graph = configuration.apply(settings);
 * }
 * </pre>
 * All configurations implement {@link org.eclipse.jnosql.communication.Settings} for dynamic
 * property management and leverage Eclipse MicroProfile Config or Jakarta Config APIs for
 * externalized configuration.
 */
package org.eclipse.jnosql.mapping.tinkerpop.connections;