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
package org.eclipse.jnosql.extensions.sql.model;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface ComputerFindRepository extends BasicRepository<Computer, Long> {

    @Find
    List<Computer> allComputers();

    @Find
    List<Computer> computersBy(@By("id") long id);

    @Find
    List<Computer> computersBy(@By("model") String model);

    @Find
    List<ComputerSummary> computersByRelease(@By("release") long release);
}
