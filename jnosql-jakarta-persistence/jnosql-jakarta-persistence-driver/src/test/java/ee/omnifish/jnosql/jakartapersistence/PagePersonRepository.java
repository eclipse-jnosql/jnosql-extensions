/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 which accompanies this distribution.
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

@Repository
public interface PagePersonRepository extends CrudRepository<Person, String> {

    @Query("FROM Person WHERE name LIKE :name ORDER BY name")
    Page<Person> findByNameLike(@Param("name") String name, PageRequest pageRequest);

    @Query("DELETE FROM Person")
    void deleteAllPersons();
}
