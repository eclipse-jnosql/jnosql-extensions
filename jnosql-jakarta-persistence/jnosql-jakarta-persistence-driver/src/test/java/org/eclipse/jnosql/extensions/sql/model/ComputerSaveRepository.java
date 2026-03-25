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
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;

import java.util.List;

@Repository
public interface ComputerSaveRepository extends BasicRepository<Computer, Long> {

    @Save
    Computer save(Computer computer);

    @Save
    Computer[] save(Computer[] computers);

    @Save
    List<Computer> save(List<Computer> computers);


    @Save
    void saveVoid(Computer computer);

    @Save
    void saveVoid(Computer[] computers);

    @Save
    void saveVoid(List<Computer> computers);
}
