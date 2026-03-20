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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.eclipse.jnosql.extensions.sql.SqlTemplate;
import org.eclipse.jnosql.extensions.sql.SqlTemplateFactory;
import org.eclipse.jnosql.extensions.sql.model.Computer;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;

@EnableWeld
class NoSQLRepositorySqlAdapterTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    SqlTemplateFactory.class,
                    NoSQLRepositorySqlAdapterTest.class
            )
            .build();

    @Inject
    private SqlTemplate template;

    private NoSQLRepository<Computer, Long> repository;

    @Produces
    @ApplicationScoped
    public SqlTemplate createEntityManager() {
        EntityManagerFactory persistenceUnit = Persistence.createEntityManagerFactory("testPersistenceUnit");
        var entityManager = persistenceUnit.createEntityManager();
        var sqlTemplateFactory = new SqlTemplateFactory();
        return sqlTemplateFactory.create(entityManager);
    }

    @BeforeEach
    void setUp() {
        repository = new NoSQLRepositorySqlAdapter<> (Computer.class, template);
    }

}