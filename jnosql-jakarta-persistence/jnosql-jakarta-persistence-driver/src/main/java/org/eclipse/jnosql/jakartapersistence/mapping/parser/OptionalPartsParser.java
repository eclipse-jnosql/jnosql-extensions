/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
 *  Petr Aubrecht
 */
package org.eclipse.jnosql.jakartapersistence.mapping.parser;

import jakarta.data.Sort;

import java.util.Collection;

/**
 * Parse the beginning of the SELECT command, fill the optional parts and return
 * complete query.
 *
 * @author Petr Aubrecht
 */
public class OptionalPartsParser {

    private static final String SELECT = "SELECT";
    private static final String COUNT = "COUNT";
    private static final String THIS = "THIS";
    private static final String DOT = ".";
    private static final String FROM = "FROM";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";

    private String queryString;
    private String entity;
    private Collection<Sort<?>> sorts;
    private int position = 0;
    private StringBuilder updatedQueryString = new StringBuilder();

    public OptionalPartsParser(String queryString, String entity) {
        this(queryString, entity, null);
    }

    public OptionalPartsParser(String queryString, String entity, Collection<Sort<?>> sorts) {
        this.queryString = queryString;
        this.entity = entity;
        this.sorts = sorts;
        selectStatement();
    }

    public String getCompleteSelect() {
        return updatedQueryString.toString().stripTrailing();
    }

    /**
     * select_statement : select_clause? from_clause? where_clause?
     * orderby_clause?;
     */
    private void selectStatement() {
        skipOpionalSpace();
        if (startsWith(SELECT)) {
            selectClause();
        } else {
            updatedQueryString.append(SELECT + " this");
        }
        skipSpace();
        if (startsWith(FROM)) {
            fromClause();
        } else {
            updatedQueryString.append(FROM + " " + entity);
        }
        skipSpace();
        // do not care about the rest
        copyRest();
        appendSorts();
    }

    /**
     * select_clause : 'SELECT' select_list;
     */
    private void selectClause() {
        skip(SELECT);
        selectList();
    }

    /**
     * from_clause : 'FROM' entity_name;
     */
    private void fromClause() {
        skip(FROM);
        skipSpace();
        identifier(); // entity_name : identifier
    }

    /**
     * Copy the remaining part of the SELECT command.
     */
    private void copyRest() {
        updatedQueryString.append(queryString.substring(position));
    }

    /**
     * Add ORDER BY with sorts. The original query shouldn't have ORDER BY if there are sorts,
     * otherwise we'll get a corrupted query, with 2 ORDER BY clauses
     */
    private void appendSorts() {
        if (sorts != null && !sorts.isEmpty()) {
            updatedQueryString.append("ORDER BY ");
            boolean firstItem = true;
            for (Sort<?> sort : sorts) {
                if (!firstItem) {
                    updatedQueryString.append(",");
                }
                if (sort.ignoreCase()) {
                    updatedQueryString.append("UPPER(");
                }
                updatedQueryString.append(sort.property());
                if (sort.ignoreCase()) {
                    updatedQueryString.append(")");
                }
                if (sort.isAscending()) {
                    updatedQueryString.append(" ASC");
                }
                if (sort.isDescending()) {
                    updatedQueryString.append(" DESC");
                }
            }
        }
    }

    /**
     * select_list : state_field_path_expression | aggregate_expression;
     */
    private void selectList() {
        skipSpace();
        if (startsWith(COUNT)) {
            aggregateExpression();
        } else {
            stateFieldPathExpression();
        }
    }

    /**
     * aggregate_expression : 'COUNT' '(' 'THIS' ')';
     */
    private void aggregateExpression() {
        skip(COUNT);
        skipOpionalSpace();
        testAndskip(OPEN_PARENTHESIS);
        skipOpionalSpace();
        testAndskip(THIS);
        skipOpionalSpace();
        testAndskip(CLOSE_PARENTHESIS);
    }

    /**
     * state_field_path_expression : IDENTIFIER ('.' IDENTIFIER)*;
     */
    private void stateFieldPathExpression() {
        identifier();
        skipOpionalSpace();
        while (startsWith(DOT)) {
            skip(DOT);
            skipOpionalSpace();
            identifier();
            skipOpionalSpace();
        }
    }

    /**
     * Identifier is any Java identifier
     */
    private void identifier() {
//        skipOpionalSpace();
        if (position >= queryString.length() || !Character.isJavaIdentifierStart(queryString.charAt(position))) {
            throw new IllegalArgumentException("Expected identifier at position " + position);
        }
        int start = position;
        position++;
        while (position < queryString.length() && Character.isJavaIdentifierPart(queryString.charAt(position))) {
            position++;
        }

        updatedQueryString.append(queryString, start, position);
    }

    private boolean startsWith(String terminal) {
        return position + terminal.length() <= queryString.length()
                && queryString.regionMatches(true, position, terminal, 0, terminal.length());
    }

    private void skip(String terminal) {
        updatedQueryString.append(terminal);
        position += terminal.length();
    }

    private void testAndskip(String terminal) {
        if (!startsWith(terminal)) {
            throw new IllegalArgumentException("Expected " + terminal + " at position " + position);
        }
        skip(terminal);
    }

    private void skipOpionalSpace() {
        while (position < queryString.length() && Character.isWhitespace(queryString.charAt(position))) {
            position++;
        }
    }

    private void skipSpace() {
        skipOpionalSpace();
        // replace all spaces with one space
        updatedQueryString.append(" ");
    }

}
