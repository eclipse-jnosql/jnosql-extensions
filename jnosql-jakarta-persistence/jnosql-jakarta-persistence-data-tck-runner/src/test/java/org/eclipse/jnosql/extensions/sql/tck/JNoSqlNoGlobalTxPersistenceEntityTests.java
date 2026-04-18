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
package org.eclipse.jnosql.extensions.sql.tck;

import org.eclipse.jnosql.extensions.sql.repository.SqlRepositoryProducer;
import org.eclipse.jnosql.extensions.sql.repository.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;

import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;

import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.reflection.FieldReader;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;



import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.extensions.sql.tck.junit.RunOnly;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ee.jakarta.tck.data.standalone.entity.EntityTests;

/**
 * This is a group of PersistenceEntityTests tests that must run outside of a global transaction,
 * otherwise the test scenario doesn't make sense and would always fail. The rest of the tests
 * are executed in {@link JakartaPersistenceEntityTests}, with global transactions created with
 * {@link TransactionExtension}
 *
 * @author ondro
 */
@EnableAutoWeld
@AddExtensions({JakartaPersistenceExtension.class, ReflectionEntityMetadataExtension.class})
@AddPackages({PersistenceDocumentTemplate.class, EntityManagerProvider.class,
        CoreDeleteOperation.class, SqlRepositoryProducer.class, FieldReader.class})
@AddPackages({JakartaPersitenceEntityTests.class, EntityTests.class})
@ExtendWith(TransactionExtension.class)
@Disabled("Disable due the migration of the Reflection engine")
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
