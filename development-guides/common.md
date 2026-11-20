# Common Development Practices

Rules shared by every module. Module-specific behavior lives in the other guides linked from
[AGENTS.md](../AGENTS.md). Existing code and [CONTRIBUTING.adoc](../CONTRIBUTING.adoc) take precedence
when they disagree with this document.

## API design

- **Developer experience first.** Prefer small, semantic APIs that read well at the call site.
- **Keep the public surface small.** Do not add public abstractions "for later". Many classes in the
  processors and in the Jakarta Persistence driver are implementation details. Do not widen
  visibility, and do not treat a `public` modifier alone as an API promise.
- **Cohesive responsibilities.** A class should do one thing. Replace large `if`/`switch` chains with
  small strategies, enums with behavior, or templates, as the surrounding code already does
  (for example `AttributeElementType` in the static metamodel processor).
- **Respect Jakarta contracts.** Behavior defined by Jakarta Data, Jakarta NoSQL, Jakarta Persistence
  or Jakarta Validation must not be narrowed or reinterpreted. The TCK runners are the safety net.
- **Stay independent.** An extension must not depend on JNoSQL core internals beyond the published
  mapping contracts, and must not add dependencies to unrelated modules. Check the module `pom.xml`
  before adding an artifact.
- **Backward compatibility.** Public signatures, configuration keys, exception types, generated class
  names and generated behavior are contracts. Change them only deliberately.

## Java

- Use the Java level the module already targets. The processors declare
  `@SupportedSourceVersion(SourceVersion.RELEASE_21)`; the Jakarta Persistence scanner module sets
  `maven.compiler.source` to 17. Check the module before using newer language features.
- Prefer records, immutable fields and `List.of`/`Map.of` where the module level allows.
- Validate public arguments the way the code base does:
  `Objects.requireNonNull(value, "value is required")`.
- Prefer compile-time metadata (`TypeMirror`, `Element`) or existing JNoSQL metadata over reflection.
- Do not add framework infrastructure (new CDI extensions, service registries) unless required.

## Public API documentation

Document every developer-facing type: interfaces, classes, annotations and their elements, public
constructors and methods, extension points, and configuration contracts (property keys and defaults).

Public Javadoc should state:

- what the API represents and why a developer would use it;
- how to use it, with a short example when that helps;
- restrictions, thrown exceptions and null handling;
- behavior that is not obvious from the signature.

Do not restate the method name. Do not document private helpers to raise coverage. Use
`{@snippet :}` only when the module's toolchain supports it (Java 21 modules); otherwise use
`<pre>{@code ...}</pre>`.

```java
/**
 * Creates a {@link SqlTemplate} backed by the given entity manager.
 *
 * @param entityManager the provider-managed entity manager
 * @return a template that delegates to {@code entityManager}
 * @throws NullPointerException if {@code entityManager} is {@code null}
 */
public SqlTemplate create(EntityManager entityManager)
```

## Tests

[TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc) is authoritative for tooling (JUnit 5, AssertJ),
naming (`<Subject>Test`, `should...` methods) and `@DisplayName` usage. Do not introduce another
convention.

Cover:

- public contracts, with positive and negative scenarios and edge cases;
- backward compatibility of keys, names and generated types;
- compile-time validation for processors;
- generated source where applicable.

## Generated code

Generated Java is a deliverable. It must:

- use meaningful identifiers and consistent indentation;
- keep control flow simple and methods short;
- use imports instead of fully qualified names when no conflict exists;
- avoid redundant variables, conversions and casts;
- preserve type safety and compile without manual changes;
- be understandable when opened in an IDE.

Change the template or the generation model, not generated output. See
[annotation-processing.md](annotation-processing.md).
