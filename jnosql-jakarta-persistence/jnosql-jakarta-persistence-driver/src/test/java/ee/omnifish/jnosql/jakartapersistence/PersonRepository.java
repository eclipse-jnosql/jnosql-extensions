/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Ondro Mihalyi
 */
package ee.omnifish.jnosql.jakartapersistence;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface PersonRepository extends CrudRepository<Person, String> {
    long countAll();
    long countByNameNotNull();
    List<Person> findByNameAndAgeLessThanEqual(String name, long age);
    List<Person> findByNameIn(Set<String> names);
    List<Person> findByNameIgnoreCaseNot(String name);
    List<Person> findBySsn(SocialSecurityNumber ssn);

    @Find
    List<Person> personsBySsn(@By("ssn") SocialSecurityNumber ssn);
}
