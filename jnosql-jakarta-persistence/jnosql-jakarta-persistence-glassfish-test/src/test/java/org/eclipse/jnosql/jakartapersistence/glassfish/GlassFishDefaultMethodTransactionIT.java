/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.jakartapersistence.glassfish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Reproduces https://github.com/eclipse-jnosql/jnosql/issues/809 on a real GlassFish: a caller-managed
 * {@code UserTransaction} must complete after repository default methods ran inside it.
 */
class GlassFishDefaultMethodTransactionIT {

    private static final String DOMAIN = "jnosql809";
    private static final String APP = "jnosql-default-method-tx";
    private static final int HTTP_PORT = 10080;
    private static final int ADMIN_PORT = 10048;

    private static Path glassfishHome;
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @BeforeAll
    static void startGlassFish() throws Exception {
        glassfishHome = Path.of(System.getProperty("glassfish.home")).toAbsolutePath();
        // a leftover domain from a previous run must be stopped before its directory is replaced
        asadminLenient("stop-domain", DOMAIN);
        if (Boolean.parseBoolean(System.getProperty("replace.driver", "true"))) {
            replaceEmbeddedDriver(Path.of(System.getProperty("driver.jar")));
        }
        asadmin("delete-domain", DOMAIN);
        asadmin("create-domain", "--nopassword=true", "--portbase", "10000", DOMAIN);
        // portbase 10000 => http 10080, admin 10048
        asadmin("start-domain", DOMAIN);
        asadmin("start-database");
        asadmin("--port", String.valueOf(ADMIN_PORT), "deploy", "--force=true", "--name", APP,
                "--contextroot", APP, Path.of(System.getProperty("war.file")).toAbsolutePath().toString());
    }

    @AfterAll
    static void stopGlassFish() throws Exception {
        if (glassfishHome == null) {
            return;
        }
        asadminLenient("stop-domain", DOMAIN);
        asadminLenient("stop-database");
    }

    @Test
    @DisplayName("Direct repository operations in a caller-managed transaction")
    void direct() throws Exception {
        assertOk("direct", "exists=true");
    }

    @Test
    @DisplayName("Non-transactional default method, then caller commits")
    void defaultMethodCommit() throws Exception {
        assertOk("default-commit", "exists=true");
    }

    @Test
    @DisplayName("Non-transactional default method, then caller rolls back")
    void defaultMethodRollback() throws Exception {
        assertOk("default-rollback", "exists=false");
    }

    @Test
    @DisplayName("@Transactional default method inside a caller-managed transaction")
    void transactionalDefaultMethod() throws Exception {
        assertOk("default-transactional", "exists=true");
    }

    @Test
    @DisplayName("Nested non-transactional default methods")
    void nestedDefaultMethods() throws Exception {
        assertOk("nested", "exists=true");
    }

    @Test
    @DisplayName("Nested default methods mixing @Transactional and non-transactional ones")
    void nestedMixedDefaultMethods() throws Exception {
        assertOk("nested-transactional", "exists=true");
    }

    private static void assertOk(String scenario, String expected) throws Exception {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(
                URI.create("http://localhost:" + HTTP_PORT + "/" + APP + "/scenario?name=" + scenario)).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Scenario " + scenario + " failed:\n" + response.body());
        assertTrue(response.body().contains(expected), "Scenario " + scenario + " returned: " + response.body());
    }

    /** Replaces the driver jar embedded in GlassFish' OSGi bundle by the one built from this repository. */
    private static void replaceEmbeddedDriver(Path driverJar) throws IOException {
        Path bundle = glassfishHome.resolve("glassfish/modules/jnosql-jakarta-persistence-osgi-bundle.jar");
        Path original = bundle.resolveSibling(bundle.getFileName() + ".orig");
        if (!Files.exists(original)) {
            Files.copy(bundle, original);
        }
        Path patched = bundle.resolveSibling("patched.tmp");
        try (JarFile in = new JarFile(original.toFile());
                JarOutputStream out = new JarOutputStream(Files.newOutputStream(patched))) {
            Enumeration<JarEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                out.putNextEntry(new ZipEntry(entry.getName()));
                if (entry.getName().equals("jnosql-jakarta-persistence.jar")) {
                    Files.copy(driverJar, out);
                } else {
                    try (InputStream is = in.getInputStream(entry)) {
                        is.transferTo(out);
                    }
                }
                out.closeEntry();
            }
        }
        Files.move(patched, bundle, StandardCopyOption.REPLACE_EXISTING);
        // force OSGi to refresh bundles
        deleteRecursively(glassfishHome.resolve("glassfish/domains").resolve(DOMAIN));
        deleteRecursively(glassfishHome.resolve("glassfish/domains/domain1/osgi-cache"));
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var paths = Files.walk(dir)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    private static void asadmin(String... args) throws Exception {
        int exit = runAsadmin(args);
        if (exit != 0 && !args[0].equals("delete-domain") && !args[0].equals("start-database")) {
            throw new IllegalStateException("asadmin " + String.join(" ", args) + " failed with exit code " + exit);
        }
    }

    private static void asadminLenient(String... args) throws Exception {
        runAsadmin(args);
    }

    private static int runAsadmin(String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(glassfishHome.resolve("bin/asadmin").toString());
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        process.getInputStream().transferTo(output);
        process.waitFor(5, TimeUnit.MINUTES);
        System.out.println("asadmin " + String.join(" ", args) + " -> " + process.exitValue() + "\n"
                + output.toString(StandardCharsets.UTF_8));
        return process.exitValue();
    }
}
