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

import jakarta.data.page.CursoredPage;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.First;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.lite.mapping.entities.Person;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface PersonRepository extends BasicRepository<Person, Long> {

    List<Person> findByUsername(String username);

    long countByUsername(String firstName);

    boolean existsByUsername(String firstName);

    void deleteByUsername(String firstName);

    @Find
    CursoredPage<Person> cursor(String firstName, Pageable pageable);


    @Find
    @First(10)
    @OrderBy(value = "username", descending = true)
    @OrderBy(value = "email")
    List<Person> findTopTen(@Param("name") String name);

    @Find
    @First
    @OrderBy(value = "email")
    List<Person> findTopOne(@Param("name") String name);

    @Find
    List<Person> name(@Param("name") String name);
}
