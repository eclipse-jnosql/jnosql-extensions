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
import jakarta.data.repository.Repository;
import jakarta.data.restrict.Restriction;

import java.util.List;
import java.util.Set;

@Repository
public interface ComputerFindByRepository extends BasicRepository<Computer, Long> {


    List<Computer> findByModel(String model);

    Set<Computer> findByRelease(long release);

    List<Computer> findByModel(String model, Restriction<Computer> restriction);

    List<Computer> findByRelease(long release, Restriction<Computer> restriction);

    List<Computer> findByModelAndRelease(String model, long release);
}
