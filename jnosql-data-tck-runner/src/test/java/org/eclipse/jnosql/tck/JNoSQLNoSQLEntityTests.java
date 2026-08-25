/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Kyle Aure
 */
package org.eclipse.jnosql.tck;

import ee.jakarta.tck.data.standalone.nosql.example.NoSQLEntityTests;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfigurations;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.config.MappingConfigurations;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.document.DocumentTemplateProducer;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;

import static org.eclipse.jnosql.tck.DocumentDatabase.INSTANCE;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
@AddPackages(DocumentTemplateProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, DocumentExtension.class})
public class JNoSQLNoSQLEntityTests extends NoSQLEntityTests {

    public static final String DATABASE_NAME = "tck";

    static {
        INSTANCE.get(DATABASE_NAME);
        System.setProperty(MongoDBDocumentConfigurations.HOST.get() + ".1", INSTANCE.host());
        System.setProperty(MappingConfigurations.DOCUMENT_DATABASE.get(), DATABASE_NAME);
    }

}
