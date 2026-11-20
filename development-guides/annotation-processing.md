# Annotation Processing and Mustache Templates

The processors in `jnosql-lite/mapping-lite-processor` and
`jnosql-static-metamodel/jnosql-metamodel-processor` use Java Annotation Processing (JSR 269) and
[mustache.java](https://github.com/spullara/mustache.java) to generate Java source at compile time.
Processors are registered through `META-INF/services/javax.annotation.processing.Processor`.

For the concrete pipelines, see [jnosql-lite.md](jnosql-lite.md) and
[static-metamodel.md](static-metamodel.md). General rules are in [common.md](common.md).

## Processor responsibilities

A processor extends `AbstractProcessor` and declares what it handles:

```java
@SupportedAnnotationTypes("jakarta.nosql.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class EntityProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // analyze -> build model -> render
        return false;
    }
}
```

- Keep `process` thin: select elements, delegate analysis, delegate rendering.
- Return `false` so other processors can still see the same annotations.
- Never depend on the order in which processors run. If output must be consumed by another processor
  (for example MapStruct), rely on standard rounds and verify with a compilation test.
- A generated type is not available in the round that creates it. Do not look it up with
  `Elements.getTypeElement` in the same round.

## Analyzing source

Use the language model APIs, not reflection:

| API | Use |
|-----|-----|
| `TypeElement` | Classes, interfaces, records: names, package, members, supertypes. |
| `ExecutableElement` | Methods and constructors: parameters, return type, annotations. |
| `TypeMirror` / `DeclaredType` | Types, including generics. Use `getTypeArguments()` instead of parsing `toString()`. |
| `Elements`, `Types` (from `processingEnv`) | Member lookup, assignability (`Types.isAssignable`), erasure. |

The static metamodel processor classifies attributes with `Types.isAssignable` against
`CharSequence`, `Number` and `Comparable`; follow that pattern instead of string comparison where a
type check is possible. Resolve optional dependencies with `Elements.getTypeElement(name) != null`,
as `DatabaseSupport` does to detect which JNoSQL template classes are on the classpath.

Unsupported declarations (unknown generic shapes, missing constructors, repositories that do not
extend a supported interface) must fail clearly, never be skipped silently. See
[Diagnostics](#diagnostics).

## Writing output

- Create sources through `processingEnv.getFiler().createSourceFile(qualifiedName, originatingElement)`.
  Passing the originating element lets build tools track incremental compilation.
- Create resources (`META-INF/services/...`) with `createResource(StandardLocation.CLASS_OUTPUT, ...)`.
- Each qualified name may be created once per compilation. Deduplicate inputs (`MappingProcessor`,
  `RepositoryMetadataProcessor` and the metamodel `EntityProcessor` keep a `references` list) and keep names unique and derived from the source type.
- Output must be deterministic: same input, same source. Iterate ordered collections, and keep
  volatile values (timestamps) out of generated code where possible. The current templates render
  `{{now}}` in `@Generated(date = ...)`; this is a known source of non-reproducible output.
- Declare accurate `@SupportedAnnotationTypes`. Processors that generate from a single originating
  element per file are friendlier to incremental builds than ones that aggregate everything.

## Diagnostics

Report problems through `processingEnv.getMessager()`:

```java
processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
        "Repository " + repository + ": method " + method + " is not supported because ... Change it to ...",
        method);
```

A good message names the annotated type, the method or field, why generation is impossible, and what
to change. Pass the `Element` so the compiler points at the source line.

Be aware that parts of the existing code throw `ValidationException`/`ProcessorValidationException`
or report only `failed to write extension file: ...`. Prefer `Messager` for new code and do not
swallow exceptions.

## Mustache templates

Generation has three separate responsibilities:

```
Java source -> processor (analysis) -> generation model -> Mustache template -> readable Java
```

1. **Analysis** uses JSR 269 types.
2. **The generation model** is a plain Java object (`EntityModel`, `FieldModel`,
   `RepositoryMetaModel`) holding ready-to-print strings and booleans. Do not pass `Element` or
   `TypeMirror` into a template.
3. **The template** renders the model as valid, readable Java.

Templates live in each processor's `src/main/resources`. Rules:

- Prefer simple variables (`{{className}}`), named sections (`{{#fields}}`) and small conditionals
  (`{{#element.isCrudRepository}}`).
- Use triple braces (`{{{returnType}}}`) for Java types containing `<` or `>` so they are not
  HTML-escaped.
- Keep each generated method in its own block, separated by a blank line, with consistent indentation.
- Do not encode business rules in templates. Compute booleans and strings in the model
  (see `hasReturn`, `parametersSignature`, `constantName`).
- Avoid deep section nesting and long branches. If a template needs many variants, use separate
  templates selected in Java, as `FieldSourceGenerator` does for default, collection, map and
  array fields.
- Do not duplicate logic across templates; share it in the model.
- Do not build whole classes with string concatenation when a template is clearer. Short expression
  fragments inside a model (for example `AttributeElementType.newInstance`) are acceptable.

A change to generated Java starts with the template or the model, never with edited output.

## Checklist for a new generator

1. Declare the annotation types and register the processor in `META-INF/services`.
2. Analyze with `Element`/`TypeMirror`; reject unsupported input with a diagnostic.
3. Build a small model class with deterministic, readable values.
4. Add or reuse a template; render through `Filer` with the originating element.
5. Add compile-and-run tests plus negative tests, following
   [TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc).
