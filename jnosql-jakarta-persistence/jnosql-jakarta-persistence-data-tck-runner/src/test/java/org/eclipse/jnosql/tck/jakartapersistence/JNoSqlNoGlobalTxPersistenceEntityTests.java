/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
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
 *   Ondro Mihalyi
 */
package org.eclipse.jnosql.tck.jakartapersistence;

import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;

import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;

import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;



import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.tck.jakartapersistence.junit.RunOnly;
import org.eclipse.jnosql.tck.jakartapersistence.junit.RunOnlyCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ee.jakarta.tck.data.standalone.entity.EntityTests;

/**
 * This is a group of PersistenceEntityTests tests that must run outside of a global transaction,
 * otherwise the test scenario doesn't make sense and would always fail. The rest of the tests
 * are executed in {@link JNoSqlPersistenceEntityTests}, with global transactions created with
 * {@link TransactionExtension}
 *
 * @author ondro
 */
@EnableAutoWeld
@AddExtensions({JakartaPersistenceExtension.class})
@AddPackages({PersistenceDocumentTemplate.class, EntityManagerProvider.class})
@AddPackages(value = {JNoSqlNoGlobalTxPersistenceEntityTests.class, EntityTests.class})
@ExtendWith(RunOnlyCondition.class)
public class JNoSqlNoGlobalTxPersistenceEntityTests extends PersistenceEntityTests {

    @Override
    @RunOnly
    @Test
    public void testVersionedInsertUpdateDelete() {
        super.testVersionedInsertUpdateDelete();
    }

    @Override
    @RunOnly
    @Test
    public void testMultipleInsertUpdateDelete() {
        super.testMultipleInsertUpdateDelete();
    }

}
