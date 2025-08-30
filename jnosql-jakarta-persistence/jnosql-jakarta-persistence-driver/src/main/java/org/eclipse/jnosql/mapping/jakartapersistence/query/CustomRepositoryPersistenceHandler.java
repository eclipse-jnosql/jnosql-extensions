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
package org.eclipse.jnosql.mapping.jakartapersistence.query;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.CustomRepositoryHandler;

/**
 *
 * @author Ondro Mihalyi
 */
public class CustomRepositoryPersistenceHandler extends CustomRepositoryHandler {

    public CustomRepositoryPersistenceHandler(EntitiesMetadata entitiesMetadata, SemiStructuredTemplate template, Class<?> customRepositoryType, Converters converters) {
        super(entitiesMetadata, template, customRepositoryType, converters);
    }

}
