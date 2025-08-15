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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Ondro Mihalyi
 */
public class QLUtilTest {

    @CsvSource(useHeadersInDisplayName = true,
            delimiter = '|',
            textBlock =
"""
selectQuery                                                                                                | expectedCountQuery
SELECT e FROM Employee e WHERE e.department = 'IT'                                                         | SELECT COUNT(this) FROM Employee e WHERE e.department = 'IT'
SELECT DISTINCT e FROM Employee e WHERE e.salary > 50000"                                                  | SELECT COUNT(DISTINCT this) FROM Employee e WHERE e.salary > 50000"
SELECT e.name, e.salary FROM Employee e JOIN e.department d WHERE d.name = 'IT' ORDER BY e.salary DESC     | SELECT COUNT(this) FROM Employee e JOIN e.department d WHERE d.name = 'IT'
SELECT e FROM Employee e WHERE e.department = 'IT' GROUP BY e.department                                   | SELECT COUNT(this) FROM Employee e WHERE e.department = 'IT' GROUP BY e.department
SELECT COUNT(e), e.department FROM Employee e GROUP BY e.department                                        | SELECT COUNT(this) FROM Employee e GROUP BY e.department
SELECT id FROM NaturalNumber WHERE isOdd = true AND id BETWEEN 21 AND ?1 ORDER BY id ASC                   | SELECT COUNT(this) FROM NaturalNumber WHERE isOdd = true AND id BETWEEN 21 AND ?1
""")
    @ParameterizedTest
        void convertToCount(String selectQuery, String expectedCountQuery) {
            assertEquals(sanitize(expectedCountQuery), sanitize(QLUtil.convertToCount(selectQuery)), "Query " + selectQuery);
        }

    private static String sanitize(String text) {
        return text.replaceAll(" +", " ").trim();
    }

}
