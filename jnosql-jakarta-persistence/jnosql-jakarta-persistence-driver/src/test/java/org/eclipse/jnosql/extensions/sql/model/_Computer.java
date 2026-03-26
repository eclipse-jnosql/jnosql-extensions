/*
 *  Copyright (c) 2025 Otávio Santana and others
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
 *   Otavio Santana
 */
package org.eclipse.jnosql.extensions.sql.model;

import jakarta.data.metamodel.NumericAttribute;
import jakarta.data.metamodel.StaticMetamodel;
import jakarta.data.metamodel.TextAttribute;

import javax.annotation.processing.Generated;

//CHECKSTYLE:OFF
@StaticMetamodel(Computer.class)
@Generated(value = "The StaticMetamodel of the class Computer provider by Eclipse JNoSQL", date = "2025-06-09T09:16:59.979587")
public interface _Computer {

    String ID = "id";
    String MODEL = "model";
    String RELEASE = "release";

    TextAttribute<Computer> id = TextAttribute.of(Computer.class, ID);
    TextAttribute<Computer> model = TextAttribute.of(Computer.class, MODEL);
    NumericAttribute<Computer, Long> release = NumericAttribute.of(Computer.class, RELEASE, long.class);

}
//CHECKSTYLE:ON