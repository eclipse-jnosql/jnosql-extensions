/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.extensions.sql.repository;

import jakarta.inject.Inject;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.model.ComputerCountByRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@EnableWeld
@DisplayName("Count Operation Repository Tests")
class CountByOperationRepositoryTest extends AbstractTestRepository {

    @Inject
    private SqlTemplate template;

    @Inject
    private SqlRepositoryProducer producer;

    private ComputerCountByRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = producer.get(ComputerCountByRepository.class, template);
    }


}