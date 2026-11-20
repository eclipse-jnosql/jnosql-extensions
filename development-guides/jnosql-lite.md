# JNoSQL Lite

`jnosql-lite` lets Eclipse JNoSQL run without its reflection engine. A set of annotation processors
generates, at compile time, the entity, repository and converter metadata that the reflection module
would build at startup. Applications keep the same Jakarta NoSQL / Jakarta Data APIs and gain CDI Lite
compatibility. The module README labels the implementation as experimental; see
[../jnosql-lite/README.adoc](../jnosql-lite/README.adoc) and the root [README.adoc](../README.adoc) for
setup (exclude `jnosql-mapping-reflection`, add `mapping-lite-processor`).

General processor and template rules: [annotation-processing.md](annotation-processing.md).

## Modules

| Module | Purpose |
|--------|---------|
| `mapping-lite-processor` | The processors, generation models, templates and the runtime `Lite*` support classes. |
| `mapping-lite-core-test` | Tests for generated entity, projection, inheritance, record and repository metadata. |
| `mapping-lite-document-test`, `-column-test`, `-key-value-test`, `-timeseries-test`, `-vector-test`, `-graph-test` | Compile the processor against each JNoSQL mapping module to test generated repositories per database model. |

## Processors

Registered in `META-INF/services/javax.annotation.processing.Processor`:

| Processor | Trigger | Generates |
|-----------|---------|-----------|
| `MappingProcessor` | `@Entity`, `@Embeddable`, `@MappedSuperclass`, `@Projection` | Per-type metadata, plus `LiteEntitiesMetadata` and the `META-INF/services` entries for `EntitiesMetadata`, `ClassConverter`, `ClassScanner`, `ConstructorBuilderSupplier` and `ProjectionBuilderSupplier`. |
| `RepositoryMetadataProcessor` | `@Repository` | Repository and method metadata aggregated in `LiteRepositoriesMetadata`. |
| `RepositoryProcessor` | `@Repository` | Concrete repository implementations, one per database model detected. |
| `AutoApplyConverterProcessor` | `@Converter` (auto-apply only) | `<Converter>AutoApplyMetadata` plus a service file. |
| `LifecycleEventTypesProcessor` | `@Entity` | `<Entity>LifecycleEventTypes`, so CDI can publish typed lifecycle events without runtime generic inspection. |

`MetadataAppender` also writes the hand-written runtime classes in
`org.eclipse.jnosql.lite.mapping.metadata` (`LiteEntityMetadata`, `LiteClassConverter`,
`RepositoryMethodLookup`, `JNoSQLRepositoryProcessor`, ...) into the user's compilation as source.
Those files are ordinary Java under `src/main/java/.../metadata/`; edit them there.

## Source generation vs. generated behavior

Keep two layers apart:

- **Source-generation logic** (processors, `*Introspector`, `*Analyzer`, `*Model`, `*SourceGenerator`)
  runs in the compiler and reads `Element`/`TypeMirror`.
- **Generated behavior** (the code in the templates and the `metadata` runtime classes) runs in the
  application. It must not use annotation-processing APIs.

Do not move runtime decisions into templates. The repository template delegates derived and annotated
queries to `JNoSQLRepositoryProcessor`, keyed by a generated `MethodSignatureKey` constant, instead of
unrolling query logic per method.

## Entity metadata pipeline

`MappingProcessor` → `MappingIntrospector` → `EntityMappingIntrospector` / `ProjectionMappingIntrospector`
→ `EntityModel`, `FieldModel`, `ConstructorMetadataModel`, `ProjectionModel`.
`EntitySourceGenerator` and `FieldSourceGenerator` render them.

Naming (from the model classes): `<Entity>EntityMetaData`, `<Entity><Field>FieldMetaData`,
`<Class>ConstructorMetadata`, `<Class>ProjectionMetadata`. `FieldSourceGenerator` picks one of four
templates: `field_metadata`, `field_collection_metadata`, `field_map_metadata`,
`field_array_metadata` (parameters have matching `parameter_*` templates). An entity needs an
accessible constructor, otherwise `MappingIntrospector` throws a `ValidationException`.

## Repository pipeline

- `RepositoryAnalyzer` builds a `RepositoryElement` for each `@Repository` and each detected database,
  then renders `SemiStructureRepositoryMetadata` or `KeyValueRepositoryMetadata`.
- `DatabaseSupport` detects the database models by checking for `DocumentTemplate`,
  `ColumnTemplate`, `KeyValueTemplate`, `GraphTemplate`, `TimeSeriesTemplate` or `VectorTemplate` on the
  processor classpath. No mapping module on the classpath means nothing is generated.
- Templates: `repository_semi_structure.mustache` (document, column, graph, time-series, vector) and
  `repository_key-value.mustache`.
- Generated class names: `<Repository>Lite<Provider>` (for example `PersonRepositoryLiteDocument`) and
  `<Repository>LiteKeyValue`. They are `@ApplicationScoped`, `@Default` and annotated with
  `@Database(DatabaseType.X)`.
- `RepositoryMetadataProcessor` uses `repository_metadata`, `repository_method_metadata`,
  `repository_method_params_metadata` and `repository_method_annotations_metadata`, with the
  `<Repository>RepositoryMetadata` and `<Class>RepositoryMethodLookup` names.
- `MethodGenerator` is public but returns an empty line list; it is a placeholder, not an extension
  point. Method bodies come from the template and `JNoSQLRepositoryProcessor`.

## Rules when changing Lite

- Keep generated names stable; runtime service files and user code refer to them.
- Keep one originating element per generated file.
- A new database model needs a `DatabaseSupport` constant, a template or `RepositoryTemplateType`
  entry, and a module of tests like the existing `mapping-lite-*-test` modules.
- Test negative cases (missing constructor, unsupported repository interface) in addition to metadata
  equality. Follow [TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc).
- Do not edit generated sources under `target/`.

## Asynchronous and nested return types

- `RepositoryMethod.returnType()` keeps the raw declared class (for example `CompletionStage`). The
  core default `returnDescriptor()` only wraps that raw class, so it loses nested type arguments.
- For parameterized return types, `repository_method_metadata` generates a `returnDescriptor()`
  override such as
  `ReturnType.of(CompletionStage.class, ReturnType.of(Optional.class, ReturnType.of(Person.class)))`.
  Other return types rely on the default method.
- `elementType()` is async-aware: `CompletionStage<Optional<P>>`, `CompletionStage<List<P>>` and
  `CompletableFuture<P>` all resolve to `P`.
- Execution reuses the core return handlers and executor; Lite adds no executor or reflection.
- Limitation: unlike the reflection-based mapping, Lite does not reject async return types on
  method kinds the core does not support asynchronously.
