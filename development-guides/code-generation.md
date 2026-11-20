# Code Generation Pipeline

This guide follows a generated class through the two code generators of this repository:
JNoSQL Lite (`jnosql-lite/mapping-lite-processor`) and the static metamodel
(`jnosql-static-metamodel/jnosql-metamodel-processor`). It complements
[annotation-processing.md](annotation-processing.md), which holds the general JSR 269 and Mustache
rules, and [common.md](common.md), which holds the generated-code quality bar.

```
Java source -> Annotation processor -> Generation model -> Mustache template -> Java source
   analysis          Elements/Types        *Model classes       *.mustache       (target/generated-sources)
```

## 1. Discovery

Processors declare what they handle with `@SupportedAnnotationTypes` and are listed in
`META-INF/services/javax.annotation.processing.Processor`.

| Processor | Handles |
|-----------|---------|
| `MappingProcessor` | `@Entity`, `@Embeddable`, `@MappedSuperclass`, `@Projection` |
| `RepositoryProcessor`, `RepositoryMetadataProcessor` | `@Repository` |
| `AutoApplyConverterProcessor` | auto-apply `@Converter` types |
| `LifecycleEventTypesProcessor` | entities, for lifecycle event types |
| `EntityProcessor` (metamodel) | `@Entity`, `@MappedSuperclass` |

Each processor gets its elements from `RoundEnvironment.getElementsAnnotatedWith`. Files that are
produced for a whole compilation (`LiteEntitiesMetadata`, `LiteRepositoriesMetadata` and the service
files) are written once from the elements collected in the round.

## 2. Metadata and generation models

Analysis classes read `Element`/`TypeMirror` and produce plain models. Examples:

- Lite entities: `MappingIntrospector` → `EntityModel`, `FieldModel`, `ConstructorMetadataModel`.
- Lite repositories: `RepositoryAnalyzer` → `RepositoryElement`, `RepositoryMetadata`;
  `RepositoryIntrospector` and `RepositoryMethodIntrospector` → the `repository/metadata` models.
- Metamodel: `ClassAnalyzer` and `FieldAnalyzer` → `EntityModel`, `FieldModel`, using
  `AttributeElementType` to select the Jakarta Data attribute type.

Models expose strings, booleans and lists; template code never calls `Types` or `Elements`.

## 3. Rendering

`*SourceGenerator` classes (for example `EntitySourceGenerator`, `FieldSourceGenerator`,
`RepositoryMethodSourceGenerator`) and `BaseMappingModel` subclasses render a model with a Mustache
template from `src/main/resources`. Choose the template in Java (`RepositoryTemplateType`,
`FieldSourceGenerator`'s four field templates) and keep the template focused on layout. The template
set is:

- Entities: `entities_metadata`, `entity_metadata`, `constructor_metadata`, `projection_metadata`,
  `field_metadata`, `field_collection_metadata`, `field_map_metadata`, `field_array_metadata`,
  `parameter_*`.
- Repositories: `repository_semi_structure`, `repository_key-value`, `repositories_metadata`,
  `repository_metadata`, `repository_method_metadata`, `repository_method_params_metadata`.
- Metamodel: `metamodel.mustache`.

## 4. Naming

Names come from the model, not from counters:

| Generated type | Name |
|----------------|------|
| Entity metadata | `<Entity>EntityMetaData` |
| Field metadata | `<Entity><Field>FieldMetaData` |
| Repository implementation | `<Repository>LiteDocument`, `...LiteColumn`, `...LiteGraph`, `...LiteTimeSeries`, `...LiteVector`, `<Repository>LiteKeyValue` |
| Repository metadata | `<Type>RepositoryMetadata`, `<Class>RepositoryMethodLookup` |
| Static metamodel | `_<Entity>` |

The generated name is derived from the originating type's name and package, so two runs produce the same
name. Generating the same name twice makes `Filer` fail the compilation with a duplicate file error; new
generators must derive their names from the element so this is not possible for distinct inputs.

## 5. CDI Lite integration

Generated repositories are ordinary beans: `@ApplicationScoped`, `@Default`, `@Database(DatabaseType.X)`,
an `@Inject` constructor receiving the template, `RepositoryOperationProvider` and
`LifecycleEventHandler`, and a no-argument constructor for proxying. Metadata is looked up through
`LiteEntitiesMetadata.INSTANCE` and `LiteRepositoriesMetadata.INSTANCE`, with SPI registrations written
under `META-INF/services` instead of using reflection scanning. A generator that adds beans must follow
the same shape and must not require a CDI portable extension.

## 6. Inspecting and debugging generated Java

- Maven writes sources to `target/generated-sources/annotations/` of the module that is compiled with the
  processor, for example
  `jnosql-lite/mapping-lite-document-test/target/generated-sources/annotations/.../PersonRepositoryLiteDocument.java`
  and `jnosql-static-metamodel/jnosql-metamodel-sample/target/generated-sources/annotations/.../_Person.java`.
- Open the file in an IDE; it is regular Java and can be stepped through with the debugger.
- Always change the template or model, then rebuild (`mvn clean compile -pl <module> -am`). Do not edit
  files under `target/`.
- To debug the processor itself, run Maven with `-Dmaven.compiler.fork=true` and attach a debugger
  through `MAVEN_OPTS`, or run the compiler in-process from a test.

## 7. Testing generated code

Follow [TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc). Existing coverage:

- `jnosql-lite/mapping-lite-*-test` modules compile entities and repositories against each database model
  and run the generated beans in CDI; this proves that the output compiles and behaves.
- `mapping-lite-processor/src/test` covers runtime support classes (`LiteClassScannerTest`,
  `JNoSQLRepositoryProcessorTest`, `DatabaseSupportTest`, ...).
- `jnosql-metamodel-sample` tests assert the generated constants and attribute types.

When adding a generator, also add:

- a valid case per supported shape (collections, arrays, nested types, generics);
- an invalid case per rejected declaration, asserting the compiler diagnostic and its element;
- a comparison with expected source for at least one representative class, so formatting regressions
  fail the build;
- a double-run comparison showing the output does not vary.

The processor modules currently have no `compile-testing` style tests; the invalid-declaration cases
above are a gap.

## 8. Compile-time errors

Prefer `processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element)` to throwing.
A message states the annotated type, the unsupported member, why it cannot be generated and what to
change, for example: `Person: the entity has no public or package-private constructor; add one or
annotate a constructor with @Inject`.

Current behavior to be aware of: write failures are reported through `Messager`
(`"failed to write extension file: ..."`) without an `Element`, while analysis failures are thrown as
`ValidationException`, `ProcessorValidationException` or `MappingException`
(for example, `MappingIntrospector` for an entity without an accessible constructor). Do not swallow
errors; when touching these paths, move toward element-aware `Messager` errors without changing what
is accepted.

## 9. Readability review of current output

Observed in generated sources, as follow-up work (no behavior change made):

| Finding | Where |
|---------|-------|
| `@Generated(date = ...)` and license-header year use the current time, so each build produces different source. | `{{now}}`, `{{currentYear}}` in the templates, set by `BaseMappingModel`, `RepositoryMetadata`, `AutoApplyConverterModel`, `LifecycleEventTypesModel`. |
| Fully qualified names for `ApplicationScoped`, `Default`, `Inject`, `Collections` and for types in `MethodSignatureKey` lists, although imports exist. | `repository_semi_structure.mustache`, `repository_key-value.mustache`, signature models. |
| `Database` and `DatabaseType` imports follow the `java.util` group, so imports are not grouped. | `repository_semi_structure.mustache` |
| Constant continuation lines are not indented and constant names such as `FINDBYNAMEORDERBYID_NAME` lose word boundaries. | method key constants in the repository templates |
| Typo `is is required` in the `deleteById` message. | `repository_semi_structure.mustache` |
| Metamodel uses wider indentation, trailing-whitespace blank lines and `//CHECKSTYLE:OFF/ON` markers. | `metamodel.mustache` |
| `MethodGenerator.getLines()` returns an empty list; `AnnotationQueryRepositoryReturnType`, `MethodQueryRepositoryReturnType`, `AnnotationOperationMethodBuilder` and `KeyValueMethodBuilder` build code by string concatenation and are not referenced from templates. | `repository` package |

Each fix should be a single-file change to the template or model, with the generated output compared
before and after.

## 10. Rules for future generators

1. Separate analysis, model and template as above; models are the only input to a template.
2. Derive names from the element; make them deterministic and collision-free.
3. Use `Filer` with the originating element, and only use the standard processing rounds.
4. Report unsupported input with `Messager` and an `Element`; never skip silently.
5. Emit imports instead of fully qualified names, sorted consistently.
6. Keep generated methods short and delegate logic to runtime classes, as the repository template does
   with `JNoSQLRepositoryProcessor`.
7. Exclude timestamps from output that is compared or cached.
8. Add valid, invalid, readability and compilation tests before merging.
