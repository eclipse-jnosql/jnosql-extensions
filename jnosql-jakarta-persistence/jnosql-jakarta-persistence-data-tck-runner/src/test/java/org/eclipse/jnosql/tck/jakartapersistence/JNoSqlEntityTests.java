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

import ee.jakarta.tck.data.standalone.entity.EntityTests;

import org.eclipse.jnosql.jakartapersistence.mapping.PersistenceDocumentTemplate;
import org.eclipse.jnosql.jakartapersistence.mapping.spi.JakartaPersistenceExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.extension.ExtendWith;

@EnableAutoWeld
@AddExtensions({JakartaPersistenceExtension.class})
@AddPackages({PersistenceDocumentTemplate.class, EntityManagerProvider.class})
@AddPackages({JNoSqlEntityTests.class, EntityTests.class})
@ExtendWith(TransactionExtension.class)
public class JNoSqlEntityTests extends EntityTests {

}
