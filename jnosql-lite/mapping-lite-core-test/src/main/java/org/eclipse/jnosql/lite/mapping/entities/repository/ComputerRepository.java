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
package org.eclipse.jnosql.lite.mapping.entities.repository;

import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.lite.mapping.entities.Computer;

@Repository
public interface ComputerRepository extends DataRepository<Computer, String> {

    @Insert
    void insert(Computer computer);

    @Update
    void update(Computer computer);

    @Delete
    void delete(Computer computer);
}
