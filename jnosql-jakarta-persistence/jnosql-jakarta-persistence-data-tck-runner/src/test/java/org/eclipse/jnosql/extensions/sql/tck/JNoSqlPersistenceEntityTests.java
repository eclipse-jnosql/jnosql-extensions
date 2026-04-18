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

import ee.jakarta.tck.data.standalone.entity.EntityTests;
import org.eclipse.jnosql.extensions.sql.repository.SqlRepositoryProducer;
import org.eclipse.jnosql.extensions.sql.repository.spi.JakartaPersistenceExtension;
import org.eclipse.jnosql.jakartapersistence.communication.EntityManagerProvider;

import ee.jakarta.tck.data.standalone.persistence.PersistenceEntityTests;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.reflection.FieldReader;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;


@EnableAutoWeld
@AddExtensions({JakartaPersistenceExtension.class, ReflectionEntityMetadataExtension.class})
@AddPackages({PersistenceDocumentTemplate.class, EntityManagerProvider.class,
        CoreDeleteOperation.class, SqlRepositoryProducer.class, FieldReader.class})
@AddPackages({JNoSqlEntityTests.class, EntityTests.class})
@ExtendWith(TransactionExtension.class)
@Disabled("Disable due the migration of the Reflection engine")
public class JNoSqlPersistenceEntityTests extends PersistenceEntityTests {

    /**
     * This test expects running outside of a global transaction. It should be
     * executed in {@link JNoSqlPersistenceEntityTestsNoGlobalTx}
     */
    @Override
    @Disabled
    public void testVersionedInsertUpdateDelete() {
        super.testVersionedInsertUpdateDelete();
    }

    /**
     * This test expects running outside of a global transaction. It should be
     * executed in {@link JNoSqlPersistenceEntityTestsNoGlobalTx}
     */
    @Override
    @Disabled
    public void testMultipleInsertUpdateDelete() {
        super.testMultipleInsertUpdateDelete();
    }

}
