/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *  Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.repository;

import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.reflection.FieldReader;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;

abstract class AbstractTestRepository {

    @SuppressWarnings("unchecked")
    @WeldSetup
    WeldInitiator weld = WeldInitiator.of(
            WeldInitiator.createWeld()
                    .addBeanClasses(
                            SqlTemplateFactory.class,
                            SqlRepositoryAdapterTest.class,
                            SqlRepositoryProducer.class,
                            ProjectorConverter.class
                    )
                    .addPackages(true, CoreDeleteOperation.class)
                    .addPackages(true, FieldReader.class)
                    .addPackages(true, SqlRepositoryOperationProvider.class)
                    .addExtensions(ReflectionEntityMetadataExtension.class)
    );

}
