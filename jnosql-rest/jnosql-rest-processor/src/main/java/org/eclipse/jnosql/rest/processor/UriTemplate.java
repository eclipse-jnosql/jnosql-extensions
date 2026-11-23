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
package org.eclipse.jnosql.rest.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A Jakarta REST URI template relative to the resource path, validated and normalized at compile time.
 *
 * <p>An empty template denotes the resource root. The template is normalized to start with {@code /} and not to
 * end with one, so {@code ""} and {@code "/"} are the same route.</p>
 *
 * @param path      the normalized template, empty for the root
 * @param variables the template variables in declaration order, without their optional regular expressions
 * @param problem   why the template is invalid, or {@code null}
 */
record UriTemplate(String path, List<String> variables, String problem) {

    private static final Pattern NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_.-]*");

    /**
     * Parses and normalizes a template.
     *
     * @param raw the value of {@code @Path} or a derived path
     * @return the template, which carries a problem when it is invalid
     */
    static UriTemplate of(String raw) {
        String path = normalize(raw);
        List<String> variables = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int index = 0; index < path.length(); index++) {
            char current = path.charAt(index);
            if (current == '{') {
                if (depth == 0) {
                    start = index + 1;
                }
                depth++;
            } else if (current == '}') {
                if (depth == 0) {
                    return invalid(path, variables, "it closes a variable that was not opened");
                }
                depth--;
                if (depth == 0) {
                    String content = path.substring(start, index);
                    int colon = content.indexOf(':');
                    String name = (colon < 0 ? content : content.substring(0, colon)).trim();
                    if (!NAME.matcher(name).matches()) {
                        return invalid(path, variables, "the variable '{" + content + "}' has no valid name");
                    }
                    if (colon >= 0) {
                        try {
                            Pattern.compile(content.substring(colon + 1).trim());
                        } catch (PatternSyntaxException exception) {
                            return invalid(path, variables, "the expression of '{" + content + "}' is not a valid "
                                    + "regular expression (" + exception.getDescription() + ")");
                        }
                    }
                    if (variables.contains(name)) {
                        return invalid(path, variables, "the variable {" + name + "} is declared twice");
                    }
                    variables.add(name);
                }
            }
        }
        if (depth != 0) {
            return invalid(path, variables, "a variable is not closed");
        }
        return new UriTemplate(path, List.copyOf(variables), null);
    }

    /**
     * The route shape, in which every variable is anonymous, so templates that differ only by variable names
     * or regular expressions are equal.
     *
     * @return the shape used to detect colliding routes
     */
    String shape() {
        StringBuilder shape = new StringBuilder();
        int depth = 0;
        for (int index = 0; index < path.length(); index++) {
            char current = path.charAt(index);
            if (current == '{') {
                if (depth == 0) {
                    shape.append("{}");
                }
                depth++;
            } else if (current == '}') {
                depth--;
            } else if (depth == 0) {
                shape.append(current);
            }
        }
        return shape.toString();
    }

    boolean isRoot() {
        return path.isEmpty();
    }

    private static UriTemplate invalid(String path, List<String> variables, String problem) {
        return new UriTemplate(path, List.copyOf(variables), problem);
    }

    private static String normalize(String raw) {
        String path = raw == null ? "" : raw.trim();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path.isEmpty() || path.startsWith("/") ? path : "/" + path;
    }
}
