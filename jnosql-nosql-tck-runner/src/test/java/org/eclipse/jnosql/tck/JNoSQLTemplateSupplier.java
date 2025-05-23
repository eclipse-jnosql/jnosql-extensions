/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.tck;

import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Template;
import jakarta.nosql.tck.TemplateSupplier;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfigurations;
import org.eclipse.jnosql.mapping.core.config.MappingConfigurations;

import static org.eclipse.jnosql.tck.DocumentDatabase.INSTANCE;

public class JNoSQLTemplateSupplier implements TemplateSupplier {

    public static final String DATABASE_NAME = "tck";

    static {
        System.setProperty(MongoDBDocumentConfigurations.HOST.get() + ".1", INSTANCE.host());
        System.setProperty(MappingConfigurations.DOCUMENT_DATABASE.get(), DATABASE_NAME);
        SeContainerInitializer.newInstance().initialize();
    }

    @Override
    public Template get() {
        return CDI.current().select(Template.class).get();
    }
}
