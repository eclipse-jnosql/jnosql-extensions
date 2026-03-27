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
import jakarta.data.repository.Update;

import java.util.List;

@Repository
public interface ComputerCountByRepository extends BasicRepository<Computer, Long> {

    @Update
    Computer update(Computer computer);

    @Update
    Computer[] update(Computer[] computers);

    @Update
    List<Computer> update(List<Computer> computers);


    @Update
    void updateVoid(Computer computer);

    @Update
    void updateVoid(Computer[] computers);

    @Update
    void updateVoid(List<Computer> computers);
}
