/*
 * Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *  Contributors:
 *
 *  Ondro Mihalyi
 */
package org.eclipse.jnosql.jakartapersistence.mapping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ondro Mihalyi
 */
final class QLUtil {

    private static final String UPDATE_KEYWORD = "UPDATE";
    private static final String DELETE_KEYWORD = "DELETE";

    private QLUtil() {
    }

    static boolean isUpdateQuery(String query) {
        return queryStartsWith(query, UPDATE_KEYWORD, UPDATE_KEYWORD.length());
    }

    static boolean isDeleteQuery(String query) {
        return queryStartsWith(query, DELETE_KEYWORD, DELETE_KEYWORD.length());
    }

    static boolean queryStartsWith(String query, String word, int lengthOfWord) {
        int startIndex = 0;
        char firstNonWhiteChar;
        final int lastPossibleStartIndex = query.length() - 1 - lengthOfWord - 1; // word must be followed by a space
        while (startIndex <= lastPossibleStartIndex && Character.isWhitespace(firstNonWhiteChar = query.charAt(startIndex))) {
            startIndex++;
        }
        if (startIndex <= lastPossibleStartIndex
                && query.regionMatches(true, startIndex, word, 0, lengthOfWord - 1)
                && Character.isWhitespace(query.charAt(startIndex + lengthOfWord))) {
            return true;
        }
        return false;
    }

    public static String convertToCount(String originalJpql) {
        return JpqlCountConverter.convertToCount(originalJpql);
    }

    static private class JpqlCountConverter {

        private static final Pattern SELECT_PATTERN = Pattern.compile(
                "^\\s*SELECT\\s+(DISTINCT\\s+)?(.+?)\\s+FROM\\s+",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        private static final Pattern FROM_PATTERN = Pattern.compile(
                "\\s+FROM\\s+([^\\s]+(?:\\s+(?:AS\\s+)?[^\\s]+)?)",
                Pattern.CASE_INSENSITIVE
        );

        private static final Pattern ORDER_BY_PATTERN = Pattern.compile(
                "\\s+ORDER\\s+BY\\s+.+$",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        private static final Pattern GROUP_BY_PATTERN = Pattern.compile(
                "(\\s+GROUP\\s+BY\\s+.+?)(?:\\s+ORDER\\s+BY\\s+.+)?$",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        private static final Pattern HAVING_PATTERN = Pattern.compile(
                "(\\s+HAVING\\s+.+?)(?:\\s+ORDER\\s+BY\\s+.+)?$",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        /**
         * Converts a JPQL SELECT query to a COUNT JPQL query
         *
         * @param jpql The original JPQL query
         * @return The converted COUNT JPQL query
         * @throws IllegalArgumentException if the query cannot be converted
         */
        public static String convertToCount(String jpql) {
            if (jpql == null || jpql.trim().isEmpty()) {
                throw new IllegalArgumentException("JPQL query cannot be null or empty");
            }

            String normalizedJpql = jpql.trim();

            if (!normalizedJpql.toUpperCase().startsWith("SELECT")) {
                throw new IllegalArgumentException("Only SELECT queries can be converted to COUNT queries");
            }

            validateQuery(normalizedJpql);

            try {
                return performConversion(normalizedJpql);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to convert JPQL query \"" + jpql + "\" to a COUNT query: " + e.getMessage(), e);
            }
        }

        private static void validateQuery(String jpql) {
            String upperJpql = jpql.toUpperCase();

            if (upperJpql.contains(" UNION ")) {
                throw new IllegalArgumentException("UNION queries are not supported for COUNT conversion");
            }

            if (upperJpql.matches(".*\\bSELECT\\b.*\\bFROM\\b.*\\bSELECT\\b.*")) {
                throw new IllegalArgumentException("Subqueries in SELECT clause are not supported for COUNT conversion");
            }
        }

        private static String performConversion(String jpql) {
            StringBuilder result = new StringBuilder();

            Matcher selectMatcher = SELECT_PATTERN.matcher(jpql);
            if (!selectMatcher.find()) {
                throw new IllegalArgumentException("Invalid SELECT clause format");
            }

            boolean hasDistinct = selectMatcher.group(1) != null;

            int fromIndex = selectMatcher.end(2);
            String fromAndAfter = jpql.substring(fromIndex);

            if (hasDistinct) {
                result.append("SELECT COUNT(DISTINCT this)");
            } else {
                result.append("SELECT COUNT(this)");
            }

            String fromWithConditions = removeOrderBy(fromAndAfter);
            fromWithConditions = removeHaving(fromWithConditions);

            result.append(" ");
            result.append(fromWithConditions);

            return result.toString();
        }

        private static String removeOrderBy(String jpql) {
            return ORDER_BY_PATTERN.matcher(jpql).replaceAll("");
        }

        private static boolean containsGroupBy(String jpql) {
            return GROUP_BY_PATTERN.matcher(jpql).find();
        }

        private static String removeHaving(String jpql) {
            return HAVING_PATTERN.matcher(jpql).replaceAll("");
        }

    }
}
