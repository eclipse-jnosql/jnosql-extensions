/*
 *  Copyright (c) 2026 Otávio Santana and others
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
package org.eclipse.jnosql.lite.mapping.entities.projection;

import jakarta.data.repository.Select;
import jakarta.nosql.Column;
import jakarta.nosql.Projection;
import org.eclipse.jnosql.lite.mapping.entities.Person;

import java.math.BigDecimal;
import java.time.LocalDate;


@Projection(from = Person.class)
public record PersonSummary(@Select("final_name") String name,
                            @Column("birthday") LocalDate release,
                            @Select("salary") BigDecimal price) {
}
