# Static Metamodel

`jnosql-static-metamodel` generates Jakarta Data static metamodel interfaces for Jakarta NoSQL
entities, so queries, sorts and restrictions can reference attributes with compile-time checked
constants instead of strings.

| Module | Purpose |
|--------|---------|
| `jnosql-metamodel-processor` | The annotation processor (`EntityProcessor`) and `metamodel.mustache`. |
| `jnosql-metamodel-sample` | Sample entities compiled with the processor (`provided` scope) and tests for the generated types. |

General processor and template rules: [annotation-processing.md](annotation-processing.md).

## Processing

`EntityProcessor` handles `@Entity` and `@MappedSuperclass`. For each type, `ClassAnalyzer` collects
the fields and `FieldAnalyzer` converts them to `FieldModel`s. `metamodel.mustache` renders an
`EntityModel`.

Only fields annotated with `@Column` or `@Id` are included (`EntityProcessor.HAS_ANNOTATION`).
Inherited members are read through `Elements.getAllMembers`.

## Generated structure

For `Person` in package `org.example`, the processor creates `org.example._Person`:

```java
@StaticMetamodel(Person.class)
@Generated(value = "The StaticMetamodel of the class Person provider by Eclipse JNoSQL", date = "...")
public interface _Person {

    String ID = "_id";
    String NAME = "name";

    TextAttribute<Person> id = TextAttribute.of(Person.class, ID);
    TextAttribute<Person> name = TextAttribute.of(Person.class, NAME);
}
```

(The real output is produced by `metamodel.mustache` and is formatted slightly differently.)

### Naming

- Type: `_` + entity simple name, same package as the entity.
- String constant: field name in upper case (`NAME`); the value is the mapped name from `@Column` /
  `@Id`, or the field name when blank.
- Attribute: the field name (`name`).
- Embedded fields are prefixed with the parent field name (`address_street` / `ADDRESS_STREET`);
  grouping embeddables and nested entities also prefix the mapped name with the parent
  (`address.street`), flat embeddables keep their own name.
- Fields of an `@Embeddable` or `@Entity` type produce a `NavigableAttribute` for the parent plus the
  nested attributes. Collections or arrays of entities/embeddables are expanded the same way.

### Attribute types

`AttributeElementType.of` chooses the Jakarta Data attribute type from the field type:

| Field type | Attribute |
|------------|-----------|
| `CharSequence` (`String`) | `TextAttribute` |
| numeric primitives (except `char`), `Number` subclasses | `NumericAttribute` |
| `boolean`, `Boolean`, other `Comparable` types | `ComparableAttribute` |
| `byte[]` | `SortableAttribute` |
| `LocalDateTime`, `LocalDate`, `LocalTime`, `Instant`, `Year`, `YearMonth` | `TemporalAttribute` |
| embeddable/entity | `NavigableAttribute` |
| anything else (collections, maps, `char`, ...) | `BasicAttribute` |

`BasicPrimitiveNumber` maps primitives to wrappers so generics are valid (`NumericAttribute<Car, Integer>`).
Collection and map attributes use an unchecked cast to `Class<T>`; this is an accepted limitation of
`BasicAttribute.of`.

## Compatibility with Jakarta Data

Generated interfaces rely only on `jakarta.data.metamodel` types and `@StaticMetamodel`. Keep the
`_` prefix and package; Jakarta Data implementations and user code discover metamodels by that
convention. Do not add runtime dependencies to the generated code.

## Readability notes

The template wraps output in `//CHECKSTYLE:OFF` / `//CHECKSTYLE:ON`, and its indentation of members
is wider than usual Java. Changes to improve it belong in `metamodel.mustache`. Keep the generated
`@Generated(date = ...)` in mind when comparing output (it makes builds non-reproducible).

## Tests

The sample module compiles real entities (`Person`, `Car`, `SoccerTeam`, `PrimitiveNumber`,
`WrapperNumber`, ...) and its tests assert the generated constants and attribute types. Add a sample
entity and a test for every new attribute shape. See
[TESTING-GUIDELINE.adoc](../TESTING-GUIDELINE.adoc).
