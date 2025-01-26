/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.tck.jakartapersistence;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Ondro Mihalyi
 */
@Tag("development")
@DisplayName(value = "Selected PersistenceEntity tests")
@EnableAutoWeld
public class SelectedJNoSqlPersistenceEntityTests extends JNoSqlPersistenceEntityTests {

    @Override
    @Test
    public void testInsertEntityThatAlreadyExists() {
        super.testInsertEntityThatAlreadyExists();
    }

    @Override
    @Test
    public void testQueryWithPositionalParameters() {
        super.testQueryWithPositionalParameters();
    }

    @Override
    @Test
    public void testLike() {
        super.testLike();
    }

}
