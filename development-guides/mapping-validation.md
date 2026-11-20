# Mapping Validation

`jnosql-mapping-validation` applies [Jakarta Validation](https://jakarta.ee/specifications/bean-validation/)
to Jakarta NoSQL entities. Constraint annotations such as `@NotNull` or `@Size` on an entity are
checked automatically when JNoSQL persists it.

## Components

| Type | Visibility | Role |
|------|------------|------|
| `MappingValidator` | public, `@ApplicationScoped` | Validates an entity or a constructor invocation and throws `ConstraintViolationException`. |
| `EntityObserver` | package-private, `@ApplicationScoped` | CDI observer that calls `MappingValidator` for `EntityPrePersist`, `EntityPostPersist` and `ConstructorEvent`. |

The module ships a `beans.xml` with `bean-discovery-mode="annotated"`. Adding the jar to the classpath
is the only setup; there is no configuration key.

## Behavior

- `validate(Object)` rejects `null` with `NullPointerException("entity is required")` and throws
  `ConstraintViolationException` when `Validator.validate` returns violations.
- `validate(ConstructorEvent)` validates constructor parameters through `ExecutableValidator`; the
  `ConstructorEvent` type comes from `org.eclipse.jnosql.mapping.reflection` in JNoSQL core. This
  repository contains no other producer of that event.
- The `Validator` is resolved once in `@PostConstruct`, in this order:
  1. a CDI `Validator` bean;
  2. a CDI `ValidatorFactory` bean;
  3. `Validation.buildDefaultValidatorFactory()`.
- Validation happens both before and after persisting (`EntityPrePersist`, `EntityPostPersist`).

## Boundaries and rules

- Dependencies on Jakarta Validation and `jnosql-mapping-core` are `provided`; do not bundle them.
- Keep the module free of database-specific code. It must work with any JNoSQL mapping module.
- Do not catch or translate `ConstraintViolationException`; applications rely on the standard type.
- Preserve the resolution order above; changing it is a behavior change.
- Do not widen `EntityObserver` or add new observed events without an explicit decision.

## Testing

`src/test` currently contains only a `beans.xml`; the test dependencies (Hibernate Validator,
Expressly, and the column, document and key-value mapping modules) are declared in the POM.
New tests should boot CDI with those, cover valid and invalid entities, the `null` argument, and each
`Validator` resolution path, and follow [TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc).
