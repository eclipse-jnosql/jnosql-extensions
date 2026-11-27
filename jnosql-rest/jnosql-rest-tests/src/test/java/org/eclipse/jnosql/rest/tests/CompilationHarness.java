/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest.tests;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Compiles source snippets with the REST processor, and optionally MapStruct, using the platform compiler.
 */
final class CompilationHarness {

    private static final String REST_PROCESSOR = "org.eclipse.jnosql.rest.processor.RestProcessor";
    private static final String MAPSTRUCT_PROCESSOR = "org.mapstruct.ap.MappingProcessor";

    private final Path directory;
    private boolean openApi = true;
    private boolean security = true;
    private boolean mapStruct = true;
    private boolean runMapStructProcessor = true;
    private boolean runRestProcessor = true;
    private final Map<String, String> sources = new java.util.LinkedHashMap<>();

    CompilationHarness(Path directory) {
        this.directory = directory;
    }

    CompilationHarness withoutOpenApi() {
        this.openApi = false;
        return this;
    }

    CompilationHarness withoutSecurity() {
        this.security = false;
        return this;
    }

    CompilationHarness withoutMapStruct() {
        this.mapStruct = false;
        this.runMapStructProcessor = false;
        return this;
    }

    CompilationHarness withoutRestProcessor() {
        this.runRestProcessor = false;
        return this;
    }

    CompilationHarness withoutMapStructProcessor() {
        this.runMapStructProcessor = false;
        return this;
    }

    CompilationHarness source(String qualifiedName, String content) {
        sources.put(qualifiedName, content);
        return this;
    }

    Result compile() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null,
                StandardCharsets.UTF_8)) {
            Path generated = Files.createDirectories(directory.resolve("generated"));
            Path classes = Files.createDirectories(directory.resolve("classes"));
            files.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(generated.toFile()));
            files.setLocation(StandardLocation.CLASS_OUTPUT, List.of(classes.toFile()));
            List<JavaFileObject> units = new ArrayList<>();
            sources.forEach((name, content) -> units.add(new StringSource(name, content)));
            List<String> processors = new ArrayList<>();
            if (runRestProcessor) {
                processors.add(REST_PROCESSOR);
            }
            if (runMapStructProcessor) {
                processors.add(MAPSTRUCT_PROCESSOR);
            }
            List<String> options = List.of("-classpath", classpath(), "-processor", String.join(",", processors),
                    "-proc:full", "-Xlint:-processing");
            boolean success = compiler.getTask(null, files, diagnostics, options, null, units).call();
            return new Result(success, diagnostics.getDiagnostics(), generated);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String classpath() {
        return Stream.of(System.getProperty("java.class.path").split(java.io.File.pathSeparator))
                .filter(entry -> openApi || !entry.contains("microprofile-openapi-api"))
                .filter(entry -> security || !entry.contains("jakarta.annotation-api"))
                .filter(entry -> mapStruct || !entry.contains("mapstruct"))
                .collect(Collectors.joining(java.io.File.pathSeparator));
    }

    record Result(boolean success, List<Diagnostic<? extends JavaFileObject>> diagnostics, Path generated) {

        Path classes() {
            return generated.resolveSibling("classes");
        }

        List<String> errors() {
            return messages(Diagnostic.Kind.ERROR);
        }

        List<String> warnings() {
            return messages(Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING);
        }

        List<String> notes() {
            return messages(Diagnostic.Kind.NOTE);
        }

        private List<String> messages(Diagnostic.Kind... kinds) {
            List<Diagnostic.Kind> accepted = List.of(kinds);
            return diagnostics.stream().filter(diagnostic -> accepted.contains(diagnostic.getKind()))
                    .map(diagnostic -> diagnostic.getMessage(null)).toList();
        }

        boolean generated(String simpleName) {
            return Files.exists(file(simpleName));
        }

        String source(String simpleName) {
            try {
                return Files.readString(file(simpleName));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }

        private Path file(String simpleName) {
            try (Stream<Path> paths = Files.walk(generated)) {
                return paths.filter(path -> path.getFileName().toString().equals(simpleName + ".java"))
                        .findFirst().orElse(generated.resolve("missing/" + simpleName + ".java"));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    private static final class StringSource extends javax.tools.SimpleJavaFileObject {

        private final String content;

        private StringSource(String qualifiedName, String content) {
            super(URI.create("string:///" + qualifiedName.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
}
