/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.lite.mapping.entities;

import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.mapping.NoSQLRepository;

import java.util.List;
import java.util.Map;

@Repository
public interface PersonCrudRepository extends NoSQLRepository<Person, Long> {

    List<Person> findByName(String name);

    @Query("select * from Person where name = @name")
    List<Person> query(@Param("name") String name);

    boolean existsByName(String name);

    long countByName(String name);

    void deleteByName(String name);

    default Map<Boolean, List<Person>> merge(String name) {
        return Map.of(existsByName(name), findByName(name));
    }


    @Find
    List<Person> age(@By("age") int age);

    @Find
    @OrderBy("name")
    List<Person> ageOrderName(@By("age") int age);

    @Find
    @OrderBy("name")
    @OrderBy(value = "id", descending = true)
    List<Person> ageOrderNameId(@By("age") int age);

    CursoredPage<Person> findByName(String name, PageRequest pageRequest);

    @Find
    CursoredPage<Person> findCursor(@By("name") String name, PageRequest pageRequest);

    @Query("select * from Person where name = @name")
    CursoredPage<Person> cursor(@Param("name") String name, PageRequest pageRequest);

    @Query("select * from Person where name = @name")
    Page<Person> offSet(@Param("name") String name, PageRequest pageRequest);

    @Find
    Page<Person> findOffSet(@By("name") String name, PageRequest pageRequest);

    @Find
    @OrderBy("name")
    Page<Person> findOffSet(@By("name") String name, PageRequest pageRequest, Sort<Person> sort);


    @Find
    @OrderBy("name")
    Page<Person> findOffSet(@By("name") String name, PageRequest pageRequest, Order<Person> order);

}
