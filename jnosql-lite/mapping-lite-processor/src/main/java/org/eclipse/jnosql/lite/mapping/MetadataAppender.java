/*
 *  Copyright (c) 2020 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping;

import jakarta.data.exceptions.MappingException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MetadataAppender {

    private static final Logger LOGGER = Logger.getLogger(MetadataAppender.class.getName());
    private static final String PACKAGE = "org.eclipse.jnosql.lite.mapping.metadata.";
    private static final String METADATA = "metadata";
    private final ProcessingEnvironment processingEnv;

    private MetadataAppender(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    void append() throws IOException, URISyntaxException {
        URL url = MappingProcessor.class.getClassLoader().getResource(METADATA);
        Objects.requireNonNull(url, "Could not load resources from metadata folder");
        LOGGER.info("URL folder: " + url);
        LOGGER.info("URI folder: " + url.toURI());
        try(Stream<Path> path = Files.walk(getPath(url))){
            path.map(Path::getFileName)
                    .map(Path::toString)
                    .filter(s -> s.contains(".java"))
                    .map(s -> s.substring(0, s.lastIndexOf(".")))
                    .forEach(this::loadClass);
        }
    }

    private Path getPath(URL url) throws IOException, URISyntaxException {
        URI uri = url.toURI();
        if ("jar".equals(uri.getScheme())) {
            FileSystem fileSystem = getFileSystem(uri);
            return fileSystem.getPath("/" + METADATA);
        } else {
            return Paths.get(uri);
        }
    }

    private FileSystem getFileSystem(URI uri) throws IOException {
        try {
            LOGGER.info("Loading File system from " + uri);
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException exception) {
            LOGGER.info("File system does not exist, creating a new one: " + uri);
            return FileSystems.newFileSystem(uri, Collections.emptyMap(), null);
        }
    }

    private void loadClass(String file) {
        try {
            Filer filer = processingEnv.getFiler();
            JavaFileObject fileObject = filer.createSourceFile(PACKAGE + file);
            try (Writer writer = fileObject.openWriter()) {
                final InputStream stream = MetadataAppender.class
                        .getClassLoader()
                        .getResourceAsStream(METADATA + "/" + file + ".java");
                String source = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));
                writer.append(source);
            }
        } catch (IOException exp) {
            throw new MappingException("There is an issue while it is loading the class: " + file, exp);
        }
    }

    public static void append(ProcessingEnvironment processingEnv) throws IOException, URISyntaxException {
        MetadataAppender appender = new MetadataAppender(processingEnv);
        appender.append();
    }
}
