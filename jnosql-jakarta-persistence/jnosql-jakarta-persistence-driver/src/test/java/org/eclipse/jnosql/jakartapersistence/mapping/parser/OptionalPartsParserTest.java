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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Test adding missing parts in SELECT.
 *
 * @author Petr Aubrecht
 */
public class OptionalPartsParserTest {

    /**
     * Test empty select.
     */
    @Test
    public void testEmptySelect() {
        OptionalPartsParser parser = new OptionalPartsParser("", "Person");
        String complete = parser.getCompleteSelect();
        assertEquals("SELECT this FROM Person", complete, "Empty select should select all people, filling both SELECT and FROM");
    }

    /**
     * Test missing select, but with FROM.
     */
    @Test
    public void testMissingSelect() {
        OptionalPartsParser parser = new OptionalPartsParser("FROM Person", "Person");
        String complete = parser.getCompleteSelect();
        assertEquals("SELECT this FROM Person", complete, "Missing select should be added, keeps FROM");
    }

    /**
     * Test missing FROM, but with SELECT.
     */
    @Test
    public void testMissingFrom() {
        OptionalPartsParser parser = new OptionalPartsParser("SELECT this.name", "Person");
        String complete = parser.getCompleteSelect();
        assertEquals("SELECT this.name FROM Person", complete, "Missing select should be added, keeps FROM");
    }

    // TODO test COUNT
    // TODO test WHERE
    // TODO test spaces everywhere
}
