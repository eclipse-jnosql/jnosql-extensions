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
        final int lastPossibleStartIndex = query.length() -1 - lengthOfWord - 1; // word must be followed by a space
        while (startIndex <= lastPossibleStartIndex && Character.isWhitespace(firstNonWhiteChar = query.charAt(startIndex))) {
            startIndex++;
        }
        if (startIndex <= lastPossibleStartIndex
                && query.regionMatches(true, startIndex, word, 0, lengthOfWord - 1)
                && Character.isWhitespace(query.charAt(startIndex + lengthOfWord)) ) {
            return true;
        }
        return false;
    }



}
