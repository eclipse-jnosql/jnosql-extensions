# TinkerPop Connections

`jnosql-tinkerpop-connections` supplies `GraphConfiguration` implementations that open Apache
TinkerPop `Graph` instances for specific graph databases. They plug into `jnosql-tinkerpop`
(the JNoSQL graph mapping); this module adds no mapping or query behavior of its own.

Package: `org.eclipse.jnosql.mapping.tinkerpop.connections`.

## Configurations

| Class | Backend | Settings |
|-------|---------|----------|
| `Neo4JGraphConfiguration` | Neo4J over Bolt (`neo4j-gremlin-bolt`) | `Neo4JGraphConfigurations`: `jnosql.neo4j.host` (default `bolt://localhost:7687`), `jnosql.neo4j.user` and `jnosql.neo4j.password` (default `neo4j`) |
| `Neo4JEmbeddedGraphConfiguration` | Embedded Neo4J (`Neo4jGraph.open`) | all string settings are copied; `jnosql.neo4j.host` is mapped to `gremlin.neo4j.directory` |
| `ArangoDBGraphConfiguration` | ArangoDB TinkerPop provider | `ArangoDBGraphConfigurations`: `jnosql.arangodb.graph.{edge,relationship,vertex,graph,host,user,password}` |
| `JanusGraphConfiguration` | JanusGraph (`JanusGraphFactory.open`) | all string settings are copied into a commons `BaseConfiguration` |
| `TitanGraphConfiguration` | Titan (`TitanFactory.open`) | same as JanusGraph, via `TitanFactory.open` |

Each class implements `GraphConfiguration.apply(Settings)` and returns a new `Graph`.

## Contracts to preserve

- Setting keys and their defaults are public contract; do not rename them.
- The ArangoDB `relationship` value uses `source|edge|target`; any other shape raises
  `IllegalArgumentException`.
- The caller owns the returned `Graph` and must close it. Configurations do not cache or share graphs.
- Provider dependencies stay specific to this module. Keep them out of other modules.

## Provider limitations

- Titan is unmaintained and kept for compatibility only.
- Embedded Neo4J and Neo4J over Bolt have different transaction and feature support; do not assume
  graph features are identical across providers.
- Janus and Titan accept any provider property, so their validation is provider-defined.

## Known documentation discrepancy

The `Neo4JEmbeddedGraphConfiguration` Javadoc example sets `gremlin.neo4j.directory`, while the code
reads the directory from `jnosql.neo4j.host` and writes `gremlin.neo4j.directory` from it. Behavior
was not changed; reconcile the Javadoc and code in a separate, deliberate change.

## Testing

The module has no tests, only a `beans.xml`. Real graph databases are needed for meaningful
integration tests; unit tests can cover setting-to-property mapping (such as the ArangoDB relationship
parsing and Neo4J defaults) without a server. See [TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc).
