# Jakarta Persistence Extension

`jnosql-jakarta-persistence` lets Jakarta Data repositories and Jakarta NoSQL-style templates run on
top of a Jakarta Persistence `EntityManager`, so relational databases are reached through the same
programming model as the NoSQL mappings. The artifact is
`org.eclipse.jnosql.mapping:jnosql-jakarta-persistence`.

## Modules

| Module | Purpose |
|--------|---------|
| `jnosql-jakarta-persistence-driver` | The extension itself: CDI extension, templates, repository proxies, query parsing, interceptors. |
| `jnosql-jakarta-persistence-scanner` | `JakartaPersistenceClassScanner` implementation that finds supported repositories (`PersistenceRepositoryFilter` accepts repositories whose entity is `jakarta.persistence.Entity` or `jakarta.nosql.Entity`). |
| `jnosql-jakarta-persistence-data-tck-runner` | Runs the Jakarta Data TCK against EclipseLink and Derby. |
| `jnosql-jakarta-persistence-glassfish-test` | GlassFish integration test, in an optional profile. |

## Integration boundaries

- The application owns persistence configuration (`persistence.xml`) and produces the `EntityManager`
  as a CDI bean. The extension never creates the `EntityManagerFactory` itself.
- `JakartaPersistenceExtension` (registered in
  `META-INF/services/jakarta.enterprise.inject.spi.Extension`) discovers repositories through the
  scanner SPI and registers repository beans after bean discovery. Failures are reported as CDI
  deployment problems.
- The extension depends on `jnosql-mapping-document`/`semistructured` and the reflection mapping; it
  does not use JNoSQL Lite.

## Public extension points

- `SqlTemplate` (`org.eclipse.jnosql.extensions.sql`): a `SemiStructuredTemplate` that also exposes
  the `EntityManager` and `PersistenceUnitUtil`. Obtain it with `SqlTemplate.of(entityManager)` or
  inject `SqlTemplateFactory.create(entityManager)`. `DefaultSqlTemplate` is package-private.
- `MethodInterceptor` (`org.eclipse.jnosql.jakartapersistence.mapping.spi`): provide an alternative
  CDI bean qualified `@MethodInterceptor.Repository` to replace the default transactional
  interception of repository methods.
- `@EnsureTransaction`: interceptor binding implemented by `EnsureTransactionInterceptor`.
- `EntityManagerProvider`: protected `produce*` hooks resolve an `EntityManager` by qualifiers, then
  by persistence unit name, then the default. An explicit request never falls back to the default.
- `PersistenceUnitCacheProvider` / `PersistenceUnitCache`: per-persistence-unit cache, keyed by
  `EntityManagerFactory.getName()`.
- `JNoSQLJakartaPersistence.PROVIDER`: the provider name `Eclipse_JNoSQL_Jakarta_Persistence`.

Other public classes (`PersistenceDocumentTemplate`, `PersistenceDatabaseManager`, `Sql*Operation`,
`SelectQueryParser`, ...) are implementation details even though they are public. Do not build on
them or document them as API.

## Provider limitations

- Queries are translated to JPQL and executed by the configured provider; behavior follows that
  provider and database (the TCK uses EclipseLink + Derby).
- Features specific to NoSQL models (key-value, graph, time series, vector) are not provided.
- Transactions are managed by the interceptor or by the surrounding Jakarta Transactions context;
  keep that distinction when changing interception.

## Testing

- Driver unit tests live in `jnosql-jakarta-persistence-driver/src/test` (for example
  `SqlTemplateFactoryTest`, `DefaultSqlTemplateTest`, `CustomRepositoryInterceptorTest`) and use Weld
  SE with Derby.
- Run the Jakarta Data TCK with `mvn clean test -B` in
  `jnosql-jakarta-persistence/jnosql-jakarta-persistence-data-tck-runner`.
- Test both the default interceptor and a replacement `MethodInterceptor` for any change to
  repository interception. Follow [TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc).
