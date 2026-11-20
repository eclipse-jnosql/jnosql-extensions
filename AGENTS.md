# AGENTS.md

Guidance for contributors and coding agents working in Eclipse JNoSQL Extensions.

## Purpose

This repository holds optional extensions for [Eclipse JNoSQL](https://github.com/eclipse-jnosql/jnosql)
and the Jakarta NoSQL / Jakarta Data specifications. Each extension is independent of the others and
must not leak into the JNoSQL core.

| Module | Role |
|--------|------|
| `jnosql-lite` | Annotation processor that replaces the reflection engine with generated metadata and repositories (CDI Lite friendly). |
| `jnosql-static-metamodel` | Annotation processor that generates Jakarta Data static metamodel interfaces (`_Entity`). |
| `jnosql-mapping-validation` | Jakarta Validation applied to entities during persistence events. |
| `jnosql-jakarta-persistence` | Jakarta Data / JNoSQL driver backed by a Jakarta Persistence provider. |
| `jnosql-tinkerpop-connections` | `GraphConfiguration` implementations for Neo4J, ArangoDB, JanusGraph and Titan. |
| `jnosql-data-tck-runner`, `jnosql-nosql-tck-runner` | Run the Jakarta Data and Jakarta NoSQL TCKs against JNoSQL. |

## Development guides

Read the guides that apply to your change before editing:

- [Common practices](development-guides/common.md) — API design, Java, Javadoc, tests, generated code.
- [Annotation processing](development-guides/annotation-processing.md) — JSR 269 and Mustache rules.
- [JNoSQL Lite](development-guides/jnosql-lite.md)
- [Static metamodel](development-guides/static-metamodel.md)
- [Mapping validation](development-guides/mapping-validation.md)
- [Jakarta Persistence](development-guides/jakarta-persistence.md)
- [TinkerPop connections](development-guides/tinkerpop-connections.md)

## Rules

1. **Existing conventions win.** Match the surrounding code, [CONTRIBUTING.adoc](CONTRIBUTING.adoc) and the
   module's existing patterns before applying anything from the guides.
2. **Testing:** [TESTING-GUIDELINE.adoc](TESTING-GUIDELINE.adoc) is authoritative for naming and style.
3. **Backward compatibility is required.** Do not change public signatures, defaults, exceptions,
   generated class names or generated behavior without an explicit decision.
4. **Public APIs need meaningful Javadoc** that explains purpose, usage, restrictions and errors.
5. **Generated Java must be readable.** Treat it as a deliverable. Change the template or generation
   model, never the generated output.

## Git workflow (mandatory)

- Work on a feature branch created from the current development branch, never directly on it.
- Keep the configured human author; never add `Co-authored-by` lines for AI agents.
- Use [Conventional Commits](https://www.conventionalcommits.org/) (`docs:`, `fix:`, `feat:`, ...).
- Sign off every commit (`git commit -s`) to satisfy the DCO / Eclipse ECA requirements.
- **One changed file per commit.** Do not combine files, and commit a Mustache template separately
  from the Java processor that uses it.
- Do not rewrite unrelated history or touch unrelated files. Do not push or merge unless asked.

```bash
git add path/to/File.java
git commit -s -m "docs: improve File javadocs"
```
